package eu.eugenioguidetti.mcnetworking.simulation.models.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 31/07/2026
 */

import org.jspecify.annotations.NonNull;

/**
 *
 * I parametri 'identifier' e 'sequenceNumber' insieme, oppure 'originalPacket' rappresentano il campo 'Rest of Header'
 *
 * @author Eugenio Guidetti
 */
public record IcmpPayload(int type, int code, int identifier, int sequenceNumber, Ipv4Packet originalPacket) implements NetworkPayload
{
    public static final int ECHO_REPLY_TYPE = 0;
    public static final int DESTINATION_UNREACHABLE_TYPE = 3;
    public static final int ECHO_REQUEST_TYPE = 8;
    public static final int TIME_EXCEEDED_TYPE = 11;

    public static final int DESTINATION_NETWORK_UNREACHABLE_CODE = 0;
    public static final int DESTINATION_HOST_UNREACHABLE_CODE = 1;

    @Override
    public @NonNull String getDisplayString()
    {
        if (type == ECHO_REQUEST_TYPE)
        {
            return String.format("ICMP Echo Request (id=%d, seq=%d)", identifier, sequenceNumber);
        }
        if (type == ECHO_REPLY_TYPE)
        {
            return String.format("ICMP Echo Reply (id=%d, seq=%d)", identifier, sequenceNumber);
        }
        if (type == TIME_EXCEEDED_TYPE)
        {
            return "ICMP Time Exceeded for " + originalPacket;
        }
        if (type == DESTINATION_UNREACHABLE_TYPE)
        {
            if (code == DESTINATION_NETWORK_UNREACHABLE_CODE)
            {
                return "ICMP Destination Network Unreachable for " + originalPacket;
            }
            else if (code == DESTINATION_HOST_UNREACHABLE_CODE)
            {
                return "ICMP Destination Host Unreachable for " + originalPacket;
            }
        }
        return "ICMP Type " + type;
    }

    @Override
    public int getSizeInBytes()
    {
        return 4 + (originalPacket != null ? originalPacket.getSizeInBytes() : 4);
    }

    public static @NonNull IcmpPayload echoRequest(int identifier, int sequenceNumber)
    {
        return new IcmpPayload(ECHO_REQUEST_TYPE, 0, identifier, sequenceNumber, null);
    }

    public static @NonNull IcmpPayload echoReply(int identifier, int sequenceNumber)
    {
        return new IcmpPayload(ECHO_REPLY_TYPE, 0, identifier, sequenceNumber, null);
    }

    public static @NonNull IcmpPayload echoReply(@NonNull IcmpPayload echoRequest)
    {
        return new IcmpPayload(ECHO_REPLY_TYPE, 0, echoRequest.identifier(), echoRequest.sequenceNumber(), null);
    }

    public static @NonNull IcmpPayload destinationNetworkUnreachable(Ipv4Packet originalPacket)
    {
        return new IcmpPayload(DESTINATION_UNREACHABLE_TYPE, DESTINATION_NETWORK_UNREACHABLE_CODE, 0, 0, originalPacket);
    }

    public static @NonNull IcmpPayload destinationHostUnreachable(Ipv4Packet originalPacket)
    {
        return new IcmpPayload(DESTINATION_UNREACHABLE_TYPE, DESTINATION_HOST_UNREACHABLE_CODE, 0, 0, originalPacket);
    }

    public static @NonNull IcmpPayload timeExceeded(Ipv4Packet originalPacket)
    {
        return new IcmpPayload(TIME_EXCEEDED_TYPE, 0, 0, 0, originalPacket);
    }
}
