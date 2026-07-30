package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

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
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL3Engine extends AbstractL3Engine
{
    private Ipv4Address defaultGateway = null;

    public EndDeviceL3Engine(NetworkingBlockEntity netEntity)
    {
        super(netEntity);
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

        if (broadcast && isInvalidBroadcast(packet, interfaceIp))
        {
            return;
        }

        NetworkPayload payload = packet.payload();

        if (payload instanceof ArpPayload arp)
        {
            arpManager.handleArp(arp, from, stack);
            return;
        }

        processChatMessage(packet, from, stack);

        // ... logica per ICMP, TCP, UDP, ecc.

    }

    @Override
    public void sendPacket(Ipv4Address destIp, NetworkPayload payload, NetworkStack stack)
    {
        // Determino interfaccia di uscita e nextHop del pacchetto

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
                int lunghezzaPrefisso = stack.getNetworkReceiver().getInterface(outName).getIpAddress().getLunghezzaPrefisso();
                nextHop = new Ipv4Address(destIp.getIp(), lunghezzaPrefisso);
            }
            else
            {
                // Inoltro al default gateway

                if (defaultGateway == null)
                {
                    // Nessun gateway configurato

                    ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
                    session.sendError(Component.translatable("mcnetworking.cli.destination_host_unreachable").getString());
                    return;
                }

                outName = getOutName(defaultGateway, stack);

                if (outName == null)
                {
                    // La rete del default gateway non è direttamente collegata a me

                    ConsoleSession session = TerminalCache.getOrCreateSession(netEntity).session();
                    session.sendError(Component.translatable("mcnetworking.cli.destination_host_unreachable").getString());
                    return;
                }

                nextHop = defaultGateway;
            }
        }

        sendPacketOut(destIp, payload, nextHop, outName, stack);
    }

    public Ipv4Address getDefaultGateway()
    {
        return defaultGateway;
    }

    public void setDefaultGateway(Ipv4Address defaultGateway)
    {
        this.defaultGateway = defaultGateway;
    }
}
