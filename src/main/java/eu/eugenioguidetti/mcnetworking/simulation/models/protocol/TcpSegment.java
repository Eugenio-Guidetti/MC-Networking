package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

/**
 *
 * @author Eugenio Guidetti
 */
public record TcpSegment(int sourcePort, int destPort, NetworkPayload payload) implements NetworkPayload
{
    public static final int TCP_HEADER_LENGTH = 20;

    @Override
    public String getDisplayString()
    {
        return String.format("[TCP %d->%d] %s", sourcePort, destPort, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return TCP_HEADER_LENGTH + payload.getSizeInBytes();
    }
}