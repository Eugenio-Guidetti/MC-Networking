package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class SwitchingL2Engine extends AbstractL2Engine
{
    protected final Map<MacAddress, String> switchingTable = new HashMap<>();

    public SwitchingL2Engine(NetworkingBlockEntity netEntity)
    {
        super(netEntity);
    }

    @Override
    public void processFrame(@NonNull EthernetFrame frame, @NonNull String from)
    {
        // Evito di inserire nella switching table indirizzi MAC di broadcast o vuoti (non dovrebbe capitare)
        if (!frame.sourceMac().equals(MacAddress.ALL_ZEROS) && !frame.sourceMac().equals(MacAddress.BROADCAST))
        {
            switchingTable.put(frame.sourceMac(), from);
        }

        // Inoltro broadcast
        if (frame.destMac().equals(MacAddress.BROADCAST))
        {
            floodFrame(frame, from);
            return;
        }

        String outName = switchingTable.get(frame.destMac());

        // Non so a chi mandare il frame
        if (outName == null)
        {
            floodFrame(frame, from);
            return;
        }

        if (!outName.equals(from))
        {
            sendFrame(frame, outName);
        }
    }

    public Map<MacAddress, String> getSwitchingTable()
    {
        return switchingTable;
    }
}