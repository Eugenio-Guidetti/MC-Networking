package eu.eugenioguidetti.mcnetworking.simulation.logic;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 27/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;
import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractL3Engine implements L3Engine
{
    protected final AbstractL3NetworkingBlockEntity l3netEntity;


    public AbstractL3Engine(AbstractL3NetworkingBlockEntity l3netEntity)
    {
        this.l3netEntity = l3netEntity;
    }


    // Ricezione pacchetti

    /**
     * @param packet il pacchetto in arrivo
     * @param from   il nome dell'interfaccia in cui è entrato il pacchetto
     *               <br>
     *               Un device L3 analizza un pacchetto {@code packet} se:
     *               <ol>
     *               <li>
     *                              Gli indirizzi di sorgente e destinazione sono di loopback e il pacchetto è entrato dall'interfaccia di loopback
     *               </li>
     *               <li>
     *                              L'indirizzo ip del destinatario è uguale all'indirizzo ip di un'interfaccia non di loopback connessa e configurata
     *               </li>
     *               <li>
     *                              L'indirizzo ip del destinatario è uguale all'indirizzo ip di broadcast della rete di un'interfaccia non di loopback connessa e configurata
     *               </li>
     *               <li>
     *                              L'indirizzo ip del destinatario è uguale all'indirizzo di limited broadcast {@code 255.255.255.255}
     *               </li>
     *               </ol>
     *               <br>
     *               (Gli host hanno al massimo una sola interfaccia non di loopback connessa e configurata)
     */
    protected boolean shouldProcessPacket(@NonNull Ipv4Packet packet, @NonNull String from)
    {
        Ipv4Address sourceIp = packet.sourceIp();
        Ipv4Address destIp = packet.destIp();

        // I pacchetti di loopback devono avere come indirizzo di sorgente e destinazione un indirizzo di loopback e devono entrare dall'interfaccia di loopback
        if ((sourceIp.isLoopback() && destIp.isLoopback()) ^ from.equals(LOOPBACK_NAME))
        {
            return false;
        }
        if (sourceIp.isLoopback())
        {
            return true;
        }

        if (!sourceIp.isBroadcast())
        {
            return true;
        }


        for (NetworkInterface nic : l3netEntity.getNics().values())
        {
            if (nic.isLoopback() || !nic.isConnected() || nic.getIpAddress().equals(Ipv4CidrAddress.ALL_ZEROS))
            {
                continue;
            }

            Ipv4CidrAddress interfaceIp = nic.getIpAddress();
            Ipv4Address networkBroadcastIp = interfaceIp.getIndirizzoDiBroadcast().address();

            if (sourceIp.equals(interfaceIp.address()))
            {
                return true;
            }
            else if (!sourceIp.equals(networkBroadcastIp))
            {
                return true;
            }
        }

        return false;
    }


    protected void handleLocalPayload(@NonNull Ipv4Packet packet, String from)
    {
        NetworkPayload payload = packet.payload();

        if (payload instanceof IcmpPayload icmp)
        {
            l3netEntity.getIcmpManager().handleIcmp(icmp, packet, from);
            return;
        }

        // Delego il payload ai livelli superiori
        handleHigherLayerPayload(packet, from);
    }


    protected abstract void handleHigherLayerPayload(Ipv4Packet packet, String from);


    protected void processChatMessage(@NonNull Ipv4Packet packet, String from)
    {
        String message = "§a" + l3netEntity.getHostname() + ":" + from + ": Ricevuto: " + packet.payload();

        ServerLevel serverLevel = (ServerLevel) l3netEntity.getLevel();

        if (serverLevel == null)
        {
            return;
        }

        serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }


    // Invio pacchetti

    protected abstract @Nullable OutPacketData findOutPacketData(@NonNull Ipv4Address destIp) throws IllegalArgumentException;

    @Override
    public void sendPayload(NetworkPayload payload, Ipv4Address destIp, int ttl)
    {
        // Il payload qui origina sempre da questo dispositivo L3

        // Determino outPacektData
        OutPacketData outPacketData = findOutPacketData(destIp);

        if (outPacketData == null)
        {
            // Non so a chi mandare il pacchetto
            Ipv4Packet abortedPacket = new Ipv4Packet(Ipv4Address.LOOPBACK, destIp, ttl, payload);

            sendPayload(IcmpPayload.destinationNetworkUnreachable(abortedPacket), Ipv4Address.LOOPBACK, DEFAULT_TTL);

            return;
        }

        // Determino ipSorgente
        Ipv4Address nextHop = outPacketData.nextHop();
        String outName = outPacketData.outNicName();

        NetworkInterface outNic = l3netEntity.getInterface(outName);
        Ipv4Address outIp = outNic.getIpAddress().address();

        // Preparo il pacchetto
        Ipv4Packet packet = new Ipv4Packet(outIp, destIp, ttl, payload);

        // Invio il pacchetto
        sendPacket(packet, outPacketData);
    }

    @Override
    public void sendPacket(@NonNull Ipv4Packet packet)
    {
        // Qui potrebbe anche trattarsi di un pacchetto in transito

        // Trovo nextHop e outNic
        OutPacketData outPacketData = findOutPacketData(packet.destIp());
        sendPacket(packet, outPacketData);
    }

    @Override
    public void sendPacket(@NonNull Ipv4Packet packet, OutPacketData outPacketData)
    {
        // Qui potrebbe anche trattarsi di un pacchetto in transito, attraverso questo device

        Ipv4Address sourceIp = packet.sourceIp();
        Ipv4Address destIp = packet.destIp();

        if (destIp.isBroadcast())
        {
            sendToBroadcast(packet);

            return;
        }

        if (destIp.equals(Ipv4Address.ALL_ZEROS))
        {
            throw new IllegalArgumentException("destIp mancante");
        }

        if (sourceIp.isLoopback() ^ sourceIp.isLoopback())
        {
            throw new IllegalArgumentException("loopback invalido");
        }
        if (destIp.isLoopback() ^ sourceIp.isLoopback())
        {
            throw new IllegalArgumentException("loopback invalido");
        }

        // Non so a chi mandare il pacchetto
        if (outPacketData == null)
        {
            sendPayload(IcmpPayload.destinationNetworkUnreachable(packet), sourceIp, DEFAULT_TTL);

            return;
        }

        // "Invio" il pacchetto a me stesso
        if (outPacketData.nextHop.isLoopback() || outPacketData.nextHop.equals(sourceIp))
        {
            processPacket(packet, outPacketData.outNicName());

            return;
        }

        // Incapsulo e mando al livello 2
        dispatchToL2(packet, outPacketData);
    }


    protected void dispatchToL2(Ipv4Packet packet, @NonNull OutPacketData outPacketData)
    {
        // Preparo un frame da mandare al livello 2

        Ipv4Address nextHop = outPacketData.nextHop();
        String outName = outPacketData.outNicName();

        NetworkInterface outNic = l3netEntity.getInterface(outName);
        Ipv4CidrAddress outIp = outNic.getIpAddress();


        // Non puoi inviare pacchetti all'indirizzo di rete della tua rete
        if (nextHop.equals(outIp.getIndirizzoDiRete().address()))
        {
            sendPayload(IcmpPayload.destinationHostUnreachable(packet), Ipv4Address.LOOPBACK, DEFAULT_TTL);

            return;
        }


        MacAddress targetMac;

        if (nextHop.equals(outIp.getIndirizzoDiBroadcast().address()))
        {
            targetMac = MacAddress.BROADCAST;
        }
        else
        {
            targetMac = l3netEntity.getArpManager().resolveMac(nextHop);
        }

        if (targetMac == null)
        {
            l3netEntity.getArpManager().enqueuePacket(packet, nextHop, outName);
            return;
        }

        EthernetFrame frame = new EthernetFrame(outNic.getMacAddress(), targetMac, packet);
        l3netEntity.getStack().sendFrame(frame, outName);
    }

    protected void sendToBroadcast(Ipv4Packet packet)
    {
        for (NetworkInterface nic : l3netEntity.getNics().values())
        {
            if (nic.isLoopback() || !nic.isConnected())
            {
                continue;
            }

            EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), MacAddress.BROADCAST, packet);
            l3netEntity.getStack().sendFrame(frame, nic.getName());
        }
    }

    /**
     * Rappresenta le informazioni
     *
     * @param nextHop:    serve a determinare l'indirizzo MAC di destinazione tramite l'ARP manager
     * @param outNicName: il nome dell'interfaccia da cui uscirà il pacchetto
     */
    public record OutPacketData(@NonNull Ipv4Address nextHop, @NonNull String outNicName)
    {
    }
}
