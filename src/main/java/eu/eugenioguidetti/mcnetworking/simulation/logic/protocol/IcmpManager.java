package eu.eugenioguidetti.mcnetworking.simulation.logic.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 11/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.L4Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.TraceRouteJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportSegment;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;

/**
 *
 * @author Eugenio Guidetti
 */
public class IcmpManager
{
    private final AbstractL3NetworkingBlockEntity l3NetEntity;

    public IcmpManager(AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        this.l3NetEntity = l3NetEntity;
    }


    public void handleIcmp(@NonNull IcmpPayload incomingIcmp, @NonNull Ipv4Packet packet, String from)
    {
        // ECHO REQUEST
        if (incomingIcmp.type() == IcmpPayload.ECHO_REQUEST_TYPE)
        {
            IcmpPayload echoReply = IcmpPayload.echoReply(incomingIcmp);

            Ipv4Address nicIp = l3NetEntity.getInterface(from).getIpAddress().address();
            Ipv4Packet replyPacket = new Ipv4Packet(nicIp, packet.sourceIp(), DEFAULT_TTL, echoReply);
            l3NetEntity.getStack().sendPacket(replyPacket);

            return;
        }

        // ECHO REPLY
        if (incomingIcmp.type() == IcmpPayload.ECHO_REPLY_TYPE)
        {
            // Nelle Echo Reply l'identifier coincide con il jobId del processo che ha inviato la richiesta
            handleIcmpJobReply(incomingIcmp, packet);
            return;
        }

        // ERRORE ICMP
        if (incomingIcmp.originalPacket() != null)
        {
            Ipv4Packet original = incomingIcmp.originalPacket();

            if (original.payload() instanceof TransportSegment originalSegment)
            {
                // Errore relativo a un job L4
                L4Engine l4Engine = l3NetEntity.getStack().getL4Engine();
                if (l4Engine != null)
                {
                    l4Engine.handleIcmpError(incomingIcmp, packet.sourceIp());
                }
                return;
            }

            if (original.payload() instanceof IcmpPayload originalIcmp)
            {
                // Errore relativo a un job L3 (ping o traceroute)

                // Qui il jobId nell'identifier è contenuto nel messaggio originale
                int jobId = originalIcmp.identifier();

                handleIcmpJobError(jobId, incomingIcmp, originalIcmp, packet);
            }
        }
    }

    private void handleIcmpJobReply(@NonNull IcmpPayload incomingIcmp, @NonNull Ipv4Packet packet)
    {
        int jobId = incomingIcmp.identifier();

        Optional<Job> foundJob = l3NetEntity.getJob(jobId);

        if (foundJob.isPresent())
        {
            if (foundJob.get() instanceof PingJob pingJob)
            {
                pingJob.handleIcmpReply(incomingIcmp, packet.sourceIp(), packet.ttl());
            }
            else if (foundJob.get() instanceof TraceRouteJob traceRouteJob)
            {
                traceRouteJob.handleIcmpReply(incomingIcmp, packet.sourceIp());
            }
        }
    }

    private void handleIcmpJobError(int jobId,
                                    @NonNull IcmpPayload incomingIcmp,
                                    @NonNull IcmpPayload originalIcmp,
                                    @NonNull Ipv4Packet packet)
    {
        Optional<Job> foundJob = l3NetEntity.getJob(jobId);

        if (foundJob.isPresent())
        {
            if (foundJob.get() instanceof PingJob pingJob)
            {
                pingJob.handleIcmpError(originalIcmp.sequenceNumber(), incomingIcmp);
            }
            else if (foundJob.get() instanceof TraceRouteJob traceRouteJob)
            {
                traceRouteJob.handleIcmpError(incomingIcmp, originalIcmp, packet.sourceIp());
            }
        }
    }
}
