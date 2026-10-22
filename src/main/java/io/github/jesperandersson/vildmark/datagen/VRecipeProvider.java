package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

/** Crafting, smelting and cooking recipes. */
public class VRecipeProvider extends RecipeProvider {
    protected VRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    // The data provider that datagen runs; it creates a VRecipeProvider once the registries are
    // available
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(
                HolderLookup.Provider registries, RecipeOutput output) {
            return new VRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Vildmark recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        shapeless(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_PLANKS, 4)
                .requires(VBlocks.PINE_LOG)
                .group("planks")
                .unlockedBy("has_log", has(VBlocks.PINE_LOG))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_SLAB, 6)
                .pattern("###")
                .define('#', VBlocks.PINE_PLANKS)
                .group("wooden_slab")
                .unlockedBy("has_planks", has(VBlocks.PINE_PLANKS))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, VBlocks.PINE_STAIRS, 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', VBlocks.PINE_PLANKS)
                .group("wooden_stairs")
                .unlockedBy("has_planks", has(VBlocks.PINE_PLANKS))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.WOOL.white(), 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', VItems.COTTON_GRASS_ITEM)
                .unlockedBy("has_cotton_grass", has(VItems.COTTON_GRASS_ITEM))
                .save(output);

        shaped(RecipeCategory.FOOD, VItems.BLUEBERRY_PIE_ITEM)
                .pattern("###")
                .pattern("@%@")
                .pattern("???")
                .define('#', VItems.BLUEBERRIES_ITEM)
                .define('%', Items.EGG)
                .define('@', Items.SUGAR)
                .define('?', Items.WHEAT)
                .unlockedBy("has_blueberries", has(VItems.BLUEBERRIES_ITEM))
                .save(output);


        cookFood(VItems.PIKE.get(), VItems.COOKED_PIKE.get());
        cookFood(VItems.ROACH.get(), VItems.COOKED_ROACH.get());

        // Same experience and times as vanilla iron ore. The blast furnace takes half as long
        Ingredient peatIronOre = Ingredient.of(VBlocks.PEAT_IRON_ORE);
        SimpleCookingRecipeBuilder.smelting(peatIronOre, RecipeCategory.MISC,
                        CookingBookCategory.MISC, Items.RAW_IRON,
                        /* experience= */ 0.7F, /* cookingTimeTicks= */ 200)
                .unlockedBy("has_peat_iron_ore", has(VBlocks.PEAT_IRON_ORE))
                .save(output, modId("raw_iron_from_smelting_peat_iron_ore"));
        SimpleCookingRecipeBuilder.blasting(peatIronOre, RecipeCategory.MISC,
                        CookingBookCategory.MISC, Items.RAW_IRON,
                        /* experience= */ 0.7F, /* cookingTimeTicks= */ 100)
                .unlockedBy("has_peat_iron_ore", has(VBlocks.PEAT_IRON_ORE))
                .save(output, modId("raw_iron_from_blasting_peat_iron_ore"));
    }

    // Furnace, smoker and campfire recipes with vanilla's cooking times for raw fish and meat
    private void cookFood(ItemLike raw, ItemLike cooked) {
        String cookedName = getItemName(cooked);
        float experience = 0.35F;
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(raw), RecipeCategory.FOOD,
                        CookingBookCategory.FOOD, cooked, experience, /* cookingTimeTicks= */ 200)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output);
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(raw), RecipeCategory.FOOD, cooked,
                        experience, /* cookingTimeTicks= */ 100)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output, modId(cookedName + "_from_smoking"));
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(raw), RecipeCategory.FOOD, cooked,
                        experience, /* cookingTimeTicks= */ 600)
                .unlockedBy(getHasName(raw), has(raw))
                .save(output, modId(cookedName + "_from_campfire_cooking"));
    }

    private static String modId(String path) {
        return Identifier.fromNamespaceAndPath(Vildmark.MODID, path).toString();
    }
}
