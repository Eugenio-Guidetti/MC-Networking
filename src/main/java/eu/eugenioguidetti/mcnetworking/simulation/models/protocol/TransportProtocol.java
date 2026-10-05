package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/10/2026
 */

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.TCP_PROTOCOL_NUMBER;
import static eu.eugenioguidetti.mcnetworking.GlobalConstants.UDP_PROTOCOL_NUMBER;

/**
 *
 * @author Eugenio Guidetti
 */
public enum TransportProtocol
{
    UDP(UDP_PROTOCOL_NUMBER),
    TCP(TCP_PROTOCOL_NUMBER);

    private final int protocolNumber;

    TransportProtocol(int protocolNumber)
    {
        this.protocolNumber = protocolNumber;
    }

    public int getProtocolNumber()
    {
        return protocolNumber;
    }
}
