package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L2Engine
{
    void processFrame(@NonNull EthernetFrame frame, @NonNull String from);

    void sendPayload(NetworkPayload payload, MacAddress destMac, String outName);

    void sendFrame(@NonNull EthernetFrame frame, String outName);
}