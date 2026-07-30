package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowIpCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.ip.output_format").getString(),
                                         session.getSelectedInterface().getIpAddress().toString()));
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.ip").getString();
    }
}
