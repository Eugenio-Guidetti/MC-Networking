package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class NetworkStack
{
    private final NetworkReceiver networkReceiver;

    // I moduli OSI componibili
    private L2Engine l2Engine = null;
    private L3Engine l3Engine = null;

    // Costruttore flessibile
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

    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull Direction from)
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

    public void sendFrameOut(EthernetFrame frame, Direction outFace)
    {
        if (!networkReceiver.getNics().containsKey(outFace))
        {
            return;
        }

        networkReceiver.getInterface(outFace).sendFrame(frame);
    }

    public void floodFrame(EthernetFrame frame, Direction exceptFace)
    {
        for (Map.Entry<Direction, NetworkInterface> entry : networkReceiver.getNics().entrySet())
        {
            if (!entry.getKey().equals(exceptFace))
            {
                entry.getValue().sendFrame(frame.copy());
            }
        }
    }


    public void receivePacket(Ipv4Packet packet, Direction from)
    {
        if (l3Engine != null)
        {
            l3Engine.processPacket(packet, from, this);
        }
    }

    public void sendPacket(Ipv4Packet packet)
    {
        if (l3Engine != null)
        {
            l3Engine.sendPacket(packet, this);
        }
    }


    public NetworkReceiver getNetworkReceiver()
    {
        return networkReceiver;
    }
}
