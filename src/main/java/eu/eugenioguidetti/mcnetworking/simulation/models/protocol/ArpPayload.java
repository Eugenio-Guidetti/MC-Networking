package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 06/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;

/**
 *
 * @author Eugenio Guidetti
 */
public record ArpPayload(MacAddress senderMac, Ipv4Address senderIp, MacAddress targetMac, Ipv4Address targetIp,
                         int operation) implements NetworkPayload
{
    public static final int OPERATION_ARP_REQUEST = 1;
    public static final int OPERATION_ARP_REPLY = 2;

    @Override
    public String getDisplayString()
    {
        String format;

        switch (operation)
        {
            case OPERATION_ARP_REQUEST:
                return String.format("%s ha chiesto a %s il suo indirizzo mac.", senderIp, targetIp);

            case OPERATION_ARP_REPLY:
                return String.format("%s dice a %s che il suo indirizzo mac è: %s", senderIp, targetIp, targetMac);

            default:
                return String.format("%s->%s,%s->%s. Operation: %d", senderMac, senderIp, targetMac, targetIp, operation);
        }
    }

    @Override
    public int getSizeInBytes()
    {
        return 4 + 6 + 4 + 6 + 4 + 4;
    }
}
