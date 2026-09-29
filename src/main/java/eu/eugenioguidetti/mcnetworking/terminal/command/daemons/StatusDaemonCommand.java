package eu.eugenioguidetti.mcnetworking.terminal.command.daemons;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class StatusDaemonCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        int jobId = Integer.parseInt(args[1]);

        Job daemon = session
                .getDevice()
                .getJob(jobId)
                .orElseThrow(() -> new IllegalStateException(String.format(Component
                                                                                   .translatable(
                                                                                           "mcnetworking.cli.command.jobs.not_found_format")
                                                                                   .getString(), jobId)));

        session.sendOutput(daemon.toString());
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.job.status").getString();
    }
}
