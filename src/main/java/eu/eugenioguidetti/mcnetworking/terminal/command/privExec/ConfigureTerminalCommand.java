package eu.eugenioguidetti.mcnetworking.terminal.command.privExec;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ConfigureTerminalCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        session.setCurrentMode(TerminalMode.GLOBAL_CONFIG);
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return String.format(Component.translatable("mcnetworking.cli.command.description.configure_format").getString(),
                             TerminalMode.GLOBAL_CONFIG);
    }
}
