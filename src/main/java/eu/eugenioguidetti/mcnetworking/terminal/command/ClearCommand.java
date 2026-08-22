package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 18/07/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClearCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        TerminalCache.clearBlock(session.getLevel(), session.getPos());
        session.updateOutput();
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.clear").getString();
    }
}
