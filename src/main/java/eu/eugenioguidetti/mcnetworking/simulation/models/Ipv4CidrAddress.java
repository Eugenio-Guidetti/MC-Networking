package eu.eugenioguidetti.mcnetworking.simulation.models;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/08/2026
 */

import org.jspecify.annotations.NonNull;

/**
 *
 * Rappresenta un indirizzo IPv4 e la relativa Subnet Mask.
 *
 * @author Eugenio Guidetti
 */
public record Ipv4CidrAddress(Ipv4Address address, Ipv4SubnetMask subnetMask)
{
    public static final Ipv4CidrAddress ALL_ZEROS = new Ipv4CidrAddress(Ipv4Address.ALL_ZEROS, 0);
    public static final Ipv4CidrAddress BROADCAST = new Ipv4CidrAddress(Ipv4Address.BROADCAST, 32);
    public static final Ipv4CidrAddress LOOPBACK = new Ipv4CidrAddress(Ipv4Address.LOOPBACK, 8);

    public Ipv4CidrAddress(@NonNull Ipv4Address address, @NonNull Ipv4SubnetMask subnetMask)
    {
        if (address.equals(Ipv4Address.ALL_ZEROS) && subnetMask.lunghezzaPrefisso() != 0 && subnetMask.lunghezzaPrefisso() != 32)
        {
            throw new IllegalArgumentException("subnetMask " + subnetMask + " invalida per l'indirizzo ip " + Ipv4Address.ALL_ZEROS);
        }
        if (address.equals(Ipv4Address.BROADCAST) && subnetMask.lunghezzaPrefisso() != 32)
        {
            throw new IllegalArgumentException("subnetMask " + subnetMask + " invalida per l'indirizzo ip " + Ipv4Address.BROADCAST);
        }
        if (address.equals(Ipv4Address.LOOPBACK) && subnetMask.lunghezzaPrefisso() != 8)
        {
            throw new IllegalArgumentException("subnetMask " + subnetMask + " invalida per l'indirizzo ip " + Ipv4Address.LOOPBACK);
        }

        this.address = address;
        this.subnetMask = subnetMask;
    }

    public Ipv4CidrAddress(String cidrAddress)
    {
        if (cidrAddress == null || cidrAddress.isBlank())
        {
            throw new IllegalArgumentException("cidrAddress vuoto");
        }

        String[] parti = cidrAddress.split("/");

        if (parti.length > 2)
        {
            throw new IllegalArgumentException("cidrAddress invalido");
        }

        int lunghezzaPrefisso = Integer.parseInt(parti[1]);

        this(new Ipv4Address(parti[0]), new Ipv4SubnetMask(lunghezzaPrefisso));
    }

    public Ipv4CidrAddress(String ipString, String subnetMaskString)
    {
        this(new Ipv4Address(ipString), new Ipv4SubnetMask(subnetMaskString));
    }

    public Ipv4CidrAddress(int ipRaw, int lunghezzaPrefisso)
    {
        this(new Ipv4Address(ipRaw), new Ipv4SubnetMask(lunghezzaPrefisso));
    }

    public Ipv4CidrAddress(Ipv4Address address, int lunghezzaPrefisso)
    {
        this(address, new Ipv4SubnetMask(lunghezzaPrefisso));
    }


    public @NonNull Ipv4CidrAddress getIndirizzoDiRete()
    {
        if (this.subnetMask.lunghezzaPrefisso() >= 31)
        {
            throw new IllegalStateException("subnetMask non valida");
        }

        int rawIpRete = this.address.rawIp() & subnetMask.getSubnetMaskAsRawInt();
        return new Ipv4CidrAddress(new Ipv4Address(rawIpRete), this.subnetMask);
    }


    public boolean isIndirizzoDiRete()
    {
        if (this.subnetMask.lunghezzaPrefisso() >= 31)
        {
            return false;
        }

        return this.equals(getIndirizzoDiRete());
    }


    public @NonNull Ipv4CidrAddress getIndirizzoDiBroadcast()
    {
        if (this.subnetMask.lunghezzaPrefisso() >= 31)
        {
            throw new IllegalStateException("subnetMask non valida");
        }

        int rawIp = this.address.rawIp();
        int negatedRawSubnetMask = ~subnetMask.getSubnetMaskAsRawInt();

        int rawIpBroadcast = this.address.rawIp() | ~subnetMask.getSubnetMaskAsRawInt();
        return new Ipv4CidrAddress(new Ipv4Address(rawIpBroadcast), this.subnetMask);
    }

    public boolean isIndirizzoDiBroadcast()
    {
        if (this.subnetMask.lunghezzaPrefisso() >= 31)
        {
            return false;
        }

        return this.address.isBroadcast() || this.equals(getIndirizzoDiBroadcast());
    }

    /**
     *
     * @return {@code true} se questo indirizzo è del tipo {@code 127.x.y.z/8}, {@code false} altrimenti
     */
    public boolean isLoopback()
    {
        return address.isLoopback() && subnetMask.lunghezzaPrefisso() == 8;
    }

    /**
     *
     * @return {@code true} se la rete di questo indirizzo ip contiene {@code altroIp}, {@code false} altrimenti
     */
    public boolean contieneIp(Ipv4Address altroIp)
    {
        if (altroIp == null)
        {
            return false;
        }

        int rawNetMask = subnetMask.getSubnetMaskAsRawInt();

        int rawIpRete1 = address.rawIp() & rawNetMask;
        int rawIpRete2 = altroIp.rawIp() & rawNetMask;

        return rawIpRete1 == rawIpRete2;
    }

    /**
     *
     * @return {@code true} se i due indirizzi ip hanno lo stesso indirizzo di rete e la stessa subnet mask, {@code false} altrimenti
     */
    public static boolean stessaRete(@NonNull Ipv4CidrAddress ip1, @NonNull Ipv4CidrAddress ip2)
    {
        return ip1.getIndirizzoDiRete().equals(ip2.getIndirizzoDiRete());
    }

    /**
     *
     * @return {@code true} se le reti a cui appartengono i due indirizzi rawIp hanno almeno un indirizzo in comune, {@code false} altrimenti
     */
    public static boolean retiSovrapposte(@NonNull Ipv4CidrAddress ip1, @NonNull Ipv4CidrAddress ip2)
    {
        if (ip1.contieneIp(ip2.address()))
        {
            return true;
        }
        return ip2.contieneIp(ip1.address());
    }

    @Override
    public @NonNull String toString()
    {
        return address.getIpString() + "/" + subnetMask.lunghezzaPrefisso();
    }
}
