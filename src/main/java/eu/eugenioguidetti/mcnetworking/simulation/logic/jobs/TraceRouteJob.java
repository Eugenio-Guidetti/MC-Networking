package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 17/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;

/**
 *
 * @author Eugenio Guidetti
 */
public class TraceRouteJob extends Job
{
    private final int resends;

    private static final int TRACEROUTE_INTERVAL_TICKS = 20 * 5; // 20 ticks = 1 secondo
    private int timerTicks;

    private final Ipv4Address destIp;

    private int currentSequence = 1;
    private boolean receivedReplyForSequence;
    private boolean routeCompleted = false;

    public TraceRouteJob(Ipv4Address destIp, int resends, AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        super(l3NetEntity);

        this.destIp = destIp;
        this.resends = resends;
        this.timerTicks = 0;

        receivedReplyForSequence = true;
    }

    @Override
    protected void onStart()
    {
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.traceroute.title_format").getString(),
                                         destIp,
                                         DEFAULT_TTL));

        super.onStart();
    }

    @Override
    public boolean internalTick()
    {
        // Timer scaduto
        if (timerTicks <= 0)
        {
            // Non ho ricevuto neanche una risposta
            if (!receivedReplyForSequence)
            {
                timeout();
            }

            if (routeCompleted || (currentSequence - 1) / resends >= DEFAULT_TTL)
            {
                // Job completato
                return true;
            }
            else
            {
                send();
            }
        }

        timerTicks--;
        return false;
    }


    public void handleIcmpReply(@NonNull Ipv4Packet packet)
    {
        IcmpPayload icmp = (IcmpPayload) packet.payload();

        if (icmp.type() == IcmpPayload.DESTINATION_UNREACHABLE_TYPE)
        {
            if (!receivedReplyForSequence)
            {
                // Evita di contare più volte un errore
                receivedReplyForSequence = true;

                timeout();
            }
            return;
        }

        if (icmp.type() == IcmpPayload.TIME_EXCEEDED_TYPE)
        {
            if (icmp.originalPacket() == null || !(icmp.originalPacket().payload() instanceof IcmpPayload originalIcmp))
            {
                return;
            }

            icmp = originalIcmp;
        }
        else if (icmp.type() != IcmpPayload.ECHO_REPLY_TYPE)
        {
            return;
        }

        if (icmp.sequenceNumber() != currentSequence - 1)
        {
            return;
        }

        int waited = TRACEROUTE_INTERVAL_TICKS - timerTicks;
        receivedReplyForSequence = true;

        session.sendOutput(String.format("%02dgt\t\t", waited));

        if ((currentSequence - 1) % resends == 0)
        {
            session.sendOutput(String.format("%s\n", packet.sourceIp()));

            if (icmp.type() == IcmpPayload.ECHO_REPLY_TYPE)
            {
                routeCompleted = true;
            }
        }
    }

    private void send()
    {
        if ((currentSequence - 1) % resends == 0)
        {
            session.sendOutput(String.format(" %02d: ", (currentSequence - 1) / resends + 1));
        }

        timerTicks = TRACEROUTE_INTERVAL_TICKS;
        receivedReplyForSequence = false; // Reset per la nuova sequenza

        int ttl = ((currentSequence - 1) / resends) + 1;

        IcmpPayload payload = IcmpPayload.echoRequest(jobId, currentSequence);
        currentSequence++;

        netEntity.getStack().sendL3Payload(payload, destIp, ttl);
    }

    private void timeout()
    {
        receivedReplyForSequence = true;

        session.sendOutput("*   \t\t");

        if ((currentSequence - 1) % resends == 0)
        {
            session.sendOutput(Component.translatable("mcnetworking.cli.command.traceroute.timeout").getString());
        }
    }

    @Override
    protected void onTerminate()
    {
        session.sendOutput(Component.translatable("mcnetworking.cli.command.traceroute.trace_completed").getString());

        super.onTerminate();
    }

}
