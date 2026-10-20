package com.github.Jesper_Andersson.nordic;

import com.github.Jesper_Andersson.nordic.compat.BiomePickCompat;
import com.github.Jesper_Andersson.nordic.core.particles.NParticleTypes;
import com.github.Jesper_Andersson.nordic.world.entity.NEntities;
import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeRegion;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.Biomes;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.feature.NFeatures;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.feature.treedecorators.NTreeDecorators;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.lake.LakeBasin;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import terrablender.api.Regions;

import java.util.List;

import static com.github.Jesper_Andersson.nordic.world.item.NItems.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Nordic.MODID)
public class Nordic {
    public static final String MODID = "nordic";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, MODID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LINGONBERRY_SHRUB_CONFIGURED = configuredFeatureKey("patch_lingonberry_shrub");
    public static final ResourceKey<PlacedFeature> PATCH_LINGONBERRY_SHRUB_PLACED = placedFeatureKey("patch_lingonberry_shrub");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BLUEBERRY_SHRUB_CONFIGURED = configuredFeatureKey("patch_blueberry_shrub");
    public static final ResourceKey<PlacedFeature> PATCH_BLUEBERRY_SHRUB_PLACED = placedFeatureKey("patch_blueberry_shrub");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CHANTARELLE_CONFIGURED = configuredFeatureKey("patch_chantarelle");
    public static final ResourceKey<PlacedFeature> PATCH_CHANTARELLE_PLACED = placedFeatureKey("patch_chantarelle");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_CONFIGURED = configuredFeatureKey("pine");
    public static final ResourceKey<PlacedFeature> PINE_PLACED = placedFeatureKey("pine");


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NORDIC_TAB = CREATIVE_MODE_TABS.register("nordic_tab",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> LINGONBERRIES_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PINE_PLANKS_ITEM.get());
                        output.accept(PINE_STAIRS_ITEM.get());
                        output.accept(PINE_SLAB_ITEM.get());
                        output.accept(SLANTED_FENCE_ITEM.get());
                        output.accept(PINE_LOG_ITEM.get());
                        output.accept(PINE_SAPLING_ITEM.get());
                        output.accept(PINE_LEAVES_ITEM.get());
                        output.accept(PINE_NEEDLES_ITEM.get());
                        output.accept(BEARD_LICHEN_ITEM.get());
                        output.accept(PEAT_ITEM.get());
                        output.accept(PEAT_IRON_ORE_ITEM.get());
                        output.accept(LINGONBERRIES_ITEM.get());
                        output.accept(LINGONBERRY_SHRUB_ITEM.get());
                        output.accept(BLUEBERRIES_ITEM.get());
                        output.accept(BLUEBERRY_SHRUB_ITEM.get());
                        output.accept(CLOUDBERRIES_ITEM.get());
                        output.accept(CLOUDBERRY_SHRUB_ITEM.get());
                        output.accept(CHANTARELLE_ITEM.get());
                        output.accept(PINE_CONK_ITEM.get());
                        output.accept(REED_ITEM.get());
                        output.accept(CATTAIL_ITEM.get());
                        output.accept(COTTON_GRASS_ITEM.get());
                        output.accept(SUNDEW_ITEM.get());
                        output.accept(STINGING_NETTLE_ITEM.get());
                        output.accept(SNAKE_FERN_ITEM.get());
                        output.accept(HEATHER_ITEM.get());
                        output.accept(PIKE.get());
                        output.accept(COOKED_PIKE.get());
                        output.accept(PIKE_BUCKET.get());
                        output.accept(ROACH.get());
                        output.accept(COOKED_ROACH.get());
                        output.accept(ROACH_BUCKET.get());
                        output.accept(PIKE_SPAWN_EGG.get());
                        output.accept(BARROW_WIGHT_SPAWN_EGG.get());
                        output.accept(ROACH_SPAWN_EGG.get());
                        output.accept(MOOSE_SPAWN_EGG.get());
                        output.accept(TOMTE_SPAWN_EGG.get());
                        output.accept(WILL_O_THE_WISP_SPAWN_EGG.get());
                    })
                    .build());

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    private static ResourceKey<PlacedFeature> placedFeatureKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    public Nordic(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueIMC);

        NBlocks.register();
        NEntities.register();
        NItems.register();

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        CONFIGURED_FEATURES.register(modEventBus);
        PLACED_FEATURES.register(modEventBus);
        LakeBasin.register(modEventBus);
        NTreeDecorators.register(modEventBus);
        NParticleTypes.register(modEventBus);
        NFeatures.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                Regions.register(new BiomeRegion(ResourceLocation.fromNamespaceAndPath(MODID, "overworld_1"), 2)));

        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
    }

    // Lists Nordic's biomes in Biome Pick'n'Choose before a world is loaded. Does nothing when it isn't installed
    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo(BiomePickCompat.MODID, "register_biomes", () -> List.of(
                Biomes.PINE_FOREST, Biomes.SPRUCE_PINE_FOREST, Biomes.LAKE, Biomes.RIVER, Biomes.BOG,
                Biomes.BIRCH_PINE_FOREST, Biomes.PLAINS, Biomes.TUNDRA));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(PINE_LOG_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        }
    }
}
