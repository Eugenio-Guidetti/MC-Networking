package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import com.mojang.serialization.MapCodec;
import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ApplicationPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class HostBlock extends NetworkingBlock
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
                for (int i = 10; i < 26; i++)
                {
                    hostEntity.triggerSendPacket(new Ipv4Address("192.168.1." + i),
                                                 new ApplicationPayload("PING from: " + hostEntity.getHostname()));

                    // hostEntity.triggerSendPacket(new Ipv4Address("127.0.0.1"), new ApplicationPayload("PING from: " + hostEntity.getHostname()));

                }

                hostEntity.triggerSendPacket(new Ipv4Address("192.168.1.1"),
                                             new ApplicationPayload("PING from: " + hostEntity.getHostname()));
                hostEntity.triggerSendPacket(new Ipv4Address("192.168.2.1"),
                                             new ApplicationPayload("PING from: " + hostEntity.getHostname()));
                hostEntity.triggerSendPacket(new Ipv4Address("192.168.2.11"),
                                             new ApplicationPayload("PING from: " + hostEntity.getHostname()));
                hostEntity.triggerSendPacket(new Ipv4Address("192.168.2.12"),
                                             new ApplicationPayload("PING from: " + hostEntity.getHostname()));
            }
        }

        // The '3' is a flag that tells Minecraft to update the block and notify clients.
        level.setBlock(pos, state.setValue(POWERED, isReceivingPower), 3);
    }
}
