package eu.eugenioguidetti.mcnetworking.terminal.command.privExec;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;

/**
 *
 * @author Eugenio Guidetti
 */
public class PingCommand implements TerminalCommand
{
    //ping [dest_ip] [message] [<resends>]

    @Override
    public void execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        HostBlockEntity host = (HostBlockEntity) session.getDevice();
        Ipv4Address destIp = new Ipv4Address(args[1]);
        String message = args[2];
        int resends;

        if (args.length == 4)
        {
            resends = Integer.parseInt(args[3]);

            if (resends <= 0 && resends != -1)
            {
                throw new IllegalArgumentException("Numero di resend non valido: " + resends);
            }
        }
        else
        {
            resends = 4;
        }

        host.startJob(new PingJob(destIp, message, resends));
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        if (!(session.getDevice() instanceof HostBlockEntity))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return "Invia messaggi di ping (non ancora pacchetti ICMP) ad un altro host";
    }
}
