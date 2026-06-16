package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.L3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingL3Engine implements L3Engine
{
    private final ArpManager arpManager = new ArpManager();
    // Il router avrà anche una tabella di routing
    // private final RoutingTable routingTable = new RoutingTable();

    @Override
    public void processPacket(Ipv4Packet packet, Direction from, NetworkStack stack)
    {
        Ipv4Address interfaceIp = stack.getNetworkReceiver().getInterface(from).getIpAddress();

        // 1. Il pacchetto è destinato al Router stesso? (es. Ping al router o ARP)
        if (packet.destIp().equals(interfaceIp) || packet.destIp().equals(Ipv4Address.BROADCAST))
        {
            NetworkPayload payload = packet.payload();

            if (payload instanceof ArpPayload arp)
            {
                arpManager.handleArp(arp, from, stack);
            }
            return;
        }

        // 2. Altrimenti, è traffico di transito (Routing)
        // - Controlla la routing table per trovare la Direction d'uscita (next hop)
        // - Usa arpManager.resolveMac(nextHopIp) per trovare il MAC di destinazione
        // - Se resolveMac restituisce null, usa arpManager.sendArpRequest(...) e metti il pacchetto IP in una coda di attesa
    }

    @Override
    public void sendPacket(Ipv4Packet packet, NetworkStack stack)
    {
        // TODO: consultare routing table
        throw new NotImplementedException();
    }
}