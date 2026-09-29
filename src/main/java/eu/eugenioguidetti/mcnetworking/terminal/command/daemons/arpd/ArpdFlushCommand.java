package eu.eugenioguidetti.mcnetworking.terminal.command.daemons.arpd;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpdFlushCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        int arpJobId = Integer.parseInt(args[1]);

        if (arpJobId == -1)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.command.jobs.daemon_not_running_format").getString(),
                                            ArpManager.class.getSimpleName()));

            return;
        }

        Optional<Job> optionalJob = session.getDevice().getJob(arpJobId);

        if (optionalJob.isEmpty())
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.command.jobs.not_found_format").getString(),
                                            arpJobId));

            return;
        }

        ArpManager arpManager = (ArpManager) optionalJob.get();
        arpManager.clearCache();
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.arpd.flush").getString();
    }
}
