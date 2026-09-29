package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

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
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder)
    {
        builder.add(POWERED, HORIZONTAL_FACING);
    }

    // Determina in che direzione "guarda" il blocco quando viene piazzato
    @Nullable
    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext context)
    {
        // getNearestLookingDirection al posto di getHorizontalDirection include anche le direzioni UP e DOWN
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state)
    {
        return new HostBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(@NonNull BlockState state,
                                   @NonNull Level level,
                                   @NonNull BlockPos pos,
                                   @NonNull Block block,
                                   Orientation orientation,
                                   boolean movedByPiston)
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
                    hostEntity.startJob(new PingJob(new Ipv4Address("192.168.1." + i), 1, hostEntity), false);
                }

                hostEntity.startJob(new PingJob(new Ipv4Address("192.168.1.1"), 1, hostEntity), false);
                hostEntity.startJob(new PingJob(new Ipv4Address("192.168.2.1"), 1, hostEntity), false);
                hostEntity.startJob(new PingJob(new Ipv4Address("192.168.2.11"), 1, hostEntity), false);
                hostEntity.startJob(new PingJob(new Ipv4Address("192.168.2.12"), 1, hostEntity), false);
            }
        }

        // The '3' is a flag that tells Minecraft to update the block and notify clients.
        level.setBlock(pos, state.setValue(POWERED, isReceivingPower), 3);
    }
}
