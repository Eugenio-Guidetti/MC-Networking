package eu.eugenioguidetti.mcnetworking.datagen;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 24/05/2026
 */

import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModModelProvider extends FabricModelProvider
{
    public ModModelProvider(FabricPackOutput output)
    {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators blockModelGenerators)
    {
        TexturedModel.Provider hostBlockTextureModelProvider = TexturedModel.ORIENTABLE;
        blockModelGenerators.createHorizontallyRotatedBlock(ModBlocks.HOST_BLOCK, hostBlockTextureModelProvider);
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators itemModelGenerators)
    {
        itemModelGenerators.generateFlatItem(ModItems.COPPER_STRAIGHT_CABLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.COPPER_CROSSOVER_CABLE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.FIBER_OPTIC_CABLE, ModelTemplates.FLAT_ITEM);
    }
}
