package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
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
public class PingJob extends Job
{
    private static final int PING_INTERVAL_TICKS = 5 * 20; // 20 ticks = 1 secondo
    private int timerTicks;
    private int remainingResends;

    private final Ipv4Address destIp;

    private int currentSequence = 1;
    private boolean receivedReplyForSequence;

    private final PingStats stats;


    public PingJob(Ipv4Address destIp, int resends, AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        super(l3NetEntity);

        this.destIp = destIp;
        this.remainingResends = resends;
        this.timerTicks = 0;

        receivedReplyForSequence = true;

        this.stats = new PingStats(destIp, resends);
    }

    @Override
    protected void onStart()
    {
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.ping.title_format").getString(),
                                         destIp,
                                         remainingResends));

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

            if (remainingResends > 0)
            {
                sendPing();
            }
            else
            {
                // Job completato
                return true;
            }
        }

        timerTicks--;
        return false;
    }

    public void handleIcmpReply(@NonNull Ipv4Packet packet)
    {
        IcmpPayload icmp = (IcmpPayload) packet.payload();

        if (icmp.type() == IcmpPayload.TIME_EXCEEDED_TYPE || icmp.type() == IcmpPayload.DESTINATION_UNREACHABLE_TYPE)
        {
            if (!receivedReplyForSequence)
            {
                // Evita di contare più volte un errore
                receivedReplyForSequence = true;

                timeout();
            }
            return;
        }
        else if (icmp.type() != IcmpPayload.ECHO_REPLY_TYPE)
        {
            return;
        }

        if (icmp.sequenceNumber() != currentSequence - 1)
        {
            return;
        }

        int waited = PING_INTERVAL_TICKS - timerTicks;
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.ping.reply_format").getString(),
                                         packet.sourceIp(),
                                         waited,
                                         packet.ttl()));

        stats.addReceivedPacket(waited);
        receivedReplyForSequence = true;
    }

    private void sendPing()
    {
        remainingResends--;
        timerTicks = PING_INTERVAL_TICKS;
        receivedReplyForSequence = false; // Reset per la nuova sequenza

        IcmpPayload payload = IcmpPayload.echoRequest(jobId, currentSequence);
        currentSequence++;

        stats.addSentPacket();

        netEntity.getStack().sendL3Payload(payload, destIp, DEFAULT_TTL);
    }

    private void timeout()
    {
        receivedReplyForSequence = true;

        session.sendOutput(Component.translatable("mcnetworking.cli.command.ping.timeout").getString());
    }

    @Override
    protected void onTerminate()
    {
        session.sendOutput(stats.toString());

        super.onTerminate();
    }

    private static class PingStats
    {
        private final Ipv4Address destIp;
        private final int totResends;

        private int totWaitingTicks = 0;
        private int sentPackets = 0;
        private int receivedPackets = 0;

        public PingStats(Ipv4Address destIp, int totResends)
        {
            this.destIp = destIp;
            this.totResends = totResends;
        }

        public void addSentPacket()
        {
            sentPackets++;
        }

        public void addReceivedPacket(int waitingTicks)
        {
            receivedPackets++;
            totWaitingTicks += waitingTicks;
        }


        @Override
        public @NonNull String toString()
        {
            int lostPackets = sentPackets - receivedPackets;

            String mediaFormattata = String.format(java.util.Locale.ROOT,
                                                   "%.2f",
                                                   receivedPackets > 0 ? (double) totWaitingTicks / receivedPackets : 0d);

            String pacchettiPersi = "";

            if (lostPackets >= 0)
            {
                String percFormattata = String.format(java.util.Locale.ROOT, "%.2f", lostPackets * 100d / sentPackets);
                pacchettiPersi = String.format(Component.translatable("mcnetworking.cli.command.ping.stats_lost_format").getString(),
                                               lostPackets,
                                               percFormattata);
            }

            return String.format(Component.translatable("mcnetworking.cli.command.ping.stats_format").getString(),
                                 destIp,
                                 mediaFormattata,
                                 sentPackets,
                                 totResends,
                                 receivedPackets,
                                 pacchettiPersi);
        }
    }
}
