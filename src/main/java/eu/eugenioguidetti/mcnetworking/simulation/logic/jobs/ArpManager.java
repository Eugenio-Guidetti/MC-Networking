package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpManager extends Job
{
    public static final int QUEUE_TIMEOUT_TICKS = 50 * 20;
    public static final int ARP_CACHE_ENTRY_LIFESPAN_TICKS = 5 * 60 * 20; // 5 min

    // Quando non so l'indirizzo MAC associato all'indirizzo IP del destinatario metto il pacchetto destinato a lui in coda per inviarlo all'arrivo della ARP reply
    private final Map<Ipv4Address, ArpQueueEntry> arpOutQueue = new HashMap<>();
    private final Map<Ipv4Address, ArpCacheEntry> arpCache;

    private final AbstractL3NetworkingBlockEntity l3NetEntity;

    public ArpManager(AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        super(l3NetEntity);

        arpCache = new HashMap<>();

        this.l3NetEntity = l3NetEntity;
    }

    public void handleArp(@NonNull ArpPayload arp, String from)
    {
        if (arp.operation() == ArpPayload.OPERATION_ARP_REQUEST)
        {
            MacAddress interfaceMac = l3NetEntity.getInterface(from).getMacAddress();
            Ipv4CidrAddress interfaceIp = l3NetEntity.getInterface(from).getIpAddress();

            // GratuitousArpRequest con ipSorgente 0.0.0.0 -> rimuovi macSorgente dalla arpCache
            if (arp.senderIp().isAllZeros())
            {
                for (var entry : arpCache.entrySet())
                {
                    if (arp.senderMac().equals(entry.getValue().getTargetMac()))
                    {
                        arpCache.remove(entry.getKey());
                        break;
                    }
                }

                return;
            }

            if (!interfaceIp.contieneIp(arp.senderIp()))
            {
                return;
            }

            // Caching opportunistico
            addEntry(arp.senderIp(), arp.senderMac());

            // Richiesta ARP per me
            if (arp.targetIp().equals(interfaceIp.address()))
            {
                EthernetFrame replyFrame = createReplyFrame(arp, interfaceMac, interfaceIp.address());
                l3NetEntity.getStack().sendFrame(replyFrame, from);
            }
        }
        else if (arp.operation() == ArpPayload.OPERATION_ARP_REPLY)
        {
            Ipv4Address resolvedIp = arp.senderIp();
            addEntry(resolvedIp, arp.senderMac());

            ArpQueueEntry entry = arpOutQueue.remove(resolvedIp);

            if (entry != null)
            {
                for (Ipv4Packet outPacket : entry.packets)
                {
                    EthernetFrame frame = new EthernetFrame(arp.targetMac(), arp.senderMac(), outPacket);
                    l3NetEntity.getStack().sendFrame(frame, from);
                }
            }
        }
    }

    private void addEntry(@NonNull Ipv4Address ip, @NonNull MacAddress mac)
    {
        arpCache.values().removeIf(arpCacheEntry -> arpCacheEntry.getTargetMac().equals(mac));

        arpCache.put(ip, new ArpCacheEntry(mac));
    }

    private @NonNull EthernetFrame createReplyFrame(@NonNull ArpPayload arp, MacAddress interfaceMac, Ipv4Address interfaceIp)
    {
        ArpPayload arpReply = new ArpPayload(interfaceMac, interfaceIp, arp.senderMac(), arp.senderIp(), ArpPayload.OPERATION_ARP_REPLY);
        return new EthernetFrame(interfaceMac, arp.senderMac(), arpReply);
    }

    public Optional<MacAddress> resolveMac(Ipv4Address ip)
    {
        if (!arpCache.containsKey(ip))
        {
            return Optional.empty();
        }

        return Optional.of(arpCache.get(ip).getTargetMac());
    }

    public void sendArpRequest(@NonNull Ipv4Address targetIp, @NonNull String outName)
    {
        NetworkInterface nic = l3NetEntity.getInterface(outName);
        Ipv4Address interfaceIp = nic.getIpAddress().address();

        ArpPayload arp = new ArpPayload(nic.getMacAddress(), interfaceIp, MacAddress.ALL_ZEROS, targetIp, ArpPayload.OPERATION_ARP_REQUEST);

        EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), MacAddress.BROADCAST, arp);

        l3NetEntity.getStack().sendFrame(frame, outName);
    }

    public void sendGratuitousArpRequest(@NonNull String outName)
    {
        NetworkInterface nic = l3NetEntity.getInterface(outName);
        Ipv4Address interfaceIp = nic.getIpAddress().address();

        sendArpRequest(interfaceIp, outName);
    }

    public void enqueuePacket(@NonNull Ipv4Packet packet, @NonNull Ipv4Address nextHop, String outName)
    {
        NetworkInterface nic = l3NetEntity.getInterface(outName);
        Ipv4CidrAddress interfaceCidrIp = nic.getIpAddress();


        // nextHop invalido
        if (nextHop.isLoopback() || nextHop.equals(Ipv4Address.ALL_ZEROS) || nextHop.equals(Ipv4Address.BROADCAST) || nextHop.equals(
                interfaceCidrIp.getIndirizzoDiRete().address()) || nextHop.equals(interfaceCidrIp.getIndirizzoDiBroadcast().address()))
        {
            l3NetEntity.getStack().sendL3Payload(IcmpPayload.destinationHostUnreachable(packet), Ipv4Address.LOOPBACK);

            return;
        }

        if (arpOutQueue.containsKey(nextHop))
        {
            arpOutQueue.get(nextHop).packets.add(packet);

            // La richiesta ARP per questo indirizzo IP è già stata mandata
            return;
        }

        ArpQueueEntry entry = new ArpQueueEntry(QUEUE_TIMEOUT_TICKS);
        entry.packets.add(packet);

        arpOutQueue.put(nextHop, entry);
        this.sendArpRequest(nextHop, outName);
    }

    @Override
    protected boolean internalTick()
    {
        arpCache.values().removeIf(ArpCacheEntry::decreaseTtl);

        if (arpOutQueue.isEmpty())
        {
            // Il job ArpManager non termina mai
            return false;
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

                List<Ipv4Packet> pacchettiScartati = new ArrayList<>(entry.getValue().packets);

                iterator.remove();

                for (Ipv4Packet packet : pacchettiScartati)
                {
                    IcmpPayload icmp = IcmpPayload.destinationHostUnreachable(new Ipv4Packet(packet.sourceIp(),
                                                                                             entry.getKey(),
                                                                                             packet.ttl(),
                                                                                             packet.payload()));

                    netEntity.getStack().sendL3Payload(icmp, packet.sourceIp());
                }
            }
        }

        // Il job ArpManager non termina mai
        return false;
    }

    public Map<Ipv4Address, ArpCacheEntry> getArpCache()
    {
        return arpCache;
    }

    public void clearCache()
    {
        arpOutQueue.clear();
        arpCache.clear();
    }

    public static class ArpCacheEntry
    {
        private final MacAddress targetMac;
        private int ticksRemaining;

        private ArpCacheEntry(MacAddress targetMac)
        {
            this.targetMac = targetMac;
            this.ticksRemaining = ARP_CACHE_ENTRY_LIFESPAN_TICKS;
        }

        public ArpCacheEntry(MacAddress targetMac, int ticksRemaining)
        {
            this.targetMac = targetMac;
            this.ticksRemaining = ticksRemaining;
        }

        protected boolean decreaseTtl()
        {
            return ticksRemaining != -1 && --ticksRemaining <= 0;
        }

        protected void resetTtl()
        {
            ticksRemaining = ARP_CACHE_ENTRY_LIFESPAN_TICKS;
        }

        public MacAddress getTargetMac()
        {
            return targetMac;
        }

        public int getTicksRemaining()
        {
            return ticksRemaining;
        }
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
