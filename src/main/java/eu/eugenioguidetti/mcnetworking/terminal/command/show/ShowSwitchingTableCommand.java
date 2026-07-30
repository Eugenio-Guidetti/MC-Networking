package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.SwitchBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowSwitchingTableCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        Map<MacAddress, String> switchingTable = null;

        if (session.getDevice() instanceof SwitchBlockEntity switchBlockEntity)
        {
            switchingTable = switchBlockEntity.getSwitchingTable();
        }

        if (switchingTable == null || switchingTable.isEmpty())
        {
            session.sendOutput(Component.translatable("mcnetworking.cli.command.show.switching_table.empty").getString());
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (var entry : switchingTable.entrySet())
        {
            sb.append(String.format(Component.translatable("mcnetworking.cli.command.show.switching_table.row_format").getString(),
                                    entry.getKey(),
                                    entry.getValue()));
        }

        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.switching_table.output_format").getString(),
                                         sb));
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC) && session.getDevice() instanceof SwitchBlockEntity;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.switching_table").getString();
    }
}
