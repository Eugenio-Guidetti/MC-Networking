package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L3Engine
{
    void processPacket(Ipv4Packet packet, String from, NetworkStack stack);

    void sendPacket(Ipv4Address destIp, NetworkPayload payload, NetworkStack stack);
}
