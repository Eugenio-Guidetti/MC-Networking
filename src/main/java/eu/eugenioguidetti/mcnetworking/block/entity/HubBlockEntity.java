package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.item.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class HubBlockEntity extends NetworkingBlockEntity
{
    public HubBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.HUB_BLOCK_ENTITY, pos, state);

        nics.put(Direction.NORTH, new NetworkInterface(MacAddress.ALL_ZEROS, pos, Direction.NORTH, ConnectorType.RJ45));
        nics.put(Direction.SOUTH, new NetworkInterface(MacAddress.ALL_ZEROS, pos, Direction.SOUTH, ConnectorType.RJ45));
        nics.put(Direction.EAST, new NetworkInterface(MacAddress.ALL_ZEROS, pos, Direction.EAST, ConnectorType.RJ45));
        nics.put(Direction.WEST, new NetworkInterface(MacAddress.ALL_ZEROS, pos, Direction.WEST, ConnectorType.RJ45));
    }

    // Quando l'hub riceve un pacchetto esegue un flooding su tutte le sue interfacce
    @Override
    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull Direction from)
    {
        floodFrame(frame, from);
    }
}
