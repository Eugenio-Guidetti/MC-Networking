package eu.eugenioguidetti.mcnetworking.item;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 24/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.item.custom.CableItem;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModItems
{
    // Registriamo 3 oggetti separati, uno per ogni tipo di cavo
    public static final Item COPPER_STRAIGHT_CABLE = registerItem("copper_straight_cable",
                                                                  props -> new CableItem(props, CableType.COPPER_STRAIGHT),
                                                                  new Item.Properties());

    public static final Item COPPER_CROSSOVER_CABLE = registerItem("copper_crossover_cable",
                                                                   props -> new CableItem(props, CableType.COPPER_CROSSOVER),
                                                                   new Item.Properties());

    public static final Item FIBER_OPTIC_CABLE = registerItem("fiber_optic_cable",
                                                              props -> new CableItem(props, CableType.FIBER_OPTIC),
                                                              new Item.Properties());

    public static ResourceKey<Item> getRK(Item item)
    {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> function, Item.Properties settings)
    {
        return Registry.register(BuiltInRegistries.ITEM,
                                 Identifier.fromNamespaceAndPath(MCNetworking.MOD_ID, name),
                                 function.apply(settings.setId(ResourceKey.create(Registries.ITEM,
                                                                                  Identifier.fromNamespaceAndPath(MCNetworking.MOD_ID,
                                                                                                                  name)))));
    }

    public static void registerModItems()
    {
        MCNetworking.LOGGER.info("Registering Mod Items for: " + MCNetworking.MOD_ID);
    }
}
