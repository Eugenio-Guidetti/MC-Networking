package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class RouterBlockEntity extends NetworkingBlockEntity
{
    public RouterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.ROUTER_BLOCK_ENTITY, pos, blockState);


    }

    @Override
    public void receiveFrame(@NotNull EthernetFrame frame, @NonNull Direction from)
    {

    }
}
