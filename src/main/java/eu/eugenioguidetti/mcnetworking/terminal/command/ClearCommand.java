package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 18/07/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClearCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        TerminalCache.clearBlock(session.getLevel(), session.getPos());
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return "Pulisce il terminale";
    }
}
