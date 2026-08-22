package eu.eugenioguidetti.mcnetworking.simulation.logic.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 11/08/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.TraceRouteJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
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


    public void handleIcmp(@NonNull IcmpPayload icmp, @NonNull Ipv4Packet originalPacket, String from)
    {
        if (icmp.type() == IcmpPayload.ECHO_REQUEST_TYPE)
        {
            Ipv4Address nicIp = l3NetEntity.getInterface(from).getIpAddress().address();

            IcmpPayload echoReply = IcmpPayload.echoReply(icmp);
            Ipv4Packet replyPacket = new Ipv4Packet(nicIp, originalPacket.sourceIp(), DEFAULT_TTL, echoReply);
            l3NetEntity.getStack().sendPacket(replyPacket);

            return;
        }

        Optional<IcmpPayload> originalIcmp = Optional.empty();
        if (icmp.originalPacket() != null && icmp.originalPacket().payload() instanceof IcmpPayload)
        {
            originalIcmp = Optional.of((IcmpPayload) icmp.originalPacket().payload());
        }

        int jobId = originalIcmp.orElse(icmp).identifier();

        Optional<Job> foundJob = l3NetEntity.getJob(jobId);

        if (foundJob.isPresent())
        {
            if (foundJob.get() instanceof PingJob pingJob)
            {
                pingJob.handleIcmpReply(originalPacket);
            }
            if (foundJob.get() instanceof TraceRouteJob traceRouteJob)
            {
                traceRouteJob.handleIcmpReply(originalPacket);
            }

            return;
        }
        MCNetworking.LOGGER.info("Ricevuto: {} da: {} TTL: {}", icmp.getDisplayString(), originalPacket.sourceIp(), originalPacket.ttl());
    }
}
