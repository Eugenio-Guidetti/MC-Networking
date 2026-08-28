package eu.eugenioguidetti.mcnetworking.terminal.command.ip;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingTable;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.UndoableTerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpRouteCommand implements UndoableTerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (!(session.getDevice() instanceof RouterBlockEntity router))
        {
            throw new IllegalStateException("Comando non supportato su: " + session.getDevice().getClass().getSimpleName());
        }

        Ipv4CidrAddress destNetwork = createDestNetwork(args);

        Ipv4Address nextHop = new Ipv4Address(args[3]);
        String outName = getOutName(session, nextHop);

        if (outName == null)
        {
            throw new IllegalArgumentException(Component.translatable("mcnetworking.cli.command.ip.route.invalid_next_hop").getString());
        }

        int costo;
        if (args.length >= 5)
        {
            costo = Integer.parseInt(args[4]);

            if (costo < 0)
            {
                throw new IllegalArgumentException();
            }
        }
        else
        {
            costo = 0;
        }

        router.getRoutingTable().addRoute(RoutingTable.RouteType.S, destNetwork, nextHop, costo, outName);
    }

    private static @NonNull Ipv4CidrAddress createDestNetwork(String @NonNull [] args)
    {
        Ipv4CidrAddress destNetwork = new Ipv4CidrAddress(args[1], args[2]);
        if (destNetwork.isLoopback())
        {
            throw new IllegalArgumentException(Component
                                                       .translatable("mcnetworking.cli.command.ip.route.invalid_dest_network")
                                                       .getString());
        }
        if (!destNetwork.isIndirizzoDiRete())
        {
            throw new IllegalArgumentException(Component
                                                       .translatable("mcnetworking.cli.command.ip.route.invalid_dest_network")
                                                       .getString());
        }
        if (destNetwork.subnetMask().lunghezzaPrefisso() >= 31)
        {
            throw new IllegalArgumentException(Component
                                                       .translatable("mcnetworking.cli.command.ip.route.invalid_dest_network")
                                                       .getString());
        }
        if (destNetwork.address().isAllZeros() ^ destNetwork.subnetMask().lunghezzaPrefisso() == 0)
        {
            throw new IllegalArgumentException(Component
                                                       .translatable("mcnetworking.cli.command.ip.route.invalid_dest_network")
                                                       .getString());
        }
        return destNetwork;
    }

    private static @Nullable String getOutName(@NonNull ConsoleSession session, Ipv4Address nextHop)
    {
        String outName = null;

        for (NetworkInterface nic : session.getDevice().getNics().values())
        {
            if (nic.isLoopback() || nic.getIpAddress().equals(Ipv4CidrAddress.ALL_ZEROS))
            {
                continue;
            }

            if (nextHop.equals(nic.getIpAddress().address()))
            {
                throw new IllegalArgumentException(Component.translatable("mcnetworking.cli.command.ip.route.invalid_next_hop_this_router")
                                                           .getString());
            }

            if (nic.getIpAddress().contieneIp(nextHop))
            {
                outName = nic.getName();
            }
        }
        return outName;
    }


    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return (session.getDevice() instanceof RouterBlockEntity) && session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.ip.route").getString();
    }

    @Override
    public void undo(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (!(session.getDevice() instanceof RouterBlockEntity router))
        {
            throw new IllegalStateException("Comando non supportato su: " + session.getDevice().getClass().getSimpleName());
        }

        Ipv4CidrAddress destNetwork = createDestNetwork(args);

        router.getRoutingTable().removeStaticRoute(destNetwork);
    }
}
