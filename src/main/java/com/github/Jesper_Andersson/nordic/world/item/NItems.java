package com.github.Jesper_Andersson.nordic.world.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.registries.RegistryObject;

import static com.github.Jesper_Andersson.nordic.world.level.block.NBlocks.*;
import static com.github.Jesper_Andersson.nordic.Nordic.ITEMS;


public class NItems {
    public static final RegistryObject<Item> LINGONBERRIES_ITEM = ITEMS.register("lingonberries", () -> new ItemNameBlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final RegistryObject<Item> CHANTARELLE_ITEM = ITEMS.register("chantarelle", () -> new ItemNameBlockItem(CHANTARELLE.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final RegistryObject<Item> LINGONBERRY_SHRUB_ITEM = ITEMS.register("lingonberry_shrub", () -> new BlockItem(LINGONBERRY_SHRUB.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLUEBERRIES_ITEM = ITEMS.register("blueberries", () -> new ItemNameBlockItem(BLUEBERRY_SHRUB.get(), new Item.Properties().food(new FoodProperties.Builder().nutrition(1).build())));

    public static final RegistryObject<Item> BLUEBERRY_SHRUB_ITEM = ITEMS.register("blueberry_shrub", () -> new BlockItem(BLUEBERRY_SHRUB.get(), new Item.Properties()));

    public static final RegistryObject<Item> PINE_SAPLING_ITEM = ITEMS.register("pine_sapling", () -> new BlockItem(PINE_SAPLING.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_LOG_ITEM = ITEMS.register("pine_log", () -> new BlockItem(PINE_LOG.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_PLANKS_ITEM = ITEMS.register("pine_planks", () -> new BlockItem(PINE_PLANKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> PINE_LEAVES_ITEM = ITEMS.register("pine_leaves", () -> new BlockItem(PINE_LEAVES.get(), new Item.Properties()));

    public static void register() {}
}
