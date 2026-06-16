package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingL3Engine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 *
 * @author Eugenio Guidetti
 */
public class RouterBlockEntity extends NetworkingBlockEntity
{
    public RouterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.ROUTER_BLOCK_ENTITY, pos, blockState);

        this.stack.setL3Engine(new RoutingL3Engine());

        hostname = "Router";
    }

    @Override
    public int getDeviceLayer()
    {
        return 3;
    }
}
