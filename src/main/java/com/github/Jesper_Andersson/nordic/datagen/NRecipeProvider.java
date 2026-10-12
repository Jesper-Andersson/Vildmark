package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.concurrent.CompletableFuture;

public class NRecipeProvider extends RecipeProvider {
    public NRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
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
    }
}
