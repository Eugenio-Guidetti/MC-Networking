package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 27/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractL3Engine implements L3Engine
{
    protected final NetworkingBlockEntity netEntity;
    protected final ArpManager arpManager = new ArpManager();


    public AbstractL3Engine(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }


    protected void processChatMessage(Ipv4Packet packet, String from, NetworkStack stack)
    {
        String message = "§a" + stack.getNetworkReceiver().getHostname() + ":" + from + ": Ricevuto: " + packet.payload();

        ServerLevel serverLevel = (ServerLevel) netEntity.getLevel();
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    /**
     * Cerca se l'indirizzo IP targetIp appartiene a una rete direttamente connessa a una delle interfacce.
     *
     * @return Il nome dell'interfaccia oppure null se non trovata.
     */
    protected String getOutName(Ipv4Address targetIp, NetworkStack stack)
    {
        for (var entry : stack.getNetworkReceiver().getNics().entrySet())
        {
            // Salta interfacce non configurate e di loopback
            if (entry.getKey().equals(NetworkInterface.LOOPBACK_NAME) || entry.getValue().getIpAddress().equals(Ipv4Address.ALL_ZEROS))
            {
                continue;
            }

            if (entry.getValue().getIpAddress().contieneIp(targetIp))
            {
                // Rete di destinazione direttamente connessa
                return entry.getKey();
            }
        }

        return null;
    }

    protected void sendPacketOut(Ipv4Address destIp, NetworkPayload payload, Ipv4Address nextHopIp, String outName, NetworkStack stack)
    {
        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outName);
        Ipv4Address outIp = outNic.getIpAddress();

        Ipv4Packet packet = new Ipv4Packet(outIp, destIp, payload);


        if (nextHopIp.equals(outIp) || nextHopIp.isIndirizzoDiLoopback())
        {
            // "Invio" il pacchetto a me stesso
            processPacket(packet, outName, stack);

            return;
        }

        dispatchToL2(packet, nextHopIp, outName, stack);
    }

    protected void dispatchToL2(Ipv4Packet packet, Ipv4Address nextHopIp, String outName, NetworkStack stack)
    {
        NetworkInterface outNic = stack.getNetworkReceiver().getInterface(outName);

        MacAddress targetMac;
        if (nextHopIp.isIndirizzoDiBroadcast())
        {
            targetMac = MacAddress.BROADCAST;
        }
        else
        {
            targetMac = arpManager.resolveMac(nextHopIp);
        }

        if (targetMac == null)
        {
            arpManager.enqueuePacket(packet, nextHopIp, outName, stack);
            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);
        stack.sendFrameOut(frame, outName);
    }

    protected boolean isInvalidBroadcast(Ipv4Packet packet, Ipv4Address interfaceIp)
    {
        return !packet.sourceIp().contieneIp(interfaceIp) && !packet.sourceIp().equals(Ipv4Address.ALL_ZEROS);
    }


    public ArpManager getArpManager()
    {
        return arpManager;
    }
}
