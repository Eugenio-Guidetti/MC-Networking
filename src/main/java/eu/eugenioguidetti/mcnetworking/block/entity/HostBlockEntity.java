package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.block.custom.HostBlock;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.item.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.ApplicationPayload;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.Ipv4Packet;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.TcpSegment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class HostBlockEntity extends NetworkingBlockEntity
{
    public HostBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.HOST_BLOCK_ENTITY, pos, state);

        Direction facing = state.getValue(HostBlock.HORIZONTAL_FACING);

        nics.put(facing, new NetworkInterface(pos, facing, ConnectorType.RJ45));
    }

    // Metodo chiamato dal Blocco quando sente la Redstone
    public void triggerSendPacket()
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

            ApplicationPayload appData = new ApplicationPayload(payload);
            TcpSegment tcp = new TcpSegment(45000, 80, appData);
            Ipv4Packet ip = new Ipv4Packet(new Ipv4Address("192.168.1.1"), new Ipv4Address("10.0.0.1"), tcp);
            EthernetFrame frame = new EthernetFrame(nic.getMacAddress(), MacAddress.BROADCAST, ip);

            nic.sendPacket(frame);
        }
    }

    @Override
    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull Direction from)
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }
        NetworkInterface receivingNic = nics.get(from);
        if (receivingNic == null || !receivingNic.isConnected())
        {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level;

        String message = null;
        int color;

        if (frame.destMac().equals(MacAddress.BROADCAST) || frame.destMac().equals(receivingNic.getMacAddress()))
        {
            // Per ora alla ricezione di un frame, questo viene stampato in chat
            message = String.format("§a[Host NIC at %s:%s, MAC: %s] Ricevuto frame!\nPayload: '%s'",
                                    receivingNic.getPos().toShortString(),
                                    receivingNic.getDirection(),
                                    receivingNic.getMacAddress(),
                                    frame.getDisplayString());

            color = ARGB.color(0, 255, 0); // Verde
        }
        else
        {
            message = String.format("§c[Host NIC at %s:%s, MAC: %s] Scartato frame!\nPayload: '%s'",
                                    receivingNic.getPos().toShortString(),
                                    receivingNic.getDirection(),
                                    receivingNic.getMacAddress(),
                                    frame.getDisplayString());

            color = ARGB.color(255, 0, 0); // Rosso
        }

        serverLevel.sendParticles(new DustParticleOptions(color, 1.5f), // color, scale
                                  this.getBlockPos().getX() + 0.5,
                                  this.getBlockPos().getY() + 1.1,
                                  this.getBlockPos().getZ() + 0.5,
                                  1,  // count
                                  0.1, // delta X
                                  0, // delta Y
                                  0.1, // delta Z
                                  0.1  // speed
        );

        this.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
