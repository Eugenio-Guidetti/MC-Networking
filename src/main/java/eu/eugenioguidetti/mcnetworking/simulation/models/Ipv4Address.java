package eu.eugenioguidetti.mcnetworking.simulation.models;

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
public record Ipv4Address(int rawIp)
{
    public static final Ipv4Address ALL_ZEROS = new Ipv4Address(0x00000000);
    public static final Ipv4Address BROADCAST = new Ipv4Address(0xFFFFFFFF);
    public static final Ipv4Address LOOPBACK = new Ipv4Address(0x7F000001);


    /**
     *
     * @param ipString la stringa in notazione decimale puntata rappresentante l'indirizzo IPv4 da creare (es. {@code 192.168.1.0})
     */
    public Ipv4Address(String ipString)
    {
        this(Ipv4.parseIp(ipString));
    }


    public boolean isLoopback()
    {
        return ((this.rawIp >> 24) & 0x000000FF) == 127;
    }

    public boolean isAllZeros()
    {
        return ALL_ZEROS.equals(this);
    }

    public boolean isBroadcast()
    {
        return BROADCAST.equals(this);
    }


    /**
     *
     * @return La rappresentazione in notazione decimale puntata di questo indirizzo ip
     */
    public @NonNull String getIpString()
    {
        return Ipv4.formatRawIpToString(this.rawIp);
    }

    @Override
    public @NonNull String toString()
    {
        return getIpString();
    }
}