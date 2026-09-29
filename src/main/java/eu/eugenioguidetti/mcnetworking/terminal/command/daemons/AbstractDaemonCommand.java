package eu.eugenioguidetti.mcnetworking.terminal.command.daemons;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 28/09/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.command.CommandRegistrar;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractDaemonCommand extends CommandRegistrar implements TerminalCommand
{
    public AbstractDaemonCommand()
    {
        super();

        commands.put("stop", new StopDaemonCommand());
        commands.put("status", new StatusDaemonCommand());
    }
}
