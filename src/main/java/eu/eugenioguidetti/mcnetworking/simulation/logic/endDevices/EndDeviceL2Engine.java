package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ArpPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL2Engine extends AbstractL2Engine
{
    private final AbstractL3NetworkingBlockEntity l3NetEntity;

    public EndDeviceL2Engine(AbstractL3NetworkingBlockEntity l3NetEntity)
    {
        super(l3NetEntity);
        this.l3NetEntity = l3NetEntity;
    }


    @Override
    public void processFrame(@NonNull EthernetFrame frame, @NonNull String from)
    {
        processColors(frame, from);

        MacAddress interfaceMac = l3NetEntity.getInterface(from).getMacAddress();

        // Il frame non è rivolto all'end device
        if (!frame.destMac().equals(interfaceMac) && !frame.destMac().equals(MacAddress.BROADCAST))
        {
            return;
        }

        if (frame.payload() instanceof ArpPayload arp)
        {
            l3NetEntity.getArpManager().handleArp(arp, from);
            return;
        }

        if (frame.payload() instanceof Ipv4Packet packet)
        {
            // Passa il payload IP al livello superiore
            l3NetEntity.getStack().receivePacket(packet, from);
        }
    }

    private void processColors(@NonNull EthernetFrame frame, String from)
    {
        MacAddress interfaceMac = l3NetEntity.getInterface(from).getMacAddress();

        int color = 0;

        if (frame.payload() instanceof ArpPayload arp)
        {
            color = ARGB.color(0, 0, 255); // Blu
        }
        else if (frame.destMac().equals(MacAddress.BROADCAST))
        {
            color = ARGB.color(255, 127, 0); // Arancione
        }
        else if (!frame.destMac().equals(interfaceMac))
        {
            color = ARGB.color(255, 0, 0); // Rosso
        }
        else if (frame.payload() instanceof Ipv4Packet packet)
        {
            color = ARGB.color(0, 255, 0); // Verde
        }


        ServerLevel serverLevel = (ServerLevel) l3NetEntity.getLevel();
        BlockPos pos = (l3NetEntity.getBlockPos());

        serverLevel.sendParticles(new DustParticleOptions(color, 1.5f), // color, scale
                                  pos.getX() + .5f, pos.getY() + 1.5f, pos.getZ() + .5f, 5,  // count
                                  0, // delta X
                                  0, // delta Y
                                  0, // delta Z
                                  1  // speed
        );
    }
}
