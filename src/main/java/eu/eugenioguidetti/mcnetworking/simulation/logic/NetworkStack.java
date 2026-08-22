package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import org.jetbrains.annotations.NotNull;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;

/**
 *
 * @author Eugenio Guidetti
 */
public class NetworkStack
{
    private L2Engine l2Engine = null;
    private L3Engine l3Engine = null;

    private final NetworkingBlockEntity netEntity;

    public NetworkStack(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }


    public NetworkingBlockEntity getNetEntity()
    {
        return netEntity;
    }


    public void setL2Engine(L2Engine processor)
    {
        if (this.l2Engine != null)
        {
            throw new IllegalArgumentException("l2Engine già inizializzata");
        }

        this.l2Engine = processor;
    }

    public void setL3Engine(L3Engine processor)
    {
        if (this.l3Engine != null)
        {
            throw new IllegalArgumentException("l3Engine già inizializzata");
        }

        this.l3Engine = processor;
    }


    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull String from)
    {
        // La logica base: passa il frame al componente di livello più basso configurato
        if (l2Engine != null)
        {
            l2Engine.processFrame(frame, from);

            return;
        }
        if (frame.payload() instanceof Ipv4Packet packet)
        {
            receivePacket(packet, from);
        }
    }

    public void receivePacket(Ipv4Packet packet, String from)
    {
        if (l3Engine != null)
        {
            l3Engine.processPacket(packet, from);
        }
    }


    public void sendL2Payload(NetworkPayload payload, MacAddress destMac, String outName)
    {
        if (l2Engine == null)
        {
            return;
        }

        l2Engine.sendPayload(payload, destMac, outName);
    }

    public void sendFrame(EthernetFrame frame, String outName)
    {
        if (l2Engine == null)
        {
            return;
        }

        l2Engine.sendFrame(frame, outName);
    }

    public void sendL3Payload(NetworkPayload payload, Ipv4Address destIp)
    {
        sendL3Payload(payload, destIp, DEFAULT_TTL);
    }

    public void sendL3Payload(NetworkPayload payload, Ipv4Address destIp, int ttl)
    {
        if (l3Engine == null)
        {
            return;
        }

        l3Engine.sendPayload(payload, destIp, ttl);
    }

    public void sendPacket(Ipv4Packet packet)
    {
        if (l3Engine == null)
        {
            return;
        }

        l3Engine.sendPacket(packet);
    }

    public void sendPacket(Ipv4Packet packet, AbstractL3Engine.OutPacketData outPacketData)
    {
        if (l3Engine == null)
        {
            return;
        }

        l3Engine.sendPacket(packet, outPacketData);
    }
}
