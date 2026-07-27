package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingL3Engine extends AbstractL3Engine
{
    private final RoutingTable routingTable = new RoutingTable();

    public RoutingL3Engine(NetworkingBlockEntity netEntity)
    {
        super(netEntity);
    }

    @Override
    public void processPacket(Ipv4Packet packet, String from, NetworkStack stack)
    {
        Ipv4Address sourceIp = packet.sourceIp();
        Ipv4Address destIp = packet.destIp();
        Ipv4Address interfaceIp = stack.getNetworkReceiver().getInterface(from).getIpAddress();

        // Se a un'interfaccia nella rete 'A' arriva un pacchetto dalla rete 'B', lo scarta
        if (!interfaceIp.contieneIp(sourceIp))
        {
            return;
        }

        boolean broadcast = destIp.equals(Ipv4Address.BROADCAST) || destIp.equals(interfaceIp.getIndirizzoDiBroadcast());
        boolean perMe = (from.equals(NetworkInterface.LOOPBACK_NAME) && destIp.isIndirizzoDiLoopback());
        for (NetworkInterface nic : stack.getNetworkReceiver().getNics().values())
        {
            if (nic.getIpAddress().equals(Ipv4Address.ALL_ZEROS) || nic.getName().equals(NetworkInterface.LOOPBACK_NAME))
            {
                continue;
            }

            perMe |= destIp.equals(nic.getIpAddress());
        }

        if (!perMe && !broadcast)
        {
            routePacket(packet, stack, from);

            return;
        }

        if (broadcast && isInvalidBroadcast(packet, interfaceIp))
        {
            return;
        }

        // Il pacchetto è destinato al Router
        NetworkPayload payload = packet.payload();

        if (payload instanceof ArpPayload arp)
        {
            arpManager.handleArp(arp, from, stack);
            return;
        }

        processChatMessage(packet, from, stack);
    }

    private void routePacket(Ipv4Packet packet, NetworkStack stack, String from)
    {
        // Esegui routing

        RoutingTable.Route route = routingTable.routePacket(packet.destIp(), stack.getNetworkReceiver().getNics());

        if (route == null)
        {
            ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
            session.sendError("Nessuna rotta trovata per: " + packet.destIp());

            return;
        }

        // Il pacchetto deve uscire dall'interfaccia da cui è entrato
        if (route.nicName().equals(from))
        {
            return;
        }

        if (route.type().equals(RoutingTable.RouteType.L))
        {
            processPacket(packet, route.nicName(), stack);
            return;
        }

        Ipv4Address nextHop;
        if (route.type().equals(RoutingTable.RouteType.C))
        {
            nextHop = packet.destIp();
        }
        else
        {
            nextHop = route.nextHop();
        }

        String outName = route.nicName();

        dispatchToL2(packet, nextHop, outName, stack);
    }


    @Override
    public void sendPacket(Ipv4Address destIp, NetworkPayload payload, NetworkStack stack)
    {
        // Determino nextHop e interfaccia di uscita del pacchetto

        String outName = null;
        Ipv4Address nextHop = null;

        if (destIp.isIndirizzoDiLoopback())
        {
            outName = NetworkInterface.LOOPBACK_NAME;
            nextHop = Ipv4Address.LOOPBACK;
        }
        else
        {
            // Controllo se il destinatario è in una rete direttamente connessa a me
            outName = getOutName(destIp, stack);

            if (outName != null)
            {
                // Rete di destinazione direttamente connessa
                nextHop = destIp;
            }
            else
            {
                // Rete di destinazione non direttamente connessa: consulta tabella di routing
                RoutingTable.Route route = routingTable.routePacket(destIp, stack.getNetworkReceiver().getNics());

                if (route == null)
                {
                    ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
                    session.sendError("Nessuna rotta trovata per: " + destIp);

                    return;
                }

                outName = route.nicName();
                nextHop = route.nextHop();
            }
        }

        sendPacketOut(destIp, payload, nextHop, outName, stack);
    }

    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }
}