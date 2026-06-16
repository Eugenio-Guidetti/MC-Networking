package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import net.minecraft.core.Direction;

/**
 *
 * @author Eugenio Guidetti
 */
public class ConsoleSession
{
    private final NetworkingBlockEntity device;
    private TerminalMode currentMode = TerminalMode.GLOBAL_CONFIG;

    // Se siamo in (config-if), qui salviamo quale interfaccia stiamo modificando
    private Direction selectedInterface = null;

    public ConsoleSession(NetworkingBlockEntity device)
    {
        this.device = device;
    }

    public NetworkingBlockEntity getDevice()
    {
        return device;
    }

    public TerminalMode getCurrentMode()
    {
        return currentMode;
    }

    public void setCurrentMode(TerminalMode currentMode)
    {
        this.currentMode = currentMode;
    }

    public NetworkInterface getSelectedInterface()
    {
        return device.getInterface(this.selectedInterface);
    }

    public Direction getSelectedInterfaceDirection()
    {
        return selectedInterface;
    }

    public void selectInterface(Direction selectedInterface)
    {
        this.selectedInterface = selectedInterface;
    }

    public String getPrompt()
    {
        return device.getHostname() + currentMode.getPromptSuffix();
    }

    @Override
    public String toString()
    {
        return "ConsoleSession{" + "device=" + device + ", currentMode=" + currentMode + '}';
    }
}