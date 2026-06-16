package eu.eugenioguidetti.mcnetworking.simulation.models.cables;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.minecraft.world.item.Item;

/**
 *
 * @author Eugenio Guidetti
 */
public record CableType(String name, int ticksDelay, ConnectorType connectorType, int colorHex, float lineWidth)
{
    private static final String COPPER_STRAIGHT_NAME = "Copper Straight-Through";
    private static final String COPPER_CROSSOVER_NAME = "Copper Crossover";
    private static final String FIBER_OPTIC_NAME = "Fiber Optic";

    public static final CableType COPPER_STRAIGHT = new CableType(COPPER_STRAIGHT_NAME, 3, ConnectorType.RJ45, 0x000000, 0.0625f);
    public static final CableType COPPER_CROSSOVER = new CableType(COPPER_CROSSOVER_NAME, 2, ConnectorType.RJ45, 0x454545, 0.0625f);
    public static final CableType FIBER_OPTIC = new CableType(FIBER_OPTIC_NAME, 1, ConnectorType.FIBER, 0x0023F5, 0.025f);

    public static CableType fromName(String name)
    {
        return switch (name)
        {
            case COPPER_STRAIGHT_NAME -> COPPER_STRAIGHT;
            case COPPER_CROSSOVER_NAME -> COPPER_CROSSOVER;
            case FIBER_OPTIC_NAME -> FIBER_OPTIC;
            default -> null;
        };
    }

    public Item getAsItem()
    {
        return switch (this.name)
        {
            case COPPER_CROSSOVER_NAME -> ModItems.COPPER_CROSSOVER_CABLE;
            case FIBER_OPTIC_NAME -> ModItems.FIBER_OPTIC_CABLE;
            default -> ModItems.COPPER_STRAIGHT_CABLE;
        };
    }

    public int getRed()
    {
        return (colorHex >> 16) & 0xFF;
    }

    public int getGreen()
    {
        return (colorHex >> 8) & 0xFF;
    }

    public int getBlue()
    {
        return colorHex & 0xFF;
    }
}