package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;

/**
 *
 * @author Eugenio Guidetti
 */
public interface Job
{
    /**
     * @return true se il job è completato
     */
    boolean tick(NetworkingBlockEntity entity);
}
