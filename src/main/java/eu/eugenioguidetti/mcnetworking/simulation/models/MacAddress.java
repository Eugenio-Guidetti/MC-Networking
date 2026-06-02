package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/06/2026
 */

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 *
 * @author Eugenio Guidetti
 */
public class MacAddress
{
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    private final byte[] macAddress;

    public static final int MAC_BYTES = 6;
    public static final String MAC_SEPARATOR = ":";

    public static final MacAddress ALL_ZEROS = new MacAddress(new byte[]{0, 0, 0, 0, 0, 0});

    public MacAddress(byte[] macAddress)
    {
        if (macAddress == null || macAddress.length != MAC_BYTES)
        {
            throw new IllegalArgumentException("Array di byte MAC nullo o di lunghezza non valida");
        }

        // Copia difensiva per garantire l'immutabilità
        this.macAddress = macAddress.clone();
    }

    public MacAddress(String macString)
    {
        this.macAddress = convertMacToBytes(macString);
    }

    public static MacAddress generateRandomMac()
    {
        byte[] bytes = new byte[MAC_BYTES];
        RANDOM.nextBytes(bytes);

        // Imposta il bit locally administered (bit 1) e forza il bit unicast a 0 (bit 0)
        bytes[0] = (byte) ((bytes[0] | 0b00000010) & 0b11111110);

        return new MacAddress(bytes);
    }

    private byte[] convertMacToBytes(String mac)
    {
        if (mac == null)
        {
            throw new IllegalArgumentException("Stringa MAC nulla");
        }

        // Rimuove separatori comuni
        String cleanMac = mac.replace(MAC_SEPARATOR, "").replace("-", "");

        if (cleanMac.length() != MAC_BYTES * 2)
        {
            throw new IllegalArgumentException("Lunghezza MAC invalida. MAC: " + mac);
        }

        try
        {
            return HEX_FORMAT.parseHex(cleanMac);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Caratteri MAC non validi. MAC: " + mac, e);
        }
    }

    @Override
    public String toString()
    {
        return HexFormat.ofDelimiter(MAC_SEPARATOR).withUpperCase().formatHex(macAddress);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        MacAddress that = (MacAddress) o;
        return Objects.deepEquals(macAddress, that.macAddress);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(macAddress);
    }
}
