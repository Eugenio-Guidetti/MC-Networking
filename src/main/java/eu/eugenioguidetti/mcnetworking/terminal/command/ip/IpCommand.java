package eu.eugenioguidetti.mcnetworking.terminal.command.ip;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.CommandRegistrar;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpCommand extends CommandRegistrar implements TerminalCommand
{
    public IpCommand()
    {
        super();

        commands.put("address", new IpAddressCommand());
        commands.put("default_gateway", new IpDefaultGatewayCommand());
    }

    @Override
    public void execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
    {
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        processInput(session, newArgs);

        session.getDevice().sync();
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getDevice().getDeviceLayer() >= 3 && (session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG) || session
                .getCurrentMode()
                .equals(TerminalMode.INTERFACE_CONFIG));
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.ip").getString();
    }
}
