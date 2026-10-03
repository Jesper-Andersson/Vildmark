package com.github.Jesper_Andersson.nordic.world.item;

import com.github.Jesper_Andersson.nordic.world.entity.NEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.github.Jesper_Andersson.nordic.world.level.block.NBlocks.*;
import static com.github.Jesper_Andersson.nordic.Nordic.ITEMS;


public class NItems {
    public static final DeferredItem<Item> LINGONBERRIES_ITEM = ITEMS.register("lingonberries", () -> new ItemNameBlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final DeferredItem<Item> CHANTARELLE_ITEM = ITEMS.register("chantarelle", () -> new ItemNameBlockItem(CHANTARELLE.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final DeferredItem<Item> LINGONBERRY_SHRUB_ITEM = ITEMS.register("lingonberry_shrub", () -> new BlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties()));

    public static final DeferredItem<Item> BLUEBERRIES_ITEM = ITEMS.register("blueberries", () -> new ItemNameBlockItem(BLUEBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final DeferredItem<Item> BLUEBERRY_SHRUB_ITEM = ITEMS.register("blueberry_shrub", () -> new BlockItem(BLUEBERRY_SHRUB.get(), new Item.Properties()));

    // Rarer than the other berries, so more filling
    public static final DeferredItem<Item> CLOUDBERRIES_ITEM = ITEMS.register("cloudberries", () -> new ItemNameBlockItem(CLOUDBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.6f).build())));

    public static final DeferredItem<Item> CLOUDBERRY_SHRUB_ITEM = ITEMS.register("cloudberry_shrub", () -> new BlockItem(CLOUDBERRY_SHRUB.get(), new Item.Properties()));

    public static final DeferredItem<Item> REED_ITEM = ITEMS.register("reed", () -> new BlockItem(REED.get(), new Item.Properties()));
    public static final DeferredItem<Item> CATTAIL_ITEM = ITEMS.register("cattail", () -> new BlockItem(CATTAIL.get(), new Item.Properties()));

    public static final DeferredItem<Item> PINE_SAPLING_ITEM = ITEMS.register("pine_sapling", () -> new BlockItem(PINE_SAPLING.get(), new Item.Properties()));
    public static final DeferredItem<Item> PINE_LOG_ITEM = ITEMS.register("pine_log", () -> new BlockItem(PINE_LOG.get(), new Item.Properties()));
    public static final DeferredItem<Item> PINE_PLANKS_ITEM = ITEMS.register("pine_planks", () -> new BlockItem(PINE_PLANKS.get(), new Item.Properties()));
    public static final DeferredItem<Item> PINE_LEAVES_ITEM = ITEMS.register("pine_leaves", () -> new BlockItem(PINE_LEAVES.get(), new Item.Properties()));
    public static final DeferredItem<Item> PEAT_ITEM = ITEMS.register("peat", () -> new BlockItem(PEAT.get(), new Item.Properties()));
    public static final DeferredItem<Item> PINE_NEEDLES_ITEM = ITEMS.register("pine_needles", () -> new BlockItem(PINE_NEEDLES.get(), new Item.Properties()));
    public static final DeferredItem<Item> PEAT_IRON_ORE_ITEM = ITEMS.register("peat_iron_ore", () -> new BlockItem(PEAT_IRON_ORE.get(), new Item.Properties()));
    public static final DeferredItem<Item> COTTON_GRASS_ITEM = ITEMS.register("cotton_grass", () -> new BlockItem(COTTON_GRASS.get(), new Item.Properties()));
    public static final DeferredItem<Item> SUNDEW_ITEM = ITEMS.register("sundew", () -> new BlockItem(SUNDEW.get(), new Item.Properties()));
    public static final DeferredItem<Item> BEARD_LICHEN_ITEM = ITEMS.register("beard_lichen", () -> new BlockItem(BEARD_LICHEN.get(), new Item.Properties()));
    public static final DeferredItem<Item> STINGING_NETTLE_ITEM = ITEMS.register("stinging_nettle", () -> new BlockItem(STINGING_NETTLE.get(), new Item.Properties()));
    public static final DeferredItem<Item> SNAKE_FERN_ITEM = ITEMS.register("snakefern", () -> new BlockItem(SNAKE_FERN.get(), new Item.Properties()));
    public static final DeferredItem<Item> HEATHER_ITEM = ITEMS.register("heather", () -> new BlockItem(HEATHER.get(), new Item.Properties()));

    // White colors so the custom egg texture isn't tinted
    public static final DeferredItem<Item> BARROW_WIGHT_SPAWN_EGG = ITEMS.register("barrow_wight_spawn_egg", () -> new DeferredSpawnEggItem(NEntities.BARROW_WIGHT, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final DeferredItem<Item> PIKE = ITEMS.register("pike", () -> new Item(new Item.Properties().food(Foods.SALMON)));
    public static final DeferredItem<Item> COOKED_PIKE = ITEMS.register("cooked_pike", () -> new Item(new Item.Properties().food(Foods.COOKED_SALMON)));
    public static final DeferredItem<Item> ROACH = ITEMS.register("roach", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).build())));
    public static final DeferredItem<Item> COOKED_ROACH = ITEMS.register("cooked_roach", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));

    // Entity types are registered before items, so .get() is safe here
    public static final DeferredItem<Item> PIKE_BUCKET = ITEMS.register("pike_bucket", () -> new MobBucketItem(NEntities.PIKE.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));
    public static final DeferredItem<Item> ROACH_BUCKET = ITEMS.register("roach_bucket", () -> new MobBucketItem(NEntities.ROACH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)));

    public static final DeferredItem<Item> PIKE_SPAWN_EGG = ITEMS.register("pike_spawn_egg", () -> new DeferredSpawnEggItem(NEntities.PIKE, 0x4F5F32, 0xC8C27A, new Item.Properties()));
    public static final DeferredItem<Item> ROACH_SPAWN_EGG = ITEMS.register("roach_spawn_egg", () -> new DeferredSpawnEggItem(NEntities.ROACH, 0xB9C0C6, 0xC2402E, new Item.Properties()));
    public static final DeferredItem<Item> MOOSE_SPAWN_EGG = ITEMS.register("moose_spawn_egg", () -> new DeferredSpawnEggItem(NEntities.MOOSE, 0x4A3526, 0xA08B6E, new Item.Properties()));
    public static final DeferredItem<Item> TOMTE_SPAWN_EGG = ITEMS.register("tomte_spawn_egg", () -> new DeferredSpawnEggItem(NEntities.TOMTE, 0xB02A2A, 0x7A7A7A, new Item.Properties()));

    public static void register() {}
}
