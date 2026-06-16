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

        nics.put(Direction.NORTH, new NetworkInterface(pos, Direction.NORTH, ConnectorType.RJ45));
        nics.put(Direction.SOUTH, new NetworkInterface(pos, Direction.SOUTH, ConnectorType.RJ45));
        nics.put(Direction.EAST, new NetworkInterface(pos, Direction.EAST, ConnectorType.RJ45));
        nics.put(Direction.WEST, new NetworkInterface(pos, Direction.WEST, ConnectorType.RJ45));
    }

    @Override
    public int getDeviceLayer()
    {
        return 2;
    }

    public Map<MacAddress, Direction> getSwitchingTable()
    {
        return this.l2Engine.getSwitchingTable();
    }
}
