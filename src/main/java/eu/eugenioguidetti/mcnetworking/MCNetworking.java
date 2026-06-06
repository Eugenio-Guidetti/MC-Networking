package eu.eugenioguidetti.mcnetworking;

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.component.ModDataComponentTypes;
import eu.eugenioguidetti.mcnetworking.creativemodetab.ModCreativeModeTabs;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MCNetworking implements ModInitializer
{
    public static final String MOD_ID = "mc-networking";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize()
    {
        ModCreativeModeTabs.registerModCreativeModeTabs();

        ModDataComponentTypes.registerModDataComponentTypes();

        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerModBlocksEntities();
    }
}