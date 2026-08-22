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
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowMacCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.mac.output_format").getString(),
                                         session.getSelectedInterface().getMacAddress().toString()));
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {

        return Component.translatable("mcnetworking.cli.command.description.show.mac").getString();
    }
}
