package eu.eugenioguidetti.mcnetworking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 06/06/2026
 */

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 *
 * @author Eugenio Guidetti
 */
public class Utils
{
    public static Vec3 getInterfaceCenterPoint(BlockPos pos, Direction face)
    {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;

        double edgeX = centerX + (face.getStepX() * 0.5);
        double edgeY = centerY + (face.getStepY() * 0.5);
        double edgeZ = centerZ + (face.getStepZ() * 0.5);

        return new Vec3(edgeX, edgeY, edgeZ);
    }
}
