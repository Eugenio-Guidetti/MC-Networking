package eu.eugenioguidetti.mcnetworking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/07/2026
 */

/**
 *
 * @author Eugenio Guidetti
 */
public class GlobalConstants
{
    public static final String LOOPBACK_NAME = "lo";


    public static final float CABLES_TENSION = 0.02f;
    public static final int CABLES_MAX_LENGTH_SQUARED = 22 * 22;


    public static final int MAX_JOBS = 65535;


    public static final int DEFAULT_ETH_TYPE = 0x0000;
    public static final int IPV4_ETH_TYPE = 0x0800;
    public static final int ARP_ETH_TYPE = 0x0806;

    public static final int DEFAULT_PROTOCOL_NUMBER = 0;
    public static final int ICMP_PROTOCOL_NUMBER = 1;
    public static final int TCP_PROTOCOL_NUMBER = 6;
    public static final int UDP_PROTOCOL_NUMBER = 17;

    public static final int DEFAULT_TTL = 32;


    public static final int WELl_KNOWN_PORTS_FIRST = 1;
    public static final int WELl_KNOWN_PORTS_LAST = 1023;
    public static final int REGISTERED_PORTS_FIRST = 1024;
    public static final int REGISTERED_PORTS_LAST = 49151;
    public static final int DYNAMIC_PORTS_FIRST = 49152;
    public static final int DYNAMIC_PORTS_LAST = 65535;

    public static final int DNS_PORT = 53;
    public static final int HTTP_PORT = 80;
}
