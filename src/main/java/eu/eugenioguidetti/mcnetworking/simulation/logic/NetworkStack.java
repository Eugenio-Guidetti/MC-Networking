package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;

/**
 *
 * @author Eugenio Guidetti
 */
public class NetworkStack
{
    private L2Engine l2Engine = null;
    private L3Engine l3Engine = null;
    private L4Engine l4Engine = null;

    private final NetworkingBlockEntity netEntity;

    public NetworkStack(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }


    public NetworkingBlockEntity getNetEntity()
    {
        return netEntity;
    }

    @Nullable
    public L2Engine getL2Engine()
    {
        return l2Engine;
    }

    @Nullable
    public L3Engine getL3Engine()
    {
        return l3Engine;
    }

    @Nullable
    public L4Engine getL4Engine()
    {
        return l4Engine;
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

    public void setL4Engine(L4Engine processor)
    {
        if (this.l4Engine != null)
        {
            throw new IllegalArgumentException("l4Engine già inizializzata");
        }

        this.l4Engine = processor;
    }


    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull String from)
    {
        if (l2Engine != null)
        {
            l2Engine.processFrame(frame, from);
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
        this.sendL3Payload(payload, destIp, DEFAULT_TTL);
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


    public void sendL4Payload(NetworkPayload payload, Ipv4Address destIp, int sourcePort, int destPort, TransportProtocol protocol)
    {
        if (l4Engine == null)
        {
            return;
        }

        l4Engine.sendPayload(payload, destIp, sourcePort, destPort, protocol);
    }

    public void sendSegment(TransportSegment segment, Ipv4Address destIp)
    {
        if (l4Engine == null)
        {
            return;
        }

        l4Engine.sendSegment(segment, destIp);
    }
}
