package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.L3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingL3Engine implements L3Engine
{
    private final ArpManager arpManager = new ArpManager();

    private final RoutingTable routingTable = new RoutingTable();


    // Serve solo a mostrare i messaggi nella chat di gioco
    NetworkingBlockEntity netEntity;

    public void setNetEntity(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }

    private void processChatMessage(NetworkStack stack, Ipv4Packet packet)
    {
        String message = "§a" + stack.getNetworkReceiver().getHostname() + ": Ricevuto: " + packet.payload().toString();

        ServerLevel serverLevel = (ServerLevel) netEntity.getLevel();
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
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

        if (broadcast)
        {
            if (!packet.sourceIp().contieneIp(interfaceIp) && !packet.sourceIp().equals(Ipv4Address.ALL_ZEROS))
            {
                return;
            }
        }

        // Il pacchetto è destinato al Router
        NetworkPayload payload = packet.payload();

        if (payload instanceof ArpPayload arp)
        {
            arpManager.handleArp(arp, from, stack);
            return;
        }

        processChatMessage(stack, packet);
    }

    private void routePacket(Ipv4Packet packet, NetworkStack stack, String from)
    {
        // Esegui routing

        RoutingTable.Route route = routingTable.routePacket(packet.destIp(), stack.getNetworkReceiver().getNics());

        if (route == null)
        {
            MCNetworking.LOGGER.error("Nessuna rotta trovata");

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
        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outName);
        MacAddress targetMac = arpManager.resolveMac(nextHop);

        if (targetMac == null)
        {
            arpManager.enqueuePacket(packet, nextHop, outName, stack);
            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);
        stack.sendFrameOut(frame, outName);
    }


    @Override
    public void sendPacket(Ipv4Address destIp, NetworkPayload payload, NetworkStack stack)
    {
        String outName = null;

        if (destIp.isIndirizzoDiLoopback())
        {
            outName = NetworkInterface.LOOPBACK_NAME;
        }
        else
        {
            for (var entry : stack.getNetworkReceiver().getNics().entrySet())
            {
                if (entry.getKey().equals(NetworkInterface.LOOPBACK_NAME))
                {
                    continue;
                }

                // Interfaccia non configurata
                if (entry.getValue().getIpAddress().equals(Ipv4Address.ALL_ZEROS))
                {
                    continue;
                }

                if (entry.getValue().getIpAddress().contieneIp(destIp))
                {
                    // Rete di destinazione direttamente connessa
                    outName = entry.getKey();

                    break;
                }
            }
        }

        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outName);
        Ipv4Address outIp = outNic.getIpAddress();

        Ipv4Packet packet = new Ipv4Packet(outIp, destIp, payload);

        if (destIp.equals(outIp) || destIp.isIndirizzoDiLoopback())
        {
            // "Invio" il pacchetto a me stesso
            processPacket(packet, outName, stack);

            return;
        }

        MacAddress targetMac;
        if (destIp.isIndirizzoDiBroadcast(destIp.getLunghezzaPrefisso()))
        {
            targetMac = MacAddress.BROADCAST;
        }
        else
        {
            targetMac = arpManager.resolveMac(destIp);
        }

        if (targetMac == null)
        {
            arpManager.enqueuePacket(packet, destIp, outName, stack);
            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);
        stack.sendFrameOut(frame, outName);
    }

    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    public ArpManager getArpManager()
    {
        return arpManager;
    }
}