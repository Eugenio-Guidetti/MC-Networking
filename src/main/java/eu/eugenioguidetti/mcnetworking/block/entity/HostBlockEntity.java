package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.block.custom.HostBlock;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices.EndDeviceL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices.EndDeviceL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ApplicationPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class HostBlockEntity extends NetworkingBlockEntity
{
    private final EndDeviceL2Engine l2Engine = new EndDeviceL2Engine();
    private final EndDeviceL3Engine l3Engine = new EndDeviceL3Engine();

    @Nullable
    private Ipv4Address dnsServer = null;

    public HostBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.HOST_BLOCK_ENTITY, pos, state);

        this.stack.setL2Engine(l2Engine);
        this.stack.setL3Engine(l3Engine);

        l2Engine.setNetEntity(this);
        l3Engine.setNetEntity(this);

        hostname = "Host";

        Direction facing = state.getValue(HostBlock.HORIZONTAL_FACING);
        nics.put(facing, new NetworkInterface(pos, facing, ConnectorType.RJ45));
    }

    public void triggerSendPacket(Ipv4Address destIp, String message)
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        // Invia il pacchetto alle interfacce
        for (NetworkInterface nic : this.nics.values())
        {
            if (!nic.isConnected())
            {
                continue;
            }

            String payload = String.format("""
                                                   PING§7
                                                   Sender nic:
                                                      MAC: %s
                                                      pos: %s
                                                      dir: %s
                                                      connType: %s
                                                   Receiver nic:
                                                      pos: %s
                                                      dir: %s
                                                   CableType: %s""",
                                           nic.getMacAddress(),
                                           nic.getPos().toShortString(),
                                           nic.getDirection(),
                                           nic.getConnectorType(),
                                           nic.getConnectedTargetPos().toShortString(),
                                           nic.getConnectedTargetFace(),
                                           nic.getConnectedCableType().name());

            Ipv4Packet packet = new Ipv4Packet(nic.getIpAddress(), destIp, new ApplicationPayload(message));

            stack.sendPacket(packet);
        }
    }


    @Override
    public int getDeviceLayer()
    {
        return 7;
    }


    public Map<Ipv4Address, MacAddress> getArpCache()
    {
        return l3Engine.getArpCache();
    }


    // --- Salvataggio/caricamento defaultGateway e dnsServer in NBT ---

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        if (l3Engine.getDefaultGateway() != null)
        {
            output.putString("DefaultGateway", l3Engine.getDefaultGateway().toString());
        }
        if (this.dnsServer != null)
        {
            output.putString("DnsServer", this.dnsServer.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        this.l3Engine.setDefaultGateway(input.getString("DefaultGateway").map(Ipv4Address::new).orElse(null));
        this.dnsServer = input.getString("DnsServer").map(Ipv4Address::new).orElse(null);
    }
}
