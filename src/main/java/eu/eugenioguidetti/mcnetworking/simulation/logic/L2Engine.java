package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import net.minecraft.core.Direction;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L2Engine
{
    void processFrame(EthernetFrame frame, Direction from, NetworkStack stack);
}