package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args)
    {
        session.setCurrentMode(TerminalMode.USER_EXEC);
        session.selectInterface(null);
        return "";
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return !session.getCurrentMode().equals(TerminalMode.USER_EXEC);
    }

    @Override
    public String getDescription()
    {
        return "Vai alla modalità di configurazione " + TerminalMode.USER_EXEC;
    }
}
