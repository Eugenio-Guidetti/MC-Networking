package eu.eugenioguidetti.mcnetworking.simulation.logic.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpManager
{
    public static final int QUEUE_TIMEOUT_TICKS = 50 * 20;

    private final Map<Ipv4Address, MacAddress> arpCache = new HashMap<>();

    // Quando non so l'indirizzo MAC associato all'indirizzo IP del destinatario metto il pacchetto destinato a lui in coda per inviarlo all'arrivo della ARP reply
    private final Map<Ipv4Address, ArpQueueEntry> arpOutQueue = new HashMap<>();

    public void handleArp(ArpPayload arp, String from, NetworkStack stack)
    {
        if (arp.operation() == ArpPayload.OPERATION_ARP_REQUEST)
        {
            MacAddress interfaceMac = stack.getNetworkReceiver().getInterface(from).getMacAddress();
            Ipv4Address interfaceIp = stack.getNetworkReceiver().getInterface(from).getIpAddress();

            // Caching opportunistico
            arpCache.put(arp.senderIp(), arp.senderMac());

            // Richiesta ARP rivolta a me
            if (arp.targetIp().equals(interfaceIp))
            {
                EthernetFrame replyFrame = createReplyFrame(arp, interfaceMac, interfaceIp);
                stack.sendFrameOut(replyFrame, from);
            }
        }
        else if (arp.operation() == ArpPayload.OPERATION_ARP_REPLY)
        {
            Ipv4Address resolvedIp = arp.senderIp();
            arpCache.put(resolvedIp, arp.senderMac());

            ArpQueueEntry entry = arpOutQueue.remove(resolvedIp);

            if (entry != null)
            {
                for (Ipv4Packet outPacket : entry.packets)
                {
                    stack.sendPacket(outPacket.destIp(), outPacket.payload());
                }
            }
        }
    }

    private EthernetFrame createReplyFrame(ArpPayload arp, MacAddress interfaceMac, Ipv4Address interfaceIp)
    {
        ArpPayload arpReply = new ArpPayload(interfaceMac, interfaceIp, arp.senderMac(), arp.senderIp(), ArpPayload.OPERATION_ARP_REPLY);
        Ipv4Packet replyPacket = new Ipv4Packet(interfaceIp, arp.senderIp(), arpReply);
        return new EthernetFrame(interfaceMac, arp.senderMac(), replyPacket);
    }

    public MacAddress resolveMac(Ipv4Address ip)
    {
        return arpCache.get(ip);
    }

    public void sendArpRequest(Ipv4Address targetIp, String outName, NetworkStack stack)
    {
        NetworkInterface nic = stack.getNetworkReceiver().getInterface(outName);
        ArpPayload arp = new ArpPayload(nic.getMacAddress(),
                                        nic.getIpAddress(),
                                        MacAddress.ALL_ZEROS,
                                        targetIp,
                                        ArpPayload.OPERATION_ARP_REQUEST);

        Ipv4Packet packet = new Ipv4Packet(nic.getIpAddress(), targetIp, arp);
        EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), MacAddress.BROADCAST, packet);

        stack.sendFrameOut(frame, outName);
    }

    public void enqueuePacket(Ipv4Packet packet, Ipv4Address nextHop, String outName, NetworkStack stack)
    {
        if (arpOutQueue.containsKey(nextHop))
        {
            arpOutQueue.get(nextHop).packets.add(packet);

            // La richiesta ARP per questo indirizzo IP è già stata mandata
            return;
        }

        ArpQueueEntry entry = new ArpQueueEntry(QUEUE_TIMEOUT_TICKS);
        entry.packets.add(packet);

        arpOutQueue.put(nextHop, entry);
        this.sendArpRequest(nextHop, outName, stack);
    }

    public void tick(NetworkingBlockEntity netEntity)
    {
        if (arpOutQueue.isEmpty())
        {
            return;
        }

        var iterator = arpOutQueue.entrySet().iterator();

        while (iterator.hasNext())
        {
            var entry = iterator.next();

            // Se il timer arriva a zero, il pacchetto scade (timeout ARP)
            if (entry.getValue().tickDown())
            {
                ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
                session.sendError(String.format(Component.translatable("mcnetworking.cli.arp_request_timeout_format").getString(),
                                                entry.getKey()));

                iterator.remove();
            }
        }
    }

    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return arpCache;
    }


    private static class ArpQueueEntry
    {
        private final List<Ipv4Packet> packets = new ArrayList<>();
        private int timeoutTicks;

        public ArpQueueEntry(int timeoutTicks)
        {
            this.timeoutTicks = timeoutTicks;
        }

        // Decrementa il timer e restituisce true se il timeout è scaduto
        public boolean tickDown()
        {
            this.timeoutTicks--;
            return this.timeoutTicks <= 0;
        }
    }
}
