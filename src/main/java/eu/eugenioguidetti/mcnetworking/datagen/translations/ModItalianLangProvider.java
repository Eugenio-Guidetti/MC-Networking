package eu.eugenioguidetti.mcnetworking.datagen.translations;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
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
public class ModItalianLangProvider extends FabricLanguageProvider
{
    public ModItalianLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(dataOutput, "it_it", registryLookup);
    }


    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider provider, TranslationBuilder translationBuilder)
    {
        // --- Items ---
        translationBuilder.add(ModItems.COPPER_STRAIGHT_CABLE, "Cavo di Rame Straight");
        translationBuilder.add(ModItems.COPPER_CROSSOVER_CABLE, "Cavo di Rame Crossover");
        translationBuilder.add(ModItems.FIBER_OPTIC_CABLE, "Cavo in Fibra Ottica");
        translationBuilder.add(ModItems.SCISSORS, "Forbici");

        // --- Item tooltips ---
        translationBuilder.add("tooltip.mcnetworking.cable", "§7§nClick destro§r§7 per selezionare la prima interfaccia");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection",
                               "§7§nClick destro§r§7 per selezionare la seconda interfaccia");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.cancel",
                               "§7§nShift-Click destro§r§7 per annullare la connessione");
        translationBuilder.add("tooltip.mcnetworking.cable.pending_connection.first_interface", "§7Prima interfaccia in §o%s, faccia: %s");

        // --- Blocks ---
        //translationBuilder.add(ModBlocks., "Nome blocco");

        // --- Creative mode tabs ---
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".cables", "MCNetworking: Cavi");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".end_devices", "MCNetworking: Dispositivi Finali");
        translationBuilder.add("itemGroup." + MCNetworking.MOD_ID + ".network_devices", "MCNetworking: Dispositivi di Rete");
    }
}
