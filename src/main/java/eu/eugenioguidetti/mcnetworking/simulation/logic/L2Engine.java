package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L2Engine
{
    void processFrame(EthernetFrame frame, String from, NetworkStack stack);
}