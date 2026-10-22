package io.github.jesperandersson.vildmark.world.item;

import io.github.jesperandersson.vildmark.world.entity.VEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;

import static io.github.jesperandersson.vildmark.world.level.block.VBlocks.*;
import static io.github.jesperandersson.vildmark.Vildmark.ITEMS;

/**
 * All of Vildmark's items. Blocks that can be held get a block item here too, with the same ID as
 * the block.
 */
public class VItems {
    // Berries and mushrooms
    public static final DeferredItem<Item> LINGONBERRIES_ITEM = ITEMS.registerSimpleItem(
            "lingonberries",
            () -> new Item.Properties().food(Foods.SWEET_BERRIES));

    // Edible block item: eating it or placing it as a chantarelle both work
    public static final DeferredItem<Item> CHANTARELLE_ITEM = ITEMS.registerItem(
            "chantarelle",
            properties -> new BlockItem(CHANTARELLE.get(), properties.useItemDescriptionPrefix()),
            () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build()));

    public static final DeferredItem<BlockItem> PINE_CONK_ITEM = ITEMS.registerItem(
            "pine_conk",
            properties -> new BlockItem(PINE_CONK.get(), properties.useItemDescriptionPrefix()));

    public static final DeferredItem<BlockItem> LINGONBERRY_SHRUB_ITEM =
            ITEMS.registerSimpleBlockItem("lingonberry_shrub", LINGONBERRY_SHRUB);

    public static final DeferredItem<Item> BLUEBERRIES_ITEM = ITEMS.registerSimpleItem(
            "blueberries",
            () -> new Item.Properties().food(Foods.SWEET_BERRIES));

    public static final DeferredItem<BlockItem> BLUEBERRY_SHRUB_ITEM =
            ITEMS.registerSimpleBlockItem("blueberry_shrub", BLUEBERRY_SHRUB);

    // Rarer than the other berries, so more filling. Planting the berries grows a cloudberry shrub
    public static final DeferredItem<Item> CLOUDBERRIES_ITEM = ITEMS.registerItem(
            "cloudberries",
            properties -> new BlockItem(
                    CLOUDBERRY_SHRUB.get(), properties.useItemDescriptionPrefix()),
            () -> new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.6f)
                    .build()));

    public static final DeferredItem<BlockItem> CLOUDBERRY_SHRUB_ITEM =
            ITEMS.registerSimpleBlockItem("cloudberry_shrub", CLOUDBERRY_SHRUB);

    // Blocks
    public static final DeferredItem<BlockItem> REED_ITEM =
            ITEMS.registerSimpleBlockItem("reed", REED);
    public static final DeferredItem<BlockItem> CATTAIL_ITEM =
            ITEMS.registerSimpleBlockItem("cattail", CATTAIL);

    public static final DeferredItem<BlockItem> PINE_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("pine_sapling", PINE_SAPLING);
    public static final DeferredItem<BlockItem> PINE_LOG_ITEM =
            ITEMS.registerSimpleBlockItem("pine_log", PINE_LOG);
    public static final DeferredItem<BlockItem> PINE_PLANKS_ITEM =
            ITEMS.registerSimpleBlockItem("pine_planks", PINE_PLANKS);
    public static final DeferredItem<BlockItem> PINE_STAIRS_ITEM =
            ITEMS.registerSimpleBlockItem("pine_stairs", PINE_STAIRS);
    public static final DeferredItem<BlockItem> PINE_SLAB_ITEM =
            ITEMS.registerSimpleBlockItem("pine_slab", PINE_SLAB);
    public static final DeferredItem<BlockItem> SLANTED_FENCE_ITEM =
            ITEMS.registerSimpleBlockItem("slanted_fence", SLANTED_FENCE);

    public static final DeferredItem<BlockItem> PINE_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("pine_leaves", PINE_LEAVES);
    public static final DeferredItem<BlockItem> PEAT_ITEM =
            ITEMS.registerSimpleBlockItem("peat", PEAT);
    public static final DeferredItem<BlockItem> PINE_NEEDLES_ITEM =
            ITEMS.registerSimpleBlockItem("pine_needles", PINE_NEEDLES);
    public static final DeferredItem<BlockItem> PEAT_IRON_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("peat_iron_ore", PEAT_IRON_ORE);
    public static final DeferredItem<BlockItem> COTTON_GRASS_ITEM =
            ITEMS.registerSimpleBlockItem("cotton_grass", COTTON_GRASS);
    public static final DeferredItem<BlockItem> SUNDEW_ITEM =
            ITEMS.registerSimpleBlockItem("sundew", SUNDEW);
    public static final DeferredItem<BlockItem> BEARD_LICHEN_ITEM =
            ITEMS.registerSimpleBlockItem("beard_lichen", BEARD_LICHEN);
    public static final DeferredItem<BlockItem> STINGING_NETTLE_ITEM =
            ITEMS.registerSimpleBlockItem("stinging_nettle", STINGING_NETTLE);
    public static final DeferredItem<BlockItem> SNAKE_FERN_ITEM =
            ITEMS.registerSimpleBlockItem("snakefern", SNAKE_FERN);
    public static final DeferredItem<BlockItem> HEATHER_ITEM =
            ITEMS.registerSimpleBlockItem("heather", HEATHER);

    public static final DeferredItem<BlockItem> BLUEBERRY_PIE_ITEM = ITEMS.registerSimpleBlockItem(
            "blueberry_pie",
            BLUEBERRY_PIE,
            () -> new Item.Properties().stacksTo(1));

    // Fish
    public static final DeferredItem<Item> PIKE = ITEMS.registerSimpleItem(
            "pike",
            () -> new Item.Properties().food(Foods.SALMON));
    public static final DeferredItem<Item> COOKED_PIKE = ITEMS.registerSimpleItem(
            "cooked_pike",
            () -> new Item.Properties().food(Foods.COOKED_SALMON));
    public static final DeferredItem<Item> ROACH = ITEMS.registerSimpleItem(
            "roach",
            () -> new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1f)
                    .build()));
    public static final DeferredItem<Item> COOKED_ROACH = ITEMS.registerSimpleItem(
            "cooked_roach",
            () -> new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.5f)
                    .build()));

    // Entity types are registered before items, so .get() is safe here
    public static final DeferredItem<Item> PIKE_BUCKET = ITEMS.registerItem(
            "pike_bucket",
            properties -> new MobBucketItem(
                    VEntities.PIKE.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties),
            () -> new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY));
    public static final DeferredItem<Item> ROACH_BUCKET = ITEMS.registerItem(
            "roach_bucket",
            properties -> new MobBucketItem(
                    VEntities.ROACH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties),
            () -> new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY));

    // Spawn eggs
    public static final DeferredItem<Item> BARROW_WIGHT_SPAWN_EGG = ITEMS.registerItem(
            "barrow_wight_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.BARROW_WIGHT.get()));
    public static final DeferredItem<Item> PIKE_SPAWN_EGG = ITEMS.registerItem(
            "pike_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.PIKE.get()));
    public static final DeferredItem<Item> ROACH_SPAWN_EGG = ITEMS.registerItem(
            "roach_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.ROACH.get()));
    public static final DeferredItem<Item> MOOSE_SPAWN_EGG = ITEMS.registerItem(
            "moose_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.MOOSE.get()));
    public static final DeferredItem<Item> TOMTE_SPAWN_EGG = ITEMS.registerItem(
            "tomte_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.TOMTE.get()));
    public static final DeferredItem<Item> WILL_O_THE_WISP_SPAWN_EGG = ITEMS.registerItem(
            "willothewisp_spawn_egg",
            SpawnEggItem::new,
            () -> new Item.Properties().spawnEgg(VEntities.WILL_O_THE_WISP.get()));

    // Called from the mod constructor only to load this class, which adds the items above to ITEMS
    public static void register() {}
}
