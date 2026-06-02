package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import net.minecraft.core.Direction;

/**
 *
 * @author Eugenio Guidetti
 */
public interface NetworkReceiver
{
    /**
     * @param packet Il pacchetto in arrivo.
     * @param from   La direzione DA CUI è arrivato (utile per non rimandarlo indietro).
     */
    void receivePacket(SimPacket packet, Direction from);

    NetworkInterface getInterface(Direction clickedFace);

    void disconnectAll();

    void sync();
}