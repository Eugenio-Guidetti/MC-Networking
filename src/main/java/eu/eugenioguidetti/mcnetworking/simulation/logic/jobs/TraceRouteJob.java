package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 17/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
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

    private static final int TRACEROUTE_INTERVAL_TICKS = 5 * 20; // 20 ticks = 1 secondo
    private int timerTicks;

    private final Ipv4Address destIp;

    private int currentSequence = 1;
    private Ipv4Address currentHopIp = null;
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

            boolean roundFinished = (currentSequence - 1) % resends == 0;
            if ((routeCompleted && roundFinished) || (currentSequence - 1) / resends >= DEFAULT_TTL)
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


    public void handleIcmpReply(@NonNull IcmpPayload icmp, @NonNull Ipv4Address sourceIp)
    {
        routeCompleted = true;
        addOutput(icmp.sequenceNumber(), sourceIp);
    }

    public void handleIcmpError(@NonNull IcmpPayload incomingIcmp, @NonNull IcmpPayload originalIcmp, @NonNull Ipv4Address sourceIp)
    {
        if (incomingIcmp.type() == IcmpPayload.TIME_EXCEEDED_TYPE)
        {
            addOutput(originalIcmp.sequenceNumber(), sourceIp);
        }
        else if (incomingIcmp.type() == IcmpPayload.DESTINATION_UNREACHABLE_TYPE)
        {
            if (originalIcmp.sequenceNumber() == currentSequence - 1 && !receivedReplyForSequence)
            {
                receivedReplyForSequence = true;
                this.currentHopIp = sourceIp;

                String errorFlag = switch (incomingIcmp.code())
                {
                    case IcmpPayload.DESTINATION_NETWORK_UNREACHABLE_CODE -> " !N \t\t";
                    case IcmpPayload.DESTINATION_HOST_UNREACHABLE_CODE -> " !H \t\t";
                    case IcmpPayload.PROTOCOL_UNREACHABLE_CODE -> " !P \t\t";
                    default -> " !U \t\t";
                };

                session.sendOutput(errorFlag);

                this.routeCompleted = true;

                checkRoundEnd();
            }
        }
    }

    private void addOutput(int sequenceNumber, @NonNull Ipv4Address sourceIp)
    {
        if (sequenceNumber != currentSequence - 1)
        {
            return;
        }

        int waited = TRACEROUTE_INTERVAL_TICKS - timerTicks;
        receivedReplyForSequence = true;
        this.currentHopIp = sourceIp;

        session.sendOutput(String.format("%3dgt\t\t", waited));
        checkRoundEnd();
    }

    private void timeout()
    {
        receivedReplyForSequence = true;
        session.sendOutput("  * \t\t");
        checkRoundEnd();
    }

    private void checkRoundEnd()
    {
        // Se è l'ultima sonda del round di questo hop
        if ((currentSequence - 1) % resends == 0)
        {
            if (currentHopIp != null)
            {
                session.sendOutput(String.format("%s\n", currentHopIp));
            }
            else
            {
                session.sendOutput(Component.translatable("mcnetworking.cli.command.traceroute.timeout").getString());
            }
            // Reset per il prossimo hop
            this.currentHopIp = null;
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

    @Override
    protected void onTerminate()
    {
        session.sendOutput(Component.translatable("mcnetworking.cli.command.traceroute.trace_completed").getString());

        super.onTerminate();
    }

}
