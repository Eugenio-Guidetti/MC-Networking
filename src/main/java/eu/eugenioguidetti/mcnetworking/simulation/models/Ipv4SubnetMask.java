package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/08/2026
 */

import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public record Ipv4SubnetMask(int lunghezzaPrefisso)
{
    public Ipv4SubnetMask(int lunghezzaPrefisso)
    {
        if (lunghezzaPrefisso < 0 || lunghezzaPrefisso > 32)
        {
            throw new IllegalArgumentException("lunghezzaPrefisso invalida");
        }

        this.lunghezzaPrefisso = lunghezzaPrefisso;
    }

    /**
     *
     * @param subnetMaskString la stringa in notazione decimale puntata rappresentante la subnet mask IPv4 da creare (es. {@code 255.255.255.0})
     */
    public Ipv4SubnetMask(String subnetMaskString)
    {
        if (subnetMaskString == null || subnetMaskString.isBlank())
        {
            throw new IllegalArgumentException("subnetMaskString vuota");
        }

        this(getLunghezzaPrefisso(new Ipv4Address(subnetMaskString).rawIp()));
    }

    public Ipv4SubnetMask(Ipv4Address subnetMask)
    {
        if (subnetMask == null)
        {
            throw new IllegalArgumentException("subnetMask null");
        }

        this(getLunghezzaPrefisso(subnetMask.rawIp()));
    }

    public int getSubnetMaskAsRawInt()
    {
        return getSubnetMaskAsRawInt(lunghezzaPrefisso);
    }

    public static int getSubnetMaskAsRawInt(int lunghezzaPrefisso)
    {
        if (lunghezzaPrefisso == 0)
        {
            return 0;
        }

        return 0xFFFFFFFF << (32 - lunghezzaPrefisso);
    }

    public static int getLunghezzaPrefisso(int rawSubnetMask)
    {
        int bitCount = Integer.bitCount(rawSubnetMask);

        if (getSubnetMaskAsRawInt(bitCount) != rawSubnetMask)
        {
            throw new IllegalArgumentException("rawSubnetMask invalida");
        }

        return bitCount;
    }

    public @NonNull String getSubnetMaskString()
    {
        return Ipv4.formatRawIpToString(getSubnetMaskAsRawInt());
    }

    @Override
    public @NonNull String toString()
    {
        return getSubnetMaskString();
    }

}
