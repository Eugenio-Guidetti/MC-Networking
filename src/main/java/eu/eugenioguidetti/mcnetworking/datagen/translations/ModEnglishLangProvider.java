package eu.eugenioguidetti.mcnetworking.datagen.translations;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModEnglishLangProvider extends FabricLanguageProvider
{
    public ModEnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        // Specifying en_us is optional, as it's the default language code
        super(dataOutput, "en_us", registryLookup);
    }


    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider provider, TranslationBuilder translationBuilder)
    {
        // --- Items ---
        translationBuilder.add(ModItems.COPPER_STRAIGHT_CABLE, "Copper Straight Cable");
        translationBuilder.add(ModItems.COPPER_CROSSOVER_CABLE, "Copper Crossover Cable");
        translationBuilder.add(ModItems.FIBER_OPTIC_CABLE, "Fiber Optic Cable");

        // --- Item tooltips ---
        translationBuilder.add("tooltip.mcnetworking.cable", "§7§nRight click§r§7 to select the first interface");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection", "§7§nRight click§r§7 to select the second interface");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.cancel", "§7§nShift-Right click§r§7 to cancel connection");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.first_interface", "§7First interface at §o%s, face: %s");

        // --- Blocks ---
        translationBuilder.add(ModBlocks.HOST_BLOCK, "Host");
        //translationBuilder.add(ModBlocks., "Block name");

        // --- Creative mode tabs ---
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".cables", "MCNetworking: Cables");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".end_devices", "MCNetworking: End Devices");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".network_devices", "MCNetworking: Network Devices");
    }
}
