package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
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
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class SwitchBlockEntity extends NetworkingBlockEntity
{
    private Map<MacAddress, Direction> switchingTable = new HashMap<>();


    public SwitchBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(ModBlockEntities.SWITCH_BLOCK_ENTITY, pos, blockState);

        nics.put(Direction.NORTH, new NetworkInterface(pos, Direction.NORTH, ConnectorType.RJ45));
        nics.put(Direction.SOUTH, new NetworkInterface(pos, Direction.SOUTH, ConnectorType.RJ45));
        nics.put(Direction.EAST, new NetworkInterface(pos, Direction.EAST, ConnectorType.RJ45));
        nics.put(Direction.WEST, new NetworkInterface(pos, Direction.WEST, ConnectorType.RJ45));
    }

    @Override
    public void receiveFrame(@NotNull EthernetFrame frame, @NonNull Direction from)
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        NetworkInterface receivingNic = nics.get(from);

        // Evito di inserire nella switching table indirizzi MAC vuoti o di broadcast (non dovrebbe capitare)
        if (!frame.sourceMac().equals(MacAddress.ALL_ZEROS) && !frame.sourceMac().equals(MacAddress.BROADCAST))
        {
            // Aggiunta indirizzi MAC (lo switch "impara" la rete)
            switchingTable.put(frame.sourceMac(), from);
        }

        if (frame.destMac().equals(receivingNic.getMacAddress()))
        {
            // Pacchetto rivolto allo switch, per ora non fare nulla (considero solo switch L2)

            return;
        }

        if (switchingTable.containsKey(frame.destMac()))
        {
            NetworkInterface sendingNic = nics.get(switchingTable.get(frame.destMac()));

            if (sendingNic.equals(receivingNic))
            {
                // Droppo il frame

                return;
            }

            sendingNic.sendPacket(frame);

            return;
        }

        // Lo switch non sa dove inoltrare il frame
        floodFrame(frame, from);
    }
}
