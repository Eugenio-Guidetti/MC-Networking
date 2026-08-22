package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public record EthernetFrame(MacAddress sourceMac, MacAddress destMac, NetworkPayload payload) implements NetworkPayload
{
    public static final int ETHERNET_HEADER_LENGTH = 18;

    public @NonNull EthernetFrame copy()
    {
        return new EthernetFrame(sourceMac, destMac, payload);
    }

    @Override
    public @NonNull String getDisplayString()
    {
        return String.format("[ETH %s -> %s] %s", sourceMac, destMac, payload.getDisplayString());
    }

    @Override
    public int getSizeInBytes()
    {
        return ETHERNET_HEADER_LENGTH + payload.getSizeInBytes();
    }
}