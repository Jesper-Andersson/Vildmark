package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
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

import java.util.concurrent.CompletableFuture;

public class NRecipeProvider extends RecipeProvider {
    public NRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, NBlocks.PINE_PLANKS, 4)
                .requires(NBlocks.PINE_LOG)
                .group("planks")
                .unlockedBy("has_log", has(NBlocks.PINE_LOG))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NBlocks.PINE_SLAB, 6)
                .pattern("###")
                .define('#', NBlocks.PINE_PLANKS)
                .group("wooden_slab")
                .unlockedBy("has_planks", has(NBlocks.PINE_PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NBlocks.PINE_STAIRS, 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', NBlocks.PINE_PLANKS)
                .group("wooden_stairs")
                .unlockedBy("has_planks", has(NBlocks.PINE_PLANKS))
                .save(output);

        cookFood(output, NItems.PIKE.get(), NItems.COOKED_PIKE.get());
        cookFood(output, NItems.ROACH.get(), NItems.COOKED_ROACH.get());

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(NBlocks.PEAT_IRON_ORE), RecipeCategory.MISC, Items.RAW_IRON, 0.7F, 200)
                .unlockedBy("has_peat_iron_ore", has(NBlocks.PEAT_IRON_ORE))
                .save(output, modLoc("raw_iron_from_smelting_peat_iron_ore"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(NBlocks.PEAT_IRON_ORE), RecipeCategory.MISC, Items.RAW_IRON, 0.7F, 100)
                .unlockedBy("has_peat_iron_ore", has(NBlocks.PEAT_IRON_ORE))
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
        return ResourceLocation.fromNamespaceAndPath(Nordic.MODID, path);
    }
}
