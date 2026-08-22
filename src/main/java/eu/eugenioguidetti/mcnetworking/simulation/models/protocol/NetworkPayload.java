package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface NetworkPayload
{
    @NonNull String getDisplayString();

    int getSizeInBytes();
}