package eu.eugenioguidetti.mcnetworking.item;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

/**
 *
 * @author Eugenio Guidetti
 */
public record ConnectorType(String name)
{
    private static final String RJ45_NAME = "RJ45";
    private static final String FIBER_NAME = "Fiber";

    public static final ConnectorType RJ45 = new ConnectorType(RJ45_NAME);
    public static final ConnectorType FIBER = new ConnectorType(FIBER_NAME);

    public static ConnectorType fromName(String name)
    {
        return switch (name)
        {
            case RJ45_NAME -> RJ45;
            case FIBER_NAME -> FIBER;
            default -> null;
        };
    }
}