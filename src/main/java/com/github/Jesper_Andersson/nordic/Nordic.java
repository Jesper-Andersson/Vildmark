package com.github.Jesper_Andersson.nordic;

import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeRegion;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.SurfaceRuleData;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import static com.github.Jesper_Andersson.nordic.world.item.NItems.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Nordic.MODID)
public class Nordic {
    public static final String MODID = "nordic";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registries.CONFIGURED_FEATURE, MODID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registries.PLACED_FEATURE, MODID);

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LINGONBERRY_SHRUB_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "patch_lingonberry_shrub"));
    public static final ResourceKey<PlacedFeature> PATCH_LINGONBERRY_SHRUB_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, "patch_lingonberry_shrub"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BLUEBERRY_SHRUB_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "patch_blueberry_shrub"));
    public static final ResourceKey<PlacedFeature> PATCH_BLUEBERRY_SHRUB_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, "patch_blueberry_shrub"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CHANTARELLE_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "patch_chantarelle"));
    public static final ResourceKey<PlacedFeature> PATCH_CHANTARELLE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, "patch_chantarelle"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "pine"));
    public static final ResourceKey<PlacedFeature> PINE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MODID, "pine"));


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, MODID);

    public static final RegistryObject<CreativeModeTab> NORDIC_TAB = CREATIVE_MODE_TABS.register("nordic_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> LINGONBERRIES_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(PINE_PLANKS_ITEM.get());
        output.accept(PINE_LOG_ITEM.get());
        output.accept(PINE_SAPLING_ITEM.get());
        output.accept(PINE_LEAVES_ITEM.get());
        output.accept(LINGONBERRIES_ITEM.get());
        output.accept(LINGONBERRY_SHRUB_ITEM.get());
        output.accept(BLUEBERRIES_ITEM.get());
        output.accept(BLUEBERRY_SHRUB_ITEM.get());
        output.accept(CHANTARELLE_ITEM.get());
    }).build());

    public Nordic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        NBlocks.register();
        NItems.register();

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        CONFIGURED_FEATURES.register(modEventBus);
        PLACED_FEATURES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                {
                    Regions.register(new BiomeRegion(new ResourceLocation(MODID, "overworld_1"), 2));

                    SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MODID, SurfaceRuleData.makeRules());
                });

        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
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
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        }
    }
}
