package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.SwitchingL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class SwitchBlockEntity extends NetworkingBlockEntity
{
    private final SwitchingL2Engine l2Engine = new SwitchingL2Engine();

    public SwitchBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.SWITCH_BLOCK_ENTITY, pos, blockState);

        this.stack.setL2Engine(l2Engine);

        hostname = "Switch";

        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth0", pos, Direction.NORTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth1", pos, Direction.SOUTH, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth2", pos, Direction.EAST, ConnectorType.RJ45));
        putInterface(new NetworkInterface(MacAddress.ALL_ZEROS, "eth3", pos, Direction.WEST, ConnectorType.RJ45));
    }

    @Override
    public int getDeviceLayer()
    {
        return 2;
    }

    public Map<MacAddress, String> getSwitchingTable()
    {
        return this.l2Engine.getSwitchingTable();
    }
}
