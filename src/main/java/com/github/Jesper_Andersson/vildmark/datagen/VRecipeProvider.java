package com.github.Jesper_Andersson.vildmark.datagen;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.github.Jesper_Andersson.vildmark.world.item.VItems;
import com.github.Jesper_Andersson.vildmark.world.level.block.VBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class VRecipeProvider extends RecipeProvider {
    public VRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_PLANKS, 4)
                .requires(VBlocks.PINE_LOG)
                .group("planks")
                .unlockedBy("has_log", has(VBlocks.PINE_LOG))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_SLAB, 6)
                .pattern("###")
                .define('#', VBlocks.PINE_PLANKS)
                .group("wooden_slab")
                .unlockedBy("has_planks", has(VBlocks.PINE_PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_STAIRS, 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', VBlocks.PINE_PLANKS)
                .group("wooden_stairs")
                .unlockedBy("has_planks", has(VBlocks.PINE_PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.WHITE_WOOL, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', VItems.COTTON_GRASS_ITEM)
                .unlockedBy("has_cotton_grass", has(VItems.COTTON_GRASS_ITEM))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, VItems.BLUEBERRY_PIE_ITEM)
                .pattern("###")
                .pattern("@%@")
                .pattern("???")
                .define('#', VItems.BLUEBERRIES_ITEM)
                .define('%', Items.EGG)
                .define('@', Items.SUGAR)
                .define('?', Items.WHEAT)
                .unlockedBy("has_blueberries", has(VItems.BLUEBERRIES_ITEM))
                .save(output);


        cookFood(output, VItems.PIKE.get(), VItems.COOKED_PIKE.get());
        cookFood(output, VItems.ROACH.get(), VItems.COOKED_ROACH.get());

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(VBlocks.PEAT_IRON_ORE), RecipeCategory.MISC, Items.RAW_IRON, 0.7F, 200)
                .unlockedBy("has_peat_iron_ore", has(VBlocks.PEAT_IRON_ORE))
                .save(output, modLoc("raw_iron_from_smelting_peat_iron_ore"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(VBlocks.PEAT_IRON_ORE), RecipeCategory.MISC, Items.RAW_IRON, 0.7F, 100)
                .unlockedBy("has_peat_iron_ore", has(VBlocks.PEAT_IRON_ORE))
                .save(output, modLoc("raw_iron_from_blasting_peat_iron_ore"));
    }

    // Furnace, smoker and campfire recipes with vanilla's cooking times for raw fish and meat
    private static void cookFood(RecipeOutput output, ItemLike raw, ItemLike cooked) {
        String cookedName = getItemName(cooked);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35F, 200)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output, modLoc(cookedName));
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35F, 100)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output, modLoc(cookedName + "_from_smoking"));
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35F, 600)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output, modLoc(cookedName + "_from_campfire_cooking"));
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, path);
    }
}
