package eu.eugenioguidetti.mcnetworking.terminal.command.globalConfig;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class HostnameCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException
    {
        session.getDevice().setHostname(args[1]);
        session.sendOutput("");
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.hostname").getString();
    }
}
