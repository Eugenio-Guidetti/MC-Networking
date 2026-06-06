package eu.eugenioguidetti.mcnetworking.datagen;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider
{
    public ModBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture)
    {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider)
    {
        valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.HOST_BLOCK)
                .add(ModBlocks.HUB_BLOCK)
                .add(ModBlocks.SWITCH_BLOCK)
                .add(ModBlocks.ROUTER_BLOCK);

        valueLookupBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.HOST_BLOCK)
                .add(ModBlocks.HUB_BLOCK)
                .add(ModBlocks.SWITCH_BLOCK)
                .add(ModBlocks.ROUTER_BLOCK);
    }
}
