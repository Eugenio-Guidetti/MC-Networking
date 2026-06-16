package eu.eugenioguidetti.mcnetworking.terminal.command.privExec;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;

/**
 *
 * @author Eugenio Guidetti
 */
public class PingCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        int resends;

        HostBlockEntity host = (HostBlockEntity) session.getDevice();
        Ipv4Address destIp = new Ipv4Address(args[1]);

        if (args.length == 4)
        {
            resends = Integer.parseInt(args[3]);

            if (resends <= 0)
            {
                throw new IllegalArgumentException("Numero di resend non valido: " + resends);
            }
        }
        else
        {
            resends = 4;
        }

        for (int i = 0; i < resends; i++)
        {
            host.triggerSendPacket(destIp, args[2]);
        }

        return "";
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        if (!(session.getDevice() instanceof HostBlockEntity))
        {
            return false;
        }

        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription()
    {
        return "Ping di test";
    }
}
