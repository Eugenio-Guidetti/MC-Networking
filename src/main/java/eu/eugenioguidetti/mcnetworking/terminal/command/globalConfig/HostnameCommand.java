package eu.eugenioguidetti.mcnetworking.terminal.command.globalConfig;

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
public class HostnameCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException
    {
        session.getDevice().setHostname(args[1]);
        return "";
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG);
    }

    @Override
    public String getDescription()
    {
        return "Imposta l'hostname del dispositivo";
    }
}
