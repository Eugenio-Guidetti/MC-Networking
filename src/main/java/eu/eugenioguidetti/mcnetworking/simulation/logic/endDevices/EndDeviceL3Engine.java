package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL3Engine extends AbstractL3Engine
{
    private Ipv4Address defaultGateway = null;

    public EndDeviceL3Engine(AbstractL3NetworkingBlockEntity netEntity)
    {
        super(netEntity);
    }


    @Override
    public void processPacket(@NonNull Ipv4Packet packet, @NonNull String from)
    {
        if (!shouldProcessPacket(packet, from))
        {
            return;
        }

        Ipv4Address destIp = packet.destIp();
        Ipv4CidrAddress nicIp = l3netEntity.getInterface(from).getIpAddress();

        // Controlla se destinato all'end device
        if (destIp.equals(Ipv4Address.BROADCAST) || destIp.equals(nicIp.address()) || destIp.equals(nicIp
                                                                                                            .getIndirizzoDiBroadcast()
                                                                                                            .address()) || (destIp.isLoopback() && from.equals(
                LOOPBACK_NAME)))
        {
            handleLocalPayload(packet, from);
        }
    }

    @Override
    protected void handleHigherLayerPayload(Ipv4Packet packet, String from)
    {
        processChatMessage(packet, from);

        // TODO: passare il payload ai livelli superiori
    }

    /**
     *
     * @return null se non è possibile determinare/raggiungere il next hop
     */
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

        // Controllo se il destinatario è in una rete direttamente connessa a me

        OutPacketData outPacketData = Utils.getOutPacketData(destIp, l3netEntity.getNics().values());

        if (outPacketData != null)
        {
            return outPacketData;
        }

        // Inoltro al default gateway

        if (defaultGateway == null || defaultGateway.isAllZeros())
        {
            // default gateway non configurato

            return null;
        }

        return Utils.getOutPacketData(defaultGateway, l3netEntity.getNics().values());
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
