package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        session.setCurrentMode(TerminalMode.USER_EXEC);
        session.selectInterface(null);
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return !session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return String.format(Component.translatable("mcnetworking.cli.command.description.end_format").getString(), TerminalMode.USER_EXEC);
    }
}
