package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.L2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class SwitchingL2Engine implements L2Engine
{
    protected Map<MacAddress, Direction> switchingTable = new HashMap<>();

    @Override
    public void processFrame(EthernetFrame frame, Direction from, NetworkStack stack)
    {
        // Evito di inserire nella switching table indirizzi MAC vuoti o di broadcast (non dovrebbe capitare)
        if (!frame.sourceMac().equals(MacAddress.ALL_ZEROS) && !frame.sourceMac().equals(MacAddress.BROADCAST))
        {
            switchingTable.put(frame.sourceMac(), from);
        }

        // Inoltro broadcast
        if (frame.destMac().equals(MacAddress.BROADCAST))
        {
            stack.floodFrame(frame, from);
            return;
        }

        Direction out = switchingTable.get(frame.destMac());

        // Non so a chi mandare il frame
        if (out == null)
        {
            stack.floodFrame(frame, from);
            return;
        }

        if (!out.equals(from))
        {
            stack.sendFrameOut(frame, out);
        }
    }

    public Map<MacAddress, Direction> getSwitchingTable()
    {
        return switchingTable;
    }
}