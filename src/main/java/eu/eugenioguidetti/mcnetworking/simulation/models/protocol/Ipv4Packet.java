package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public record Ipv4Packet(Ipv4Address sourceIp, Ipv4Address destIp, int ttl, NetworkPayload payload) implements NetworkPayload
{
    public static final int IPV4_HEADER_LENGTH = 20;

    @Override
    public @NonNull String getDisplayString()
    {
        return String.format("[IP %s->%s, TTL: %d] %s", sourceIp, destIp, ttl, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return IPV4_HEADER_LENGTH + payload.getSizeInBytes();
    }


    public @NonNull Ipv4Packet decreaseTtl()
    {
        return new Ipv4Packet(this.sourceIp, this.destIp, this.ttl - 1, this.payload);
    }
}