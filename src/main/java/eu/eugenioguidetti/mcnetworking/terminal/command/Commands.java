package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.command.globalConfig.HostnameCommand;
import eu.eugenioguidetti.mcnetworking.terminal.command.interfaceConfig.InterfaceCommand;
import eu.eugenioguidetti.mcnetworking.terminal.command.interfaceConfig.IpCommand;
import eu.eugenioguidetti.mcnetworking.terminal.command.privExec.ConfigureCommand;
import eu.eugenioguidetti.mcnetworking.terminal.command.privExec.PingCommand;
import eu.eugenioguidetti.mcnetworking.terminal.command.userExec.EnableCommand;

/**
 *
 * @author Eugenio Guidetti
 */
public class Commands extends CommandRegistrar
{
    public Commands()
    {
        super();

        commands.put("configure", new ConfigureCommand());
        commands.put("enable", new EnableCommand());
        commands.put("end", new EndCommand());
        commands.put("exit", new ExitCommand());
        commands.put("hostname", new HostnameCommand());
        commands.put("interface", new InterfaceCommand());
        commands.put("ip", new IpCommand());
        commands.put("ping", new PingCommand());
        commands.put("show", new ShowCommand());
    }
}