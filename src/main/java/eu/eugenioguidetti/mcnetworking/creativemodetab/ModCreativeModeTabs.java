package eu.eugenioguidetti.mcnetworking.creativemodetab;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 24/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModCreativeModeTabs
{
    public static final CreativeModeTab CABLES_TAB = registerModCreativeModeTab("cables",
                                                                                ModItems.COPPER_STRAIGHT_CABLE,
                                                                                (parameters, output) ->
                                                                                {
                                                                                    output.accept(ModItems.COPPER_STRAIGHT_CABLE);
                                                                                    output.accept(ModItems.COPPER_CROSSOVER_CABLE);
                                                                                    output.accept(ModItems.FIBER_OPTIC_CABLE);
                                                                                    output.accept(ModItems.SCISSORS);
                                                                                });

    public static final CreativeModeTab END_DEVICES_TAB = registerModCreativeModeTab("end_devices",
                                                                                     ModBlocks.HOST_BLOCK,
                                                                                     (parameters, output) ->
                                                                                     {
                                                                                         output.accept(ModBlocks.HOST_BLOCK);
                                                                                     });

    public static final CreativeModeTab NETWORK_DEVICES_TAB = registerModCreativeModeTab("network_devices",
                                                                                         ModBlocks.ROUTER_BLOCK,
                                                                                         (parameters, output) ->
                                                                                         {
                                                                                             output.accept(ModBlocks.HUB_BLOCK);
                                                                                             output.accept(ModBlocks.SWITCH_BLOCK);
                                                                                             output.accept(ModBlocks.ROUTER_BLOCK);
                                                                                         });

    private static CreativeModeTab registerModCreativeModeTab(String name,
                                                              ItemLike icon,
                                                              CreativeModeTab.DisplayItemsGenerator displayItems)
    {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                                 Identifier.fromNamespaceAndPath(MCNetworking.MOD_ID, name),
                                 FabricCreativeModeTab
                                         .builder()
                                         .icon(() -> new ItemStack(icon))
                                         .title(Component.translatable("itemGroup." + MCNetworking.MOD_ID + "." + name))
                                         .displayItems(displayItems)
                                         .build());
    }

    public static void registerModCreativeModeTabs()
    {
        MCNetworking.LOGGER.info("Registering Mod Creative Mode Tabs for: " + MCNetworking.MOD_ID);
    }
}
