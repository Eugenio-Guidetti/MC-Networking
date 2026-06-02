package eu.eugenioguidetti.mcnetworking.datagen;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlocks;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModRecipeProvider extends FabricRecipeProvider
{
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output)
    {
        return new RecipeProvider(registries, output)
        {
            @Override
            public void buildRecipes()
            {
                shaped(RecipeCategory.REDSTONE, ModBlocks.HOST_BLOCK)
                        .pattern("IGI")
                        .pattern("ITI")
                        .pattern("III")
                        .define('I', Items.IRON_BLOCK)
                        .define('G', Items.GOLD_BLOCK)
                        .define('T', Items.REDSTONE_TORCH)
                        .unlockedBy(getHasName(Blocks.AIR), has(Blocks.AIR))
                        .save(output);

                shaped(RecipeCategory.REDSTONE, ModItems.COPPER_STRAIGHT_CABLE)
                        .pattern("HGH")
                        .pattern("HCH")
                        .pattern("HCH")
                        .define('H', Items.HONEYCOMB)
                        .define('C', Items.COPPER_INGOT)
                        .define('G', Items.GOLD_NUGGET)
                        .unlockedBy(getHasName(Items.AIR), has(Items.AIR))
                        .save(output);

                shaped(RecipeCategory.REDSTONE, ModItems.COPPER_CROSSOVER_CABLE)
                        .pattern("HCH")
                        .pattern("HCH")
                        .pattern("HGH")
                        .define('H', Items.HONEYCOMB)
                        .define('C', Items.COPPER_INGOT)
                        .define('G', Items.GOLD_NUGGET)
                        .unlockedBy(getHasName(Items.AIR), has(Items.AIR))
                        .save(output);

                shaped(RecipeCategory.REDSTONE, ModItems.FIBER_OPTIC_CABLE)
                        .pattern("HGH")
                        .pattern("HGH")
                        .pattern("HGH")
                        .define('H', Items.HONEYCOMB)
                        .define('G', Items.GLASS_PANE)
                        .unlockedBy(getHasName(Items.AIR), has(Items.AIR))
                        .save(output);
            }
        };
    }


    @Override
    public String getName()
    {
        return MCNetworking.MOD_ID + " Recipes";
    }
}
