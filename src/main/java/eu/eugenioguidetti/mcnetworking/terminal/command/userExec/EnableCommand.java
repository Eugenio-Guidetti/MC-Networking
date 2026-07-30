package eu.eugenioguidetti.mcnetworking.terminal.command.userExec;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class EnableCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        session.setCurrentMode(TerminalMode.PRIV_EXEC);
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return String.format(Component.translatable("mcnetworking.cli.command.description.enable_format").getString(),
                             TerminalMode.PRIV_EXEC);
    }
}
