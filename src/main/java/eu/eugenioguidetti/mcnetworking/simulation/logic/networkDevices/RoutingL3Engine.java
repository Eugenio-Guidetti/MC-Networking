package eu.eugenioguidetti.mcnetworking.simulation.logic.networkDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.IcmpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.DEFAULT_TTL;
import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public class RoutingL3Engine extends AbstractL3Engine
{
    private final RoutingTable routingTable;

    public RoutingL3Engine(AbstractL3NetworkingBlockEntity l3netEntity)
    {
        super(l3netEntity);

        this.routingTable = new RoutingTable(l3netEntity);
    }

    @Override
    public void processPacket(@NonNull Ipv4Packet packet, @NonNull String from)
    {
        if (!shouldProcessPacket(packet, from))
        {
            return;
        }

        // Pacchetto destinato al Router

        routePacket(packet, from);
    }

    private void routePacket(@NonNull Ipv4Packet packet, String from)
    {
        // Esegui routing

        RoutingTable.Route route = routingTable.routePacket(packet.destIp());

        if (route == null)
        {
            sendPayload(IcmpPayload.destinationNetworkUnreachable(packet), packet.sourceIp(), DEFAULT_TTL);

            return;
        }

        // Hairpinning Il pacchetto deve uscire dall'interfaccia da cui è entrato
        if (from.equals(route.nicName()))
        {
            MCNetworking.LOGGER.info("Hairpinning. TTL: {}", packet.ttl());

            //return;
        }

        if (route.type().equals(RoutingTable.RouteType.L))
        {
            Ipv4Address destIp = packet.destIp();
            Ipv4CidrAddress nicIp = l3netEntity.getInterface(from).getIpAddress();
            Ipv4CidrAddress routeNicIp = l3netEntity.getInterface(route.nicName()).getIpAddress();

            if (!nicIp.contieneIp(packet.sourceIp()))
            {
                return;
            }

            if (destIp.equals(Ipv4Address.BROADCAST) || destIp.equals(routeNicIp.address()) || destIp.equals(routeNicIp
                                                                                                                     .getIndirizzoDiBroadcast()
                                                                                                                     .address()) || destIp.isLoopback())
            {
                // Pacchetto destinato al router
                handleLocalPayload(packet, route.nicName());
            }
            return;
        }


        if (packet.ttl() <= 1)
        {
            sendPayload(IcmpPayload.timeExceeded(packet), packet.sourceIp(), DEFAULT_TTL);

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

        sendPacket(packet.decreaseTtl(), new OutPacketData(nextHop, outName));
    }


    @Override
    protected void handleHigherLayerPayload(Ipv4Packet packet, String from)
    {
        processChatMessage(packet, from);

        // TODO: I router scartano i messaggi applicativi, tranne SSH e Telnet, che verranno gestiti qui
    }

    @Override
    protected @Nullable OutPacketData findOutPacketData(@NonNull Ipv4Address destIp) throws IllegalArgumentException
    {
        if (destIp.isAllZeros() || destIp.isBroadcast())
        {
            return null;
        }

        if (destIp.isLoopback())
        {
            return new AbstractL3Engine.OutPacketData(Ipv4Address.LOOPBACK, LOOPBACK_NAME);
        }

        RoutingTable.Route route = routingTable.routePacket(destIp);

        if (route == null)
        {
            return null;
        }

        if (route.type().equals(RoutingTable.RouteType.L) || route.type().equals(RoutingTable.RouteType.C))
        {
            return new OutPacketData(destIp, route.nicName());
        }

        return new OutPacketData(route.nextHop(), route.nicName());
    }


    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
    }
}