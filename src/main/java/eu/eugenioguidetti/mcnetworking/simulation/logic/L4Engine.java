package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/10/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportProtocol;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportSegment;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface L4Engine
{
    /**
     * Elabora il segmento in arrivo.
     *
     * @return {@code true} se il segmento è stato preso in carico da un job (o gestito internamente), <br>
     * {@code false} se la porta non è in ascolto (porta chiusa).
     */
    boolean processSegment(@NonNull TransportSegment segment, @NonNull Ipv4Address sourceIp);

    void sendSegment(@NonNull TransportSegment segment, @NonNull Ipv4Address destIp);

    void sendPayload(@NonNull NetworkPayload payload,
                     @NonNull Ipv4Address destIp,
                     int sourcePort,
                     int destPort,
                     @NonNull TransportProtocol protocol);


    default void handleIcmpError(@NonNull IcmpPayload icmp, @NonNull Ipv4Address reporterIp)
    {
    }
}
