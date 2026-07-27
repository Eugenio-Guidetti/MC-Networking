package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingTable;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class RouterBlockEntity extends NetworkingBlockEntity
{
    private final RoutingL3Engine l3Engine = new RoutingL3Engine(this);

    public RouterBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.ROUTER_BLOCK_ENTITY, pos, blockState);

        this.stack.setL3Engine(l3Engine);

        hostname = "Router";

        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth0", pos, Direction.NORTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth1", pos, Direction.SOUTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth2", pos, Direction.EAST, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth3", pos, Direction.WEST, ConnectorType.RJ45));
    }

    public RoutingTable getRoutingTable()
    {
        return this.l3Engine.getRoutingTable();
    }

    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return l3Engine.getArpManager().getArpCache();
    }


    @Override
    public void tickServer(Level level)
    {
        super.tickServer(level);

        l3Engine.getArpManager().tick(this);
    }

    @Override
    public int getDeviceLayer()
    {
        return 3;
    }
}
