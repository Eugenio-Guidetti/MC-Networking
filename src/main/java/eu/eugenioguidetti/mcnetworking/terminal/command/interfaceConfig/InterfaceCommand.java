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
    public void execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        String message = null;

        NetworkInterface nic;
        String name = args[1];

        // toLowerCase non dovrebbe servire
        Direction dir = Direction.byName(name.toLowerCase());

        if (dir != null)
        {
            nic = session.getDevice().getInterface(dir);
        }
        else
        {
            nic = session.getDevice().getInterface(name);
        }


        if (nic == null)
        {
            throw new IllegalArgumentException("Interfaccia: " + args[1].toLowerCase() + " non trovata");
        }

        session.selectInterface(nic.getName());
        session.setCurrentMode(TerminalMode.INTERFACE_CONFIG);
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
        return "Vai alla modalità di configurazione " + TerminalMode.INTERFACE_CONFIG;
    }
}
