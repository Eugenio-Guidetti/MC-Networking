package eu.eugenioguidetti.mcnetworking.block.registry;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.HubBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.SwitchBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModBlockEntities
{
    public static final BlockEntityType<HostBlockEntity> HOST_BLOCK_ENTITY = registerBlockEntity("host_block",
                                                                                                 HostBlockEntity::new,
                                                                                                 ModBlocks.HOST_BLOCK);

    public static final BlockEntityType<HubBlockEntity> HUB_BLOCK_ENTITY = registerBlockEntity("hub_block",
                                                                                               HubBlockEntity::new,
                                                                                               ModBlocks.HUB_BLOCK);

    public static final BlockEntityType<SwitchBlockEntity> SWITCH_BLOCK_ENTITY = registerBlockEntity("switch_block",
                                                                                                     SwitchBlockEntity::new,
                                                                                                     ModBlocks.SWITCH_BLOCK);

    public static final BlockEntityType<RouterBlockEntity> ROUTER_BLOCK_ENTITY = registerBlockEntity("router_block",
                                                                                                     RouterBlockEntity::new,
                                                                                                     ModBlocks.ROUTER_BLOCK);

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
                                                                                  FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
                                                                                  Block... blocks)
    {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                 Identifier.fromNamespaceAndPath(MCNetworking.MOD_ID, name),
                                 FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void registerModBlocksEntities()
    {
        MCNetworking.LOGGER.info("Registering Mod Blocks Entities for: " + MCNetworking.MOD_ID);
    }
}
