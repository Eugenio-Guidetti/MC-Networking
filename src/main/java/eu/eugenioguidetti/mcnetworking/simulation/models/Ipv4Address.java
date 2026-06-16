package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/06/2026
 */

import java.util.Objects;

/**
 *
 * Rappresenta un indirizzo IPv4 e la relativa Subnet Mask.
 *
 * @author Eugenio Guidetti
 */
public class Ipv4Address
{
    public static final Ipv4Address ALL_ZEROS = new Ipv4Address(0x00000000, 0);
    public static final Ipv4Address BROADCAST = new Ipv4Address(0xFFFFFFFF, 32);
    private final int ip;
    private final int lunghezzaPrefisso;

    public Ipv4Address(int ip, int lunghezzaPrefisso)
    {
        this.ip = ip;

        if (lunghezzaPrefisso < 0 || lunghezzaPrefisso > 32)
        {
            throw new IllegalArgumentException("lunghezzaPrefisso invalida");
        }

        this.lunghezzaPrefisso = lunghezzaPrefisso;
    }

    public Ipv4Address(String cidrAddress)
    {
        if (cidrAddress == null || cidrAddress.isEmpty())
        {
            throw new IllegalArgumentException("cidrAddress vuoto");
        }

        String[] parti = cidrAddress.split("/");

        if (parti.length > 2)
        {
            throw new IllegalArgumentException("cidrAddress invalido");
        }

        this.ip = parseIp(parti[0]);

        int lunghezzaPrefisso;

        if (parti.length == 2)
        {
            lunghezzaPrefisso = Integer.parseInt(parti[1]);
            if (lunghezzaPrefisso < 0 || lunghezzaPrefisso > 32)
            {
                throw new IllegalArgumentException("lunghezzaPrefisso invalida");
            }
        }
        else
        {
            lunghezzaPrefisso = 32; // Host singolo
        }

        this.lunghezzaPrefisso = lunghezzaPrefisso;
    }

    public Ipv4Address(String ip, String subnetMask)
    {
        if (ip == null || ip.isEmpty())
        {
            throw new IllegalArgumentException("ip vuoto");
        }
        if (subnetMask == null || subnetMask.isEmpty())
        {
            throw new IllegalArgumentException("subnetMask vuota");
        }

        this.ip = parseIp(ip);
        this.lunghezzaPrefisso = getLunghezzaPrefisso(parseIp(subnetMask));
    }

    public static int parseIp(String ipString)
    {
        int ip = 0;

        if (ipString == null || ipString.isEmpty())
        {
            throw new IllegalArgumentException("ip vuoto");
        }

        String[] ottetti = ipString.split("\\.");

        if (ottetti.length != 4)
        {
            throw new IllegalArgumentException("Numero di ottetti errato");
        }

        for (int i = 0; i < 4; i++)
        {
            int ottetto = Integer.parseInt(ottetti[i]);

            if (ottetto < 0 || ottetto > 255)
            {
                throw new IllegalArgumentException("Valore ottetto errato");
            }

            ip <<= 8;
            ip |= ottetto;
        }

        return ip;
    }

    public static String formatIpToString(int ip)
    {
        return String.format("%d.%d.%d.%d", (ip >> 24) & 0x000000FF, (ip >> 16) & 0x000000FF, (ip >> 8) & 0x000000FF, ip & 0x000000FF);
    }

    public static int getSubnetMask(int lunghezzaPrefisso)
    {
        if (lunghezzaPrefisso == 0)
        {
            return 0;
        }

        return 0xFFFFFFFF << (32 - lunghezzaPrefisso);
    }

    public static int getLunghezzaPrefisso(int subnetMask)
    {
        int bitCount = Integer.bitCount(subnetMask);

        if (getSubnetMask(bitCount) != subnetMask)
        {
            throw new IllegalArgumentException("subnetMask invalida");
        }

        return bitCount;
    }

    public int getIp()
    {
        return this.ip;
    }

    public String getIpString()
    {
        return formatIpToString(this.ip);
    }

    public int getLunghezzaPrefisso()
    {
        return this.lunghezzaPrefisso;
    }

    public String getSubnetMaskString()
    {
        return formatIpToString(getSubnetMask(this.lunghezzaPrefisso));
    }

    public Ipv4Address getIndirizzoDiRete()
    {
        int ipRete = this.ip & getSubnetMask(this.lunghezzaPrefisso);
        return new Ipv4Address(ipRete, this.lunghezzaPrefisso);
    }

    public boolean isIndirizzoDiRete()
    {
        if (this.lunghezzaPrefisso >= 31)
        {
            return false;
        }

        return this.ip == getIndirizzoDiRete().ip;
    }

    public Ipv4Address getIndirizzoDiBroadcast()
    {
        return this.getIndirizzoDiBroadcast(this.lunghezzaPrefisso);
    }

    public Ipv4Address getIndirizzoDiBroadcast(int lunghezzaPrefisso)
    {
        int ipBroadcast = this.ip | ~getSubnetMask(lunghezzaPrefisso);
        return new Ipv4Address(ipBroadcast, lunghezzaPrefisso);
    }

    public boolean isIndirizzoDiBroadcast()
    {
        return this.isIndirizzoDiBroadcast(this.lunghezzaPrefisso);
    }

    public boolean isIndirizzoDiBroadcast(int lunghezzaPrefisso)
    {
        if (this.ip == 0xFFFFFFFF)
        {
            return true;
        }
        if (lunghezzaPrefisso >= 31)
        {
            return false;
        }

        return this.ip == getIndirizzoDiBroadcast(lunghezzaPrefisso).ip;
    }

    public boolean isIndirizzoDiLoopback()
    {
        return ((this.ip >> 24) & 0x000000FF) == 127;
    }

    public boolean stessaRete(Ipv4Address altroIp)
    {
        if (altroIp == null)
        {
            return false;
        }

        int ipRete1 = this.getIndirizzoDiRete().ip;
        int ipRete2 = new Ipv4Address(altroIp.ip, this.lunghezzaPrefisso).getIndirizzoDiRete().ip;

        return ipRete1 == ipRete2;
    }

    @Override
    public String toString()
    {
        return getIpString() + "/" + this.lunghezzaPrefisso;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Ipv4Address that = (Ipv4Address) o;
        return ip == that.ip;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(ip);
    }
}