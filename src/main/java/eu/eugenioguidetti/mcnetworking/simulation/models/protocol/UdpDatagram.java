package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 *
 * @author Eugenio Guidetti
 */
public record UdpDatagram(int sourcePort, int destPort, NetworkPayload payload) implements TransportSegment
{
    public static final int UDP_HEADER_LENGTH = 8;

    public UdpDatagram
    {
        TransportSegment.validatePort(sourcePort);
        TransportSegment.validatePort(destPort);
        Objects.requireNonNull(payload, "Payload null");
    }

    @Override
    public @NonNull String getDisplayString()
    {
        return String.format("[UDP %d->%d] %s", sourcePort, destPort, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return UDP_HEADER_LENGTH + payload.getSizeInBytes();
    }

    @Override
    public TransportProtocol protocol()
    {
        return TransportProtocol.UDP;
    }
}