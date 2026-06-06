package eu.eugenioguidetti.mcnetworking.simulation.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

/**
 *
 * @author Eugenio Guidetti
 */
public record ApplicationPayload(String message) implements NetworkPayload
{
    @Override
    public String getDisplayString()
    {
        return message;
    }

    @Override
    public int getSizeInBytes()
    {
        return message.getBytes().length;
    }
}