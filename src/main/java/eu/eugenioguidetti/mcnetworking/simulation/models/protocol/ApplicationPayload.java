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
public record ApplicationPayload(String message) implements NetworkPayload
{
    @Override
    public @NonNull String getDisplayString()
    {
        return message;
    }

    @Override
    public int getSizeInBytes()
    {
        return message.getBytes().length;
    }
}