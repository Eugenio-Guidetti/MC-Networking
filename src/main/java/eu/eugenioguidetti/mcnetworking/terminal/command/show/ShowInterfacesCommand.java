package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowInterfacesCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        StringBuilder sb = new StringBuilder(Component.translatable("mcnetworking.cli.command.show.interfaces.output").getString());

        List<NetworkInterface> nics = new ArrayList<>(session.getDevice().getNics().values());

        nics.sort(Comparator.comparing(NetworkInterface::getName, String.CASE_INSENSITIVE_ORDER));

        for (NetworkInterface nic : nics)
        {
            sb.append("\n ");

            sb.append(nic.getName());

            if (nic.getDirection() != null)
            {
                sb.append(":");
                sb.append(nic.getDirection().getName());
            }

            sb.append(" ");

            if (nic.isLoopback())
            {
                sb.append(Component.translatable("mcnetworking.cli.command.show.interfaces.loopback").getString());
            }
            else if (nic.isConnected())
            {
                sb.append(Component.translatable("mcnetworking.cli.command.show.interfaces.connected").getString());
            }
            else
            {
                sb.append(Component.translatable("mcnetworking.cli.command.show.interfaces.not_connected").getString());
            }
        }

        session.sendOutput(sb.toString());
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.interfaces").getString();
    }
}
