package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;

/**
 *
 * @author Eugenio Guidetti
 */
public record Ipv4Packet(Ipv4Address sourceIp, Ipv4Address destIp, NetworkPayload payload) implements NetworkPayload
{
    public static final int IPV4_HEADER_LENGTH = 20;

    @Override
    public String getDisplayString()
    {
        return String.format("[IP %s->%s] %s", sourceIp, destIp, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return IPV4_HEADER_LENGTH + payload.getSizeInBytes();
    }
}