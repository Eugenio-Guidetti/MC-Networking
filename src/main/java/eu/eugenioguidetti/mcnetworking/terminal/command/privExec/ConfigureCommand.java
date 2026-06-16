package eu.eugenioguidetti.mcnetworking.terminal.command.privExec;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;

/**
 *
 * @author Eugenio Guidetti
 */
public class ConfigureCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args)
    {
        session.setCurrentMode(TerminalMode.GLOBAL_CONFIG);
        return "";
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription()
    {
        return "Vai alla modalità di configurazione " + TerminalMode.GLOBAL_CONFIG;
    }
}
