package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/10/2026
 */

import eu.eugenioguidetti.mcnetworking.GlobalConstants;

/**
 *
 * @author Eugenio Guidetti
 */
public interface TransportSegment extends NetworkPayload
{
    int sourcePort();

    int destPort();

    TransportProtocol protocol();

    NetworkPayload payload();

    static boolean isValidPort(int port)
    {
        return port >= GlobalConstants.WELl_KNOWN_PORTS_FIRST && port <= GlobalConstants.DYNAMIC_PORTS_LAST;
    }

    static void validatePort(int port)
    {
        if (!isValidPort(port))
        {
            throw new IllegalArgumentException("Numero di porta invalido: " + port);
        }
    }
}
