package eu.eugenioguidetti.mcnetworking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 06/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.logic.AbstractL3Engine;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class Utils
{
    public static @NonNull Vec3 getInterfaceCenterPoint(@NonNull BlockPos pos, @NonNull Direction face)
    {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        double edgeX = centerX + (face.getStepX() * 0.5);
        double edgeY = centerY + (face.getStepY() * 0.5);
        double edgeZ = centerZ + (face.getStepZ() * 0.5);

        return new Vec3(edgeX, edgeY, edgeZ);
    }

    /**
     * Cerca se l'indirizzo IP 'targetIp' appartiene a una rete direttamente connessa a una delle interfacce 'nics'.
     *
     * @return Il nome dell'interfaccia oppure null se non trovata.
     */
    public static AbstractL3Engine.@Nullable OutPacketData getOutPacketData(@NonNull Ipv4Address targetIp,
                                                                            @NonNull Collection<NetworkInterface> nics)
    {
        if (targetIp.isLoopback())
        {
            return new AbstractL3Engine.OutPacketData(Ipv4Address.LOOPBACK, LOOPBACK_NAME);
        }

        for (NetworkInterface nic : nics)
        {
            // Salta interfacce non configurate e di loopback
            if (nic.isLoopback() || !nic.isConnected() || nic.getIpAddress().address().isAllZeros())
            {
                continue;
            }

            if (nic.getIpAddress().contieneIp(targetIp))
            {
                // Rete di destinazione direttamente connessa
                return new AbstractL3Engine.OutPacketData(targetIp, nic.getName());
            }
        }

        return null;
    }
}
