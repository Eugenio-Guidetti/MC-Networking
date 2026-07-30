package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingTable;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowRoutingTableCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        RoutingTable routingTable = null;

        if (session.getDevice() instanceof RouterBlockEntity router)
        {
            routingTable = router.getRoutingTable();
        }

        if (routingTable == null)
        {
            throw new IllegalStateException(Component.translatable("mcnetworking.cli.command.show.routing_table.missing").getString());
        }

        List<RoutingTable.Route> routes = routingTable.getRoutes(session.getDevice().getNics());

        if (routes == null || routes.isEmpty())
        {
            session.sendOutput(Component.translatable("mcnetworking.cli.command.show.routing_table.empty").getString());
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (var route : routes)
        {
            sb.append(String.format(Component.translatable("mcnetworking.cli.command.show.routing_table.row_format").getString(), route));
        }

        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.routing_table.output_format").getString(),
                                         sb));
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session
                .getCurrentMode()
                .equals(TerminalMode.PRIV_EXEC) && session.getDevice() instanceof RouterBlockEntity routerBlockEntity;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.routing_table").getString();
    }
}
