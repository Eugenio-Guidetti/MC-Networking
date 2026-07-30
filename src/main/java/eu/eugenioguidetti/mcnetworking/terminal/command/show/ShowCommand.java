package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
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
public class ShowCommand extends CommandRegistrar implements TerminalCommand
{
    public ShowCommand()
    {
        super();

        commands.put("mac", new ShowMacCommand());
        commands.put("ip", new ShowIpCommand());

        commands.put("pos", new ShowPosCommand());
        commands.put("arp_cache", new ShowArpCacheCommand());
        commands.put("switching_table", new ShowSwitchingTableCommand());
        commands.put("routing_table", new ShowRoutingTableCommand());
        commands.put("interfaces", new ShowInterfacesCommand());
    }

    @Override
    public void execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException
    {
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        processInput(session, newArgs);

        session.getDevice().sync();
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG) || session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show").getString();
    }
}
