package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class HubL2Engine extends AbstractL2Engine
{
    public HubL2Engine(NetworkingBlockEntity netEntity)
    {
        super(netEntity);
    }

    @Override
    public void processFrame(@NonNull EthernetFrame frame, @NonNull String from)
    {
        floodFrame(frame, from);
    }
}
