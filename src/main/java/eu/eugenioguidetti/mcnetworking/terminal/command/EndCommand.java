package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        session.setCurrentMode(TerminalMode.USER_EXEC);
        session.selectInterface(null);
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return !session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return String.format(Component.translatable("mcnetworking.cli.command.description.end_format").getString(), TerminalMode.USER_EXEC);
    }
}
