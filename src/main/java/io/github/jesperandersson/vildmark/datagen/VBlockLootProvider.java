package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;


import java.util.Set;

public class VBlockLootProvider extends BlockLootSubProvider {
    public VBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Pine wood
        dropSelf(VBlocks.PINE_LOG.get());
        dropOther(VBlocks.PEELING_PINE_LOG.get(), VItems.PINE_LOG_ITEM.get());
        dropOther(VBlocks.PEELED_PINE_LOG.get(), VItems.PINE_LOG_ITEM.get());
        dropSelf(VBlocks.PINE_PLANKS.get());
        dropSelf(VBlocks.PINE_STAIRS.get());
        add(VBlocks.PINE_SLAB.get(), this::createSlabItemTable);
        dropSelf(VBlocks.SLANTED_FENCE.get());
        add(VBlocks.PINE_LEAVES.get(), block -> createLeavesDrops(block, VBlocks.PINE_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(VBlocks.PINE_SAPLING.get());
        add(VBlocks.PINE_NEEDLES.get(), this::createPetalsDrops);

        dropSelf(VBlocks.BEARD_LICHEN.get());

        // Bog
        dropSelf(VBlocks.PEAT.get());
        dropSelf(VBlocks.PEAT_IRON_ORE.get());
        dropSelf(VBlocks.COTTON_GRASS.get());
        dropSelf(VBlocks.SUNDEW.get());
        // Like cake: drops nothing, so a half-eaten pie can't be picked up whole again
        add(VBlocks.BLUEBERRY_PIE.get(), noDrop());

        // Plants
        add(VBlocks.LINGONBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, VItems.LINGONBERRIES_ITEM.get()));
        add(VBlocks.BLUEBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, VItems.BLUEBERRIES_ITEM.get()));
        add(VBlocks.CLOUDBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, VItems.CLOUDBERRIES_ITEM.get()));
        dropOther(VBlocks.CHANTARELLE.get(), VItems.CHANTARELLE_ITEM.get());
        dropSelf(VBlocks.PINE_CONK.get());
        add(VBlocks.REED.get(), block -> createLowerHalfDrops(block, block));
        add(VBlocks.CATTAIL.get(), block -> createLowerHalfDrops(block, block));
        add(VBlocks.TALL_STINGING_NETTLE.get(), block -> createLowerHalfDrops(block, VItems.STINGING_NETTLE_ITEM.get()));
        dropSelf(VBlocks.STINGING_NETTLE.get());
        dropSelf(VBlocks.SNAKE_FERN.get());
        dropSelf(VBlocks.HEATHER.get());
    }

    // Same as the sweet berry bush, but the shrubs fruit at age 4 instead of 3
    private LootTable.Builder createBerryShrubDrops(Block block, ItemLike berries) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        // Silk touch or shears drop the shrub itself, otherwise only the berries drop
        return createSilkTouchOrShearsDispatchTable(block, AlternativesEntry.alternatives(
                applyExplosionDecay(block, LootItem.lootTableItem(berries)
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.AGE_4, 4)))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))),
                applyExplosionDecay(block, LootItem.lootTableItem(berries)
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.AGE_4, 3)))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE))))));
    }


    // Two-tall plants only drop from the lower half so breaking them never gives two items
    private LootTable.Builder createLowerHalfDrops(Block block, ItemLike drop) {
        return LootTable.lootTable().withPool(applyExplosionCondition(drop, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)))
                .add(LootItem.lootTableItem(drop))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Vildmark.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
