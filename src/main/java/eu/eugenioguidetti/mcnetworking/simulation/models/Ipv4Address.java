package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Eugenio Guidetti
 */
public class Ipv4Address
{
    private final byte[] ipv4Address;
    private final byte[] subnetMask = {};

    public static final int IPV4_BYTES = 4;
    public static final char IPV4_SEPARATOR = '.';

    public static final Ipv4Address ALL_ZEROS = new Ipv4Address(new byte[]{0, 0, 0, 0});
    public static final Ipv4Address BROADCAST = new Ipv4Address(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});

    public Ipv4Address(byte[] ipv4Address)
    {
        if (ipv4Address == null || ipv4Address.length != IPV4_BYTES)
        {
            throw new IllegalArgumentException("Array di byte IPv4 nullo o di lunghezza non valida");
        }

        // Copia difensiva per garantire l'immutabilità
        this.ipv4Address = ipv4Address.clone();
    }

    public Ipv4Address(String ipv4String)
    {
        this.ipv4Address = convertIpToBytes(ipv4String);
    }


    public boolean isBroadcast(Ipv4Address ipv4Address)
    {
        for (byte b : ipv4Address.ipv4Address)
        {
            if (b != (byte) 0xFF)
            {
                return false;
            }
        }

        return true;
    }

    private byte[] convertIpToBytes(String ip)
    {
        byte[] bytes = new byte[IPV4_BYTES];

        if (ip == null)
        {
            throw new IllegalArgumentException("Stringa IP nulla");
        }

        try
        {
            int i = 0;
            for (String ottetto : ip.split("\\" + IPV4_SEPARATOR))
            {
                byte b = (byte) (Integer.parseInt(ottetto) - 128);
                bytes[i] = b;
                i++;
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Caratteri IP non validi. IP: " + ip, e);
        }

        return bytes;
    }

    @Override
    public String toString()
    {
        StringBuilder ipv4String = new StringBuilder();

        for (int i = 0; i < IPV4_BYTES - 1; i++)
        {
            ipv4String.append(ipv4Address[i] + 128);
            ipv4String.append(IPV4_SEPARATOR);
        }

        ipv4String.append(ipv4Address[IPV4_BYTES - 1] + 128);

        return ipv4String.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Ipv4Address that = (Ipv4Address) o;
        return Objects.deepEquals(ipv4Address, that.ipv4Address);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(ipv4Address);
    }
}
