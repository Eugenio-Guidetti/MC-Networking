package eu.eugenioguidetti.mcnetworking.component;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.function.UnaryOperator;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModDataComponentTypes
{
    public static final DataComponentType<PendingConnection> PENDING_CONNECTION = registerModDataComponentType("pending_connection",
                                                                                                               builder -> builder.persistent(
                                                                                                                       PendingConnection.CODEC));

    private static <T> DataComponentType<T> registerModDataComponentType(String name,
                                                                         UnaryOperator<DataComponentType.Builder<T>> builderOperator)
    {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                                 Identifier.fromNamespaceAndPath(MCNetworking.MOD_ID, name),
                                 builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void registerModDataComponentTypes()
    {
        MCNetworking.LOGGER.info("Registering Mod Data Component Types for: " + MCNetworking.MOD_ID);
    }
}
