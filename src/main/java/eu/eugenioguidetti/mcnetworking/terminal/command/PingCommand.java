package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class PingCommand implements TerminalCommand
{
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
                throw new IllegalArgumentException(String.format(Component
                                                                         .translatable(
                                                                                 "mcnetworking.cli.command.invalid_resends_number_format")
                                                                         .getString(), resends));
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

        return !session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.ping").getString();
    }
}
