package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL3Engine implements L3Engine
{
    private final ArpManager arpManager = new ArpManager();
    // Quando non so l'indirizzo MAC associato all'indirizzo IP del destinatario metto il pacchetto destinato a lui in coda per inviarlo all'arrivo della ARP reply
    private final Map<Integer, List<Ipv4Packet>> arpOutQueue = new HashMap<>();
    NetworkingBlockEntity netEntity;
    private Ipv4Address defaultGateway = null;

    public void setNetEntity(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }

    @Override
    public void processPacket(Ipv4Packet packet, Direction from, NetworkStack stack)
    {
        Ipv4Address interfaceIp = stack.getNetworkReceiver().getInterface(from).getIpAddress();

        // Il pacchetto non è destinato a me

        Ipv4Address destIp = packet.destIp();
        if (!destIp.equals(interfaceIp) && !destIp.isIndirizzoDiBroadcast(interfaceIp.getLunghezzaPrefisso()) && !destIp.isIndirizzoDiLoopback())
        {
            return;
        }


        NetworkPayload payload = packet.payload();

        if (payload instanceof ArpPayload arp)
        {
            int operazioneArpGestita = arpManager.handleArp(arp, from, stack);
            if (operazioneArpGestita != ArpPayload.OPERATION_ARP_REPLY)
            {
                return;
            }

            int resolvedIp = arp.senderIp().getIp();

            List<Ipv4Packet> outPackets = arpOutQueue.getOrDefault(resolvedIp, null);

            if (outPackets == null)
            {
                return;
            }

            for (Ipv4Packet outPacket : outPackets)
            {
                sendPacket(outPacket, stack);
            }

            arpOutQueue.remove(resolvedIp);

            return;
        }
        else
        {
            processChatMessage(stack, packet);
        }

        // ... logica per ICMP, TCP, UDP, ecc.

    }

    private void processChatMessage(NetworkStack stack, Ipv4Packet packet)
    {
        String message = "§a" + stack.getNetworkReceiver().getHostname() + ": Ricevuto: " + packet.payload().toString();

        ServerLevel serverLevel = (ServerLevel) netEntity.getLevel();
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    @Override
    public void sendPacket(Ipv4Packet packet, NetworkStack stack)
    {
        Direction outFace = null;

        for (var entry : stack.getNetworkReceiver().getNics().entrySet())
        {
            if (entry.getValue().getIpAddress().equals(packet.sourceIp()))
            {
                outFace = entry.getKey();
                break;
            }
        }

        if (outFace == null)
        {
            // Nessuna interfaccia configurata per inviare con questo indirizzo IP
            return;
        }

        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outFace);
        Ipv4Address interfaceIp = outNic.getIpAddress();
        Ipv4Address nextHop = null;

        if (interfaceIp.equals(packet.destIp()) || packet.destIp().isIndirizzoDiLoopback())
        {
            // "Invio" il pacchetto a me stesso
            processPacket(packet, outFace, stack);

            return;
        }

        if (interfaceIp.stessaRete(packet.destIp()))
        {
            nextHop = packet.destIp();
        }
        else
        {
            if (defaultGateway == null)
            {
                // Destination Host Unreachable (nessun gateway configurato)
                MCNetworking.LOGGER.warn("Destination Host Unreachable");

                return;
            }

            nextHop = defaultGateway;
        }

        MacAddress targetMac = null;

        if (nextHop.isIndirizzoDiBroadcast(interfaceIp.getLunghezzaPrefisso()))
        {
            targetMac = MacAddress.BROADCAST;
        }
        else
        {
            targetMac = arpManager.resolveMac(nextHop);
        }

        if (targetMac == null)
        {
            int nextHopIp = nextHop.getIp();

            if (arpOutQueue.containsKey(nextHopIp))
            {
                var list = arpOutQueue.get(nextHopIp);

                list.add(packet);

                // La richiesta ARP per questo indirizzo IP è già stata mandata
                return;
            }

            List<Ipv4Packet> packets = new ArrayList<>();
            packets.add(packet);

            arpOutQueue.put(nextHopIp, packets);
            arpManager.sendArpRequest(nextHop, outFace, stack);

            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);

        stack.sendFrameOut(frame, outFace);
    }

    public Ipv4Address getDefaultGateway()
    {
        return defaultGateway;
    }

    public void setDefaultGateway(Ipv4Address defaultGateway)
    {
        this.defaultGateway = defaultGateway;
    }

    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return arpManager.getArpCache();
    }
}
