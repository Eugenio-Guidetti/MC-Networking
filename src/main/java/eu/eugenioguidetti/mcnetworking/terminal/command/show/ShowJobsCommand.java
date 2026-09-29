package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowJobsCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        session.sendOutput(Component.translatable("mcnetworking.cli.command.show.jobs.output").getString());

        for (Job j : session.getDevice().getRunningJobs())
        {
            session.sendOutput(" " + j.toString());
        }
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.jobs").getString();
    }
}
