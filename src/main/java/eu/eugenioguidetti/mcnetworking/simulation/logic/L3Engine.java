package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L3Engine
{
    void processPacket(@NonNull Ipv4Packet packet, @NonNull String from);

    void sendPayload(NetworkPayload payload, Ipv4Address destIp, int ttl);

    void sendPacket(@NonNull Ipv4Packet packet);

    void sendPacket(@NonNull Ipv4Packet packet, AbstractL3Engine.OutPacketData outPacketData);
}
