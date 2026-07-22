package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

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
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL3Engine implements L3Engine
{
    private final ArpManager arpManager = new ArpManager();
    private Ipv4Address defaultGateway = null;

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

        boolean broadcast = destIp.equals(Ipv4Address.BROADCAST) || destIp.equals(interfaceIp.getIndirizzoDiBroadcast());
        boolean perMe = destIp.equals(interfaceIp) || (from.equals(NetworkInterface.LOOPBACK_NAME) && destIp.isIndirizzoDiLoopback());

        // Il pacchetto non è destinato a me
        if (!perMe && !broadcast)
        {
            return;
        }
        if (broadcast)
        {
            if (!packet.sourceIp().contieneIp(interfaceIp) && !packet.sourceIp().equals(Ipv4Address.ALL_ZEROS))
            {
                return;
            }
        }

        NetworkPayload payload = packet.payload();

        if (payload instanceof ArpPayload arp)
        {
            arpManager.handleArp(arp, from, stack);
            return;
        }

        processChatMessage(stack, packet);

        // ... logica per ICMP, TCP, UDP, ecc.

    }

    @Override
    public void sendPacket(Ipv4Address destIp, NetworkPayload payload, NetworkStack stack)
    {
        String outName = null;
        Ipv4Address nextHop = null;

        if (destIp.isIndirizzoDiLoopback())
        {
            outName = NetworkInterface.LOOPBACK_NAME;
            nextHop = Ipv4Address.LOOPBACK;
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
                    nextHop = destIp;

                    break;
                }
            }
            try
            {
                if (outName == null)
                {
                    if (defaultGateway == null)
                    {
                        // Destination Host Unreachable (nessun gateway configurato)
                        throw new RuntimeException("Destination Host Unreachable");
                    }

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

                        if (entry.getValue().getIpAddress().contieneIp(defaultGateway))
                        {
                            // Rete di destinazione direttamente connessa
                            outName = entry.getKey();
                            nextHop = defaultGateway;
                        }
                    }
                }
                if (outName == null)
                {
                    throw new RuntimeException("Nessuna interfaccia trovata");
                }
            }
            catch (RuntimeException e)
            {
                ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
                session.sendError(e.getMessage(), e);
                return;
            }
        }

        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outName);
        Ipv4Address outIp = outNic.getIpAddress();

        Ipv4Packet packet = new Ipv4Packet(outIp, destIp, payload);

        if (nextHop.equals(outIp) || nextHop.isIndirizzoDiLoopback())
        {
            // "Invio" il pacchetto a me stesso
            processPacket(packet, outName, stack);

            return;
        }

        MacAddress targetMac;
        if (destIp.isIndirizzoDiBroadcast(nextHop.getLunghezzaPrefisso()))
        {
            targetMac = MacAddress.BROADCAST;
        }
        else
        {
            targetMac = arpManager.resolveMac(nextHop);
        }

        if (targetMac == null)
        {
            arpManager.enqueuePacket(packet, nextHop, outName, stack);
            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);
        stack.sendFrameOut(frame, outName);
    }

    public Ipv4Address getDefaultGateway()
    {
        return defaultGateway;
    }

    public void setDefaultGateway(Ipv4Address defaultGateway)
    {
        this.defaultGateway = defaultGateway;
    }

    public ArpManager getArpManager()
    {
        return arpManager;
    }
}
