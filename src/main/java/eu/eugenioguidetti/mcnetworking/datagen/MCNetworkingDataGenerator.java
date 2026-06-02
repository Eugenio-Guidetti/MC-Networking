package eu.eugenioguidetti.mcnetworking.datagen;

import eu.eugenioguidetti.mcnetworking.datagen.translations.ModEnglishLangProvider;
import eu.eugenioguidetti.mcnetworking.datagen.translations.ModItalianLangProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NonNull;

public class MCNetworkingDataGenerator implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator)
    {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModItalianLangProvider::new);
        pack.addProvider(ModEnglishLangProvider::new);

        pack.addProvider(ModModelProvider::new);

        pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider(ModBlockLootTableProvider::new);

        pack.addProvider(ModRecipeProvider::new);
    }
}
