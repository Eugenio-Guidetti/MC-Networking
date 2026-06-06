package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface NetworkReceiver
{
    /**
     * @param frame Il frame in arrivo.
     * @param from  L'interfaccia a cui è stato inviato il frame
     */
    void receiveFrame(@NotNull EthernetFrame frame, @NotNull Direction from);

    NetworkInterface getInterface(Direction clickedFace);

    void disconnectAll();

    void sync();
}