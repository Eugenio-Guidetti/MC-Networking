package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.PingJob;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class PingCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session,
                        String @NonNull [] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        AbstractL3NetworkingBlockEntity l3NetEntity = (AbstractL3NetworkingBlockEntity) session.getDevice();
        Ipv4Address destIp = new Ipv4Address(args[1]);

        int resends;
        if (args.length == 3)
        {
            resends = Integer.parseInt(args[2]);

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

        l3NetEntity.startJob(new PingJob(destIp, resends, l3NetEntity), true);
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        if (!(session.getDevice() instanceof AbstractL3NetworkingBlockEntity))
        {
            return false;
        }

        return !session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.ping").getString();
    }
}
