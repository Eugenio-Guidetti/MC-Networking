package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.SwitchBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices.RoutingTable;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException
    {
        if (session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG))
        {
            executeInterface(session, args, session.getSelectedInterface());
        }
        else
        {
            executePrivExec(session, args);
        }
    }

    private void executeInterface(ConsoleSession session,
                                  String[] args,
                                  NetworkInterface selectedInterface) throws ArrayIndexOutOfBoundsException
    {
        String message = null;

        switch (args[1])
        {
            case "mac":
                message = selectedInterface.getMacAddress().toString();
                break;

            case "ip":
                message = selectedInterface.getIpAddress().toString();
                break;

            default:
                message = "Nessuna informazione disponibile per: " + args[1];
        }

        session.sendOutput(message);
    }

    private void executePrivExec(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        String message = null;
        StringBuilder argsSb = new StringBuilder(args[1]);

        for (int i = 2; i < args.length; i++)
        {
            argsSb.append(" ");
            argsSb.append(args[i]);
        }

        switch (argsSb.toString())
        {
            case "pos":
                message = session.getDevice().getBlockPos().toShortString();
                break;

            case "arp":
                if (session.getDevice().getDeviceLayer() < 3)
                {
                    throw new IllegalArgumentException("Operazione non valida per " + session.getDevice().getClass().getSimpleName());
                }

                Map<Ipv4Address, MacAddress> arpCache = null;

                if (session.getDevice() instanceof HostBlockEntity host)
                {
                    arpCache = host.getArpCache();
                }

                else if (session.getDevice() instanceof RouterBlockEntity router)
                {
                    arpCache = router.getArpCache();
                }

                if (arpCache.isEmpty())
                {
                    message = "Arp cache vuota";
                    break;
                }

                StringBuilder sbArp = new StringBuilder("Arp:");

                for (var entry : arpCache.entrySet())
                {
                    sbArp.append("\n ");
                    sbArp.append(entry.getKey());
                    sbArp.append(" -> ");
                    sbArp.append(entry.getValue());
                }

                message = sbArp.toString();
                break;

            case "switching table":
                if (!(session.getDevice() instanceof SwitchBlockEntity switchBlockEntity))
                {
                    throw new IllegalArgumentException("Operazione non valida per " + session.getDevice().getClass().getSimpleName());
                }

                if (switchBlockEntity.getSwitchingTable().isEmpty())
                {
                    message = "Switching table vuota";
                    break;
                }

                StringBuilder sbSwitchingTable = new StringBuilder("Switching table:");

                for (var entry : switchBlockEntity.getSwitchingTable().entrySet())
                {
                    sbSwitchingTable.append("\n ");
                    sbSwitchingTable.append(entry.getKey());
                    sbSwitchingTable.append(" -> ");
                    sbSwitchingTable.append(entry.getValue());
                }

                message = sbSwitchingTable.toString();
                break;

            case "routing table":
                if (!(session.getDevice() instanceof RouterBlockEntity routerBlockEntity))
                {
                    throw new IllegalArgumentException("Operazione non valida per " + session.getDevice().getClass().getSimpleName());
                }

                List<RoutingTable.Route> routes = routerBlockEntity.getRoutingTable().getRoutes(session.getDevice().getNics());

                if (routes.isEmpty())
                {
                    message = "Routing table vuota";
                    break;
                }

                StringBuilder sbRoutingTable = new StringBuilder("Routing table:");

                for (RoutingTable.Route r : routes)
                {
                    sbRoutingTable.append("\n ");
                    sbRoutingTable.append(r);
                }

                message = sbRoutingTable.toString();
                break;

            case "interfaces":
                StringBuilder sbInterfacce = new StringBuilder("Interfacce:");

                List<NetworkInterface> nics = new ArrayList<>(session.getDevice().getNics().values());

                nics.sort(Comparator.comparing(NetworkInterface::getName, String.CASE_INSENSITIVE_ORDER));

                for (NetworkInterface nic : nics)
                {
                    sbInterfacce.append("\n ");

                    sbInterfacce.append(nic.getName());

                    if (nic.getDirection() != null)
                    {
                        sbInterfacce.append(":");
                        sbInterfacce.append(nic.getDirection().getName());
                    }

                    if (nic.isLoopback())
                    {
                        sbInterfacce.append(" (loopback)");
                    }
                    else if (nic.isConnected())
                    {
                        sbInterfacce.append(" (connessa)");
                    }
                    else
                    {
                        sbInterfacce.append(" (non connessa)");
                    }
                }

                message = sbInterfacce.toString();
                break;

            default:
                message = "Nessuna informazione disponibile per: " + argsSb;
        }

        session.sendOutput(message);
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG) || session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        if (session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG))
        {
            return "Ottieni informazioni sull'interfaccia\nOpzioni disponibili: mac, ip";
        }

        return "Ottieni informazioni sull'apparato\nOpzioni disponibili: pos, arp, switching table, routing table, interfaces";
    }
}
