package eu.eugenioguidetti.mcnetworking.datagen;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModBlockLootTableProvider extends FabricBlockLootSubProvider
{
    public ModBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate()
    {
          dropSelf(ModBlocks.HOST_BLOCK);
    }
}
