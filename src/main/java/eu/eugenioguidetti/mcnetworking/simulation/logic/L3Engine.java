package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.Direction;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L3Engine
{
    void processPacket(Ipv4Packet packet, Direction from, NetworkStack stack);

    void sendPacket(Ipv4Packet packet, NetworkStack stack);
}
