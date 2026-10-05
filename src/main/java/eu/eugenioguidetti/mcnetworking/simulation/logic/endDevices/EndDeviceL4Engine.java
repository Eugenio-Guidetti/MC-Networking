package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/10/2026
 */

import eu.eugenioguidetti.mcnetworking.GlobalConstants;
import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.L4Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4.L4Job;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.*;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL4Engine implements L4Engine
{
    public record PortKey(int port, TransportProtocol protocol)
    {
    }

    private final HashMap<PortKey, L4Job> boundPorts = new HashMap<>();

    private int nextDynamicPort = GlobalConstants.DYNAMIC_PORTS_FIRST;

    private final HostBlockEntity l4netEntity;

    public EndDeviceL4Engine(HostBlockEntity l4netEntity)
    {
        this.l4netEntity = l4netEntity;
    }


    public int allocateDynamicPort(TransportProtocol protocol, L4Job job)
    {
        int tentativi = GlobalConstants.DYNAMIC_PORTS_LAST - GlobalConstants.DYNAMIC_PORTS_FIRST + 1;

        for (int i = 0; i < tentativi; i++)
        {
            int candidate = nextDynamicPort;

            nextDynamicPort++;
            if (nextDynamicPort > GlobalConstants.DYNAMIC_PORTS_LAST)
            {
                nextDynamicPort = GlobalConstants.DYNAMIC_PORTS_FIRST;
            }

            PortKey key = new PortKey(candidate, protocol);
            if (!boundPorts.containsKey(key))
            {
                boundPorts.put(key, job);
                return candidate;
            }
        }

        return -1;
    }

    public boolean bindPort(int port, TransportProtocol protocol, L4Job job)
    {
        if (!TransportSegment.isValidPort(port))
        {
            return false;
        }

        PortKey key = new PortKey(port, protocol);

        if (boundPorts.containsKey(key))
        {
            return false;
        }

        boundPorts.put(key, job);

        return true;
    }

    public void unbindPort(int port, TransportProtocol protocol)
    {
        boundPorts.remove(new PortKey(port, protocol));
    }

    public void unbindJobPorts(L4Job job)
    {
        boundPorts.values().removeIf(registeredJob -> registeredJob.equals(job));
    }


    @Override
    public boolean processSegment(@NonNull TransportSegment segment, @NonNull Ipv4Address sourceIp)
    {
        PortKey key = new PortKey(segment.destPort(), segment.protocol());
        L4Job targetJob = boundPorts.get(key);

        if (targetJob == null)
        {
            return false;
        }

        // Decapsulamento e consegna diretta al job responsabile
        targetJob.onSegmentReceived(segment, sourceIp);

        return true;
    }

    @Override
    public void sendSegment(@NonNull TransportSegment segment, @NonNull Ipv4Address destIp)
    {
        l4netEntity.getStack().sendL3Payload(segment, destIp);
    }

    @Override
    public void sendPayload(@NonNull NetworkPayload payload,
                            @NonNull Ipv4Address destIp,
                            int sourcePort,
                            int destPort,
                            @NonNull TransportProtocol protocol)
    {
        TransportSegment segment;

        switch (protocol)
        {
            case TCP -> segment = new TcpSegment(sourcePort, destPort, payload);
            case UDP -> segment = new UdpDatagram(sourcePort, destPort, payload);
            default -> throw new IllegalArgumentException("protocol non valido: " + protocol);
        }

        sendSegment(segment, destIp);
    }

    @Override
    public void handleIcmpError(@NonNull IcmpPayload icmp, @NonNull Ipv4Address reporterIp)
    {
        MCNetworking.LOGGER.info("Errore ICMP L4: ICMP: {}, IP: {}", icmp.getDisplayString(), reporterIp);
    }
}
