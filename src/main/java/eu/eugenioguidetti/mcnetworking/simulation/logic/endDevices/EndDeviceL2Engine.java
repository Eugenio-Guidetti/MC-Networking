package eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 12/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.L2Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.Ipv4Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;

/**
 *
 * @author Eugenio Guidetti
 */
public class EndDeviceL2Engine implements L2Engine
{
    NetworkingBlockEntity netEntity;

    public void setNetEntity(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }

    @Override
    public void processFrame(EthernetFrame frame, Direction from, NetworkStack stack)
    {
        processColors(frame, from, stack);


        MacAddress interfaceMac = stack.getNetworkReceiver().getInterface(from).getMacAddress();

        // Il frame non è rivolto all'end device
        if (!frame.destMac().equals(interfaceMac) && !frame.destMac().equals(MacAddress.BROADCAST))
        {
            return;
        }

        if (frame.payload() instanceof Ipv4Packet packet)
        {
            // Passa il payload IP al livello superiore
            stack.receivePacket(packet, from);
        }
    }

    private void processColors(EthernetFrame frame, Direction from, NetworkStack stack)
    {
        MacAddress interfaceMac = stack.getNetworkReceiver().getInterface(from).getMacAddress();

        int color = 0;

        // Il frame non è rivolto all'end device
        if (!frame.destMac().equals(interfaceMac) && !frame.destMac().equals(MacAddress.BROADCAST))
        {
            color = ARGB.color(255, 0, 0); // Rosso
        }
        else if (frame.payload() instanceof Ipv4Packet packet)
        {
            color = ARGB.color(0, 255, 0); // Verde
        }


        ServerLevel serverLevel = (ServerLevel) netEntity.getLevel();
        BlockPos pos = (netEntity.getBlockPos());

        serverLevel.sendParticles(new DustParticleOptions(color, 1.5f), // color, scale
                                  pos.getX() + .5f, pos.getY() + 1.5f, pos.getZ() + .5f, 5,  // count
                                  0, // delta X
                                  0, // delta Y
                                  0, // delta Z
                                  1  // speed
        );
    }
}
