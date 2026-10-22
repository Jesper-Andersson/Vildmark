package io.github.jesperandersson.vildmark;

import io.github.jesperandersson.vildmark.compat.BiomePickCompat;
import io.github.jesperandersson.vildmark.core.particles.VParticleTypes;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import io.github.jesperandersson.vildmark.world.level.levelgen.biome.BiomeRegion;
import io.github.jesperandersson.vildmark.world.level.levelgen.biome.Biomes;
import io.github.jesperandersson.vildmark.world.level.levelgen.feature.VFeatures;
import io.github.jesperandersson.vildmark.world.level.levelgen.feature.treedecorators.VTreeDecorators;
import io.github.jesperandersson.vildmark.world.level.levelgen.lake.LakeBasin;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

import static io.github.jesperandersson.vildmark.world.item.VItems.*;

/**
 * The mod's entry point. Creates the registers that blocks, items and other content are added to,
 * attaches them to the mod event bus, and adds Vildmark's biome region to TerraBlender.
 */
// The value here must match the modId in META-INF/neoforge.mods.toml
@Mod(Vildmark.MODID)
public class Vildmark {
    public static final String MODID = "vildmark";
    private static final Logger LOGGER = LogUtils.getLogger();
    // How often TerraBlender picks Vildmark's region compared to the others, such as vanilla's
    private static final int REGION_WEIGHT = 2;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registries.CONFIGURED_FEATURE, MODID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, MODID);
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(Registries.BIOME, MODID);

    // Keys of features defined in the data pack (data/vildmark/worldgen), so code can look them up
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LINGONBERRY_SHRUB_CONFIGURED =
            configuredFeatureKey("patch_lingonberry_shrub");
    public static final ResourceKey<PlacedFeature> PATCH_LINGONBERRY_SHRUB_PLACED =
            placedFeatureKey("patch_lingonberry_shrub");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BLUEBERRY_SHRUB_CONFIGURED =
            configuredFeatureKey("patch_blueberry_shrub");
    public static final ResourceKey<PlacedFeature> PATCH_BLUEBERRY_SHRUB_PLACED =
            placedFeatureKey("patch_blueberry_shrub");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CHANTARELLE_CONFIGURED =
            configuredFeatureKey("patch_chantarelle");
    public static final ResourceKey<PlacedFeature> PATCH_CHANTARELLE_PLACED =
            placedFeatureKey("patch_chantarelle");
    // The patch of chantarelles that bonemeal grows around a chantarelle
    public static final ResourceKey<PlacedFeature> CHANTARELLE_BONEMEAL_PLACED =
            placedFeatureKey("chantarelle_bonemeal");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_CONFIGURED =
            configuredFeatureKey("pine");
    public static final ResourceKey<PlacedFeature> PINE_PLACED = placedFeatureKey("pine");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VILDMARK_TAB =
            CREATIVE_MODE_TABS.register("vildmark_tab", () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .title(Component.translatable("Vildmark"))
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
                        output.accept(BLUEBERRY_PIE_ITEM.get());
                    })
                    .build());

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, id);
    }

    private static ResourceKey<PlacedFeature> placedFeatureKey(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MODID, name);
        return ResourceKey.create(Registries.PLACED_FEATURE, id);
    }

    public Vildmark(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::enqueueIMC);

        // Load the content classes, so their static fields add every block, entity and item to the
        // registers below before those are attached to the event bus
        VBlocks.register();
        VEntities.register();
        VItems.register();

        // Attaching a register to the mod event bus makes NeoForge register its entries at the
        // right time during loading
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        CONFIGURED_FEATURES.register(modEventBus);
        PLACED_FEATURES.register(modEventBus);
        LakeBasin.register(modEventBus);
        VTreeDecorators.register(modEventBus);
        VParticleTypes.register(modEventBus);
        VFeatures.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Game events, such as onServerStarting below, are on a separate bus from the mod events
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Mods run common setup in parallel, so changes to shared state like TerraBlender's regions
        // go through enqueueWork, which runs them on the main thread afterwards
        Identifier regionName = Identifier.fromNamespaceAndPath(MODID, "overworld_1");
        event.enqueueWork(() -> Regions.register(new BiomeRegion(regionName, REGION_WEIGHT)));
    }

    // Lists Vildmark's biomes in Biome Pick'n'Choose before a world is loaded. Does nothing when it
    // isn't installed
    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo(BiomePickCompat.MODID, "register_biomes", () -> List.of(
                Biomes.PINE_FOREST, Biomes.SPRUCE_PINE_FOREST, Biomes.LAKE, Biomes.RIVER,
                Biomes.BOG, Biomes.BIRCH_PINE_FOREST, Biomes.PLAINS, Biomes.TUNDRA));
    }

    // Also lists the pine log in the vanilla building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(PINE_LOG_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Vildmark loaded on the server.");
    }

    // EventBusSubscriber registers the static @SubscribeEvent methods of this class on its own.
    // Dist.CLIENT keeps it from loading on a dedicated server
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Vildmark loaded.");
        }
    }
}
