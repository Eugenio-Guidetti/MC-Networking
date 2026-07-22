package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class NetworkStack
{
    private final NetworkReceiver networkReceiver;

    private L2Engine l2Engine = null;
    private L3Engine l3Engine = null;

    public NetworkStack(NetworkReceiver networkReceiver)
    {
        this.networkReceiver = networkReceiver;
    }

    public void setL2Engine(L2Engine processor)
    {
        this.l2Engine = processor;
    }

    public void setL3Engine(L3Engine processor)
    {
        this.l3Engine = processor;
    }

    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull String from)
    {
        // La logica base: passa il frame al componente di livello più basso configurato
        if (l2Engine != null)
        {
            l2Engine.processFrame(frame, from, this);

            return;
        }
        if (l3Engine != null)
        {
            if (frame.payload() instanceof Ipv4Packet packet)
            {
                l3Engine.processPacket(packet, from, this);
            }

            return;
        }

        // Nessun engine registrata (es: hub)
        floodFrame(frame, from);
    }

    public void sendFrameOut(EthernetFrame frame, String outName)
    {
        if (!networkReceiver.getNics().containsKey(outName))
        {
            return;
        }

        networkReceiver.getInterface(outName).sendFrame(frame);
    }

    public void floodFrame(EthernetFrame frame, String exceptNic)
    {
        for (Map.Entry<String, NetworkInterface> entry : networkReceiver.getNics().entrySet())
        {
            if (entry.getValue().isLoopback())
            {
                continue;
            }

            if (!entry.getKey().equals(exceptNic))
            {
                entry.getValue().sendFrame(frame.copy());
            }
        }
    }


    public void receivePacket(Ipv4Packet packet, String from)
    {
        if (l3Engine != null)
        {
            l3Engine.processPacket(packet, from, this);
        }
    }

    public void sendPacket(Ipv4Address destIp, NetworkPayload payload)
    {
        if (l3Engine != null)
        {
            l3Engine.sendPacket(destIp, payload, this);
        }
    }


    public NetworkReceiver getNetworkReceiver()
    {
        return networkReceiver;
    }
}
