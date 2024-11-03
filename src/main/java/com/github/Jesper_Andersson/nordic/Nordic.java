package com.github.Jesper_Andersson.nordic;

import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.LingonberryShrubBlock;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.BiomeRegion;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.SurfaceRuleData;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Nordic.MODID)
public class Nordic {
    public static final String MODID = "nordic";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, MODID);

    //public static final RegistryObject<Biome> PINE_FOREST = BIOMES.register("nordic_forest", () -> new Biome.BiomeBuilder());

    public static final ResourceKey<PlacedFeature> PATCH_LINGONBERRY_SHRUB = PlacementUtils.createKey("");

    public static final RegistryObject<Block> LINGONBERRY_SHRUB = BLOCKS.register("lingonberry_shrub", () -> new LingonberryShrubBlock(BlockBehaviour.Properties.of().noOcclusion().noCollission().randomTicks()));

    public static final RegistryObject<Block> PINE_SAPLING = BLOCKS.register("pine_sapling", () -> new SaplingBlock(new OakTreeGrower(), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> PINE_LOG = BLOCKS.register("pine_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2f,2f)));
    public static final RegistryObject<Block> PINE_PLANKS = BLOCKS.register("pine_planks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2f, 2f)));
    public static final RegistryObject<Block> PINE_LEAVES = BLOCKS.register("pine_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));

    public static final RegistryObject<Item> LINGONBERRIES_ITEM = ITEMS.register("lingonberries", () -> new ItemNameBlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final RegistryObject<Item> LINGONBERRY_SHRUB_ITEM = ITEMS.register("lingonberry_shrub", () -> new BlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_SAPLING_ITEM = ITEMS.register("pine_sapling", () -> new BlockItem(PINE_SAPLING.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_LOG_ITEM = ITEMS.register("pine_log", () -> new BlockItem(PINE_LOG.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_PLANKS_ITEM = ITEMS.register("pine_planks", () -> new BlockItem(PINE_PLANKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_LEAVES_ITEM = ITEMS.register("pine_leaves", () -> new BlockItem(PINE_LEAVES.get(), new Item.Properties()));

    //public static final RegistryObject<SaplingBlock> PINE_SAPLING = SAPLING_BLOCKS.register("pine_sapling", () -> new SaplingBlock(new SpruceTreeGrower(), BlockBehaviour.Properties.of()));
    //public static final RegistryObject<Item> PINE_SAPLING_ITEM = ITEMS.register("pine_sapling", () -> new BlockItem(PINE_SAPLING.get(), new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> NORDIC_TAB = CREATIVE_MODE_TABS.register("nordic_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> LINGONBERRIES_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(PINE_PLANKS_ITEM.get());
        output.accept(PINE_LOG_ITEM.get());
        output.accept(PINE_SAPLING_ITEM.get());
        output.accept(PINE_LEAVES_ITEM.get());
        output.accept(LINGONBERRIES_ITEM.get());
        output.accept(LINGONBERRY_SHRUB_ITEM.get());

    }).build());

    public Nordic() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
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
