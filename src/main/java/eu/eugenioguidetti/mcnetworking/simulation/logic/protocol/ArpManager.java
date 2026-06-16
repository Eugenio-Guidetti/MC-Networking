package eu.eugenioguidetti.mcnetworking.simulation.logic.protocol;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpManager
{
    private final Map<Ipv4Address, MacAddress> arpCache = new HashMap<>();


    public int handleArp(ArpPayload arp, Direction from, NetworkStack stack)
    {
        Ipv4Address interfaceIp = stack.getNetworkReceiver().getInterface(from).getIpAddress();

        // Richiesta ARP rivolta a me
        if (arp.operation() == ArpPayload.OPERATION_ARP_REQUEST && arp.targetIp().equals(interfaceIp))
        {
            MacAddress interfaceMac = stack.getNetworkReceiver().getInterface(from).getMacAddress();

            ArpPayload arpReply = new ArpPayload(interfaceMac,
                                                 interfaceIp,
                                                 arp.senderMac(),
                                                 arp.senderIp(),
                                                 ArpPayload.OPERATION_ARP_REPLY);

            Ipv4Packet replyPacket = new Ipv4Packet(interfaceIp, arp.senderIp(), arpReply);
            EthernetFrame replyFrame = new EthernetFrame(interfaceMac, arp.senderMac(), replyPacket);

            stack.sendFrameOut(replyFrame, from);
        }
        else if (arp.operation() == ArpPayload.OPERATION_ARP_REPLY)
        {
            arpCache.put(arp.senderIp(), arp.senderMac());
        }

        return arp.operation();
    }

    public MacAddress resolveMac(Ipv4Address ip)
    {
        return arpCache.get(ip);
    }

    public void sendArpRequest(Ipv4Address targetIp, Direction outFace, NetworkStack stack)
    {
        NetworkInterface nic = stack.getNetworkReceiver().getInterface(outFace);
        ArpPayload arp = new ArpPayload(nic.getMacAddress(),
                                        nic.getIpAddress(),
                                        MacAddress.ALL_ZEROS,
                                        targetIp,
                                        ArpPayload.OPERATION_ARP_REQUEST);

        Ipv4Packet packet = new Ipv4Packet(nic.getIpAddress(), targetIp, arp);
        EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), MacAddress.BROADCAST, packet);

        stack.sendFrameOut(frame, outFace);
    }

    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return arpCache;
    }
}
