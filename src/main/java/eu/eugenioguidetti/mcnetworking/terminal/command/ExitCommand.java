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
public class ExitCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args) throws IllegalStateException
    {
        switch (session.getCurrentMode())
        {
            case GLOBAL_CONFIG:
                session.setCurrentMode(TerminalMode.PRIV_EXEC);
                break;
            case INTERFACE_CONFIG:
                session.deselectInterface();
                session.setCurrentMode(TerminalMode.GLOBAL_CONFIG);
                break;
            default:
                throw new IllegalStateException(session.getCurrentMode().name());
        }
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG) || session
                .getCurrentMode()
                .equals(TerminalMode.INTERFACE_CONFIG);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return "Vai alla modalità precedente";
    }
}
