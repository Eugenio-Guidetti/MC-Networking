package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import com.mojang.serialization.MapCodec;
import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Eugenio Guidetti
 */
public class HostBlock extends BaseEntityBlock
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    public HostBlock(Properties settings)
    {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(POWERED, HORIZONTAL_FACING);
    }

    // Determina in che direzione "guarda" il blocco quando viene piazzato
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // getNearestLookingDirection al posto di getHorizontalDirection include anche le direzioni UP e DOWN
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return simpleCodec(HostBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new HostBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston)
    {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

        if (level.isClientSide())
        {
            return;
        }

        // Is there redstone pointing into the block right now?
        boolean isReceivingPower = level.hasNeighborSignal(pos);
        boolean isCurrentlyPowered = state.getValue(POWERED);

        if (isReceivingPower == isCurrentlyPowered)
        {
            return;
        }

        if (isReceivingPower)
        {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof HostBlockEntity hostEntity)
            {
                hostEntity.triggerSendPacket();
            }
        }

        // The '3' is a flag that tells Minecraft to update the block and notify clients.
        level.setBlock(pos, state.setValue(POWERED, isReceivingPower), 3);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        // Registriamo il ticker solo lato Server
        if (level.isClientSide())
        {
            return null;
        }

        return createTickerHelper(type, ModBlockEntities.HOST_BLOCK_ENTITY, HostBlockEntity::tick);
    }
}
