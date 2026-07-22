package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ApplicationPayload;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;

/**
 *
 * @author Eugenio Guidetti
 */
public class PingJob implements Job
{
    private final int INTERVAL_TICKS = 40; // 20 tick = 1 secondo (ritardo standard di un ping)

    private final Ipv4Address destIp;
    private final String message;
    private int remainingResends;
    private int timerTicks;

    public PingJob(Ipv4Address destIp, String message, int resends)
    {
        this.destIp = destIp;
        this.message = message;
        this.remainingResends = resends;
        this.timerTicks = 0;
    }

    @Override
    public boolean tick(NetworkingBlockEntity netEntity)
    {
        if (!(netEntity instanceof HostBlockEntity host))
        {
            throw new IllegalStateException("Dispositivo non in grado di eseguire ping");
        }

        if (remainingResends == 0)
        {
            return true;
        }

        if (timerTicks <= 0)
        {
            host.triggerSendPacket(destIp, new ApplicationPayload(message));
            remainingResends--;
            timerTicks = INTERVAL_TICKS; // Reimposta il timer per il pacchetto successivo

            ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
            session.sendOutput("Invio ping, rimanenti: " + remainingResends);
        }
        else
        {
            timerTicks--;
        }

        return remainingResends == 0;
    }
}
