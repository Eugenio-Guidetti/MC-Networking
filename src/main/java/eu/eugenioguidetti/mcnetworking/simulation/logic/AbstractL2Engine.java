package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractL2Engine implements L2Engine
{
    protected final NetworkingBlockEntity netEntity;

    public AbstractL2Engine(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }

    protected void floodFrame(EthernetFrame frame, @Nullable String exceptNic)
    {
        for (NetworkInterface nic : netEntity.getNics().values())
        {
            if (nic.isLoopback())
            {
                continue;
            }

            if (nic.getName().equals(exceptNic))
            {
                continue;
            }

            nic.sendFrame(frame.copy());
        }
    }

    protected void floodPayload(NetworkPayload payload, MacAddress destMac)
    {
        for (NetworkInterface nic : netEntity.getNics().values())
        {
            if (nic.isLoopback())
            {
                continue;
            }

            EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), destMac, payload);
            nic.sendFrame(frame);
        }
    }

    @Override
    public void sendPayload(NetworkPayload payload, MacAddress destMac, String outName)
    {
        NetworkInterface nic = netEntity.getInterface(outName);

        if (nic == null)
        {
            return;
        }

        EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), destMac, payload);

        nic.sendFrame(frame);
    }

    @Override
    public void sendFrame(@NonNull EthernetFrame frame, String outName)
    {
        NetworkInterface nic = netEntity.getInterface(outName);

        if (nic != null)
        {
            nic.sendFrame(frame);
        }
    }
}
