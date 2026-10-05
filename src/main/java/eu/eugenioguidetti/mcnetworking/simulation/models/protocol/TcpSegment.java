package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 *
 * @author Eugenio Guidetti
 */
public record TcpSegment(int sourcePort, int destPort, NetworkPayload payload) implements TransportSegment
{
    public static final int TCP_HEADER_LENGTH = 20;

    public TcpSegment
    {
        TransportSegment.validatePort(sourcePort);
        TransportSegment.validatePort(destPort);
        Objects.requireNonNull(payload, "Payload null");
    }

    @Override
    public @NonNull String getDisplayString()
    {
        return String.format("[TCP %d->%d] %s", sourcePort, destPort, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return TCP_HEADER_LENGTH + payload.getSizeInBytes();
    }

    @Override
    public TransportProtocol protocol()
    {
        return TransportProtocol.TCP;
    }
}