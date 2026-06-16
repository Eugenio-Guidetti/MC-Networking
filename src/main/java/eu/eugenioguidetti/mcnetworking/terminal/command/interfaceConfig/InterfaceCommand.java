package eu.eugenioguidetti.mcnetworking.terminal.command.interfaceConfig;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.core.Direction;

/**
 *
 * @author Eugenio Guidetti
 */
public class InterfaceCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        String message = null;

        Direction dir = Direction.byName(args[1].toLowerCase());
        NetworkInterface networkInterface = session.getDevice().getInterface(dir);

        if (networkInterface == null)
        {
            throw new IllegalArgumentException("Interfaccia: " + args[1].toLowerCase() + " non trovata");
        }

        session.selectInterface(dir);
        session.setCurrentMode(TerminalMode.INTERFACE_CONFIG);

        return "";
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG) || session
                .getCurrentMode()
                .equals(TerminalMode.INTERFACE_CONFIG);
    }

    @Override
    public String getDescription()
    {
        return "Vai alla modalità di configurazione " + TerminalMode.INTERFACE_CONFIG;
    }
}
