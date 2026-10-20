package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
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

public class NBlockLootProvider extends BlockLootSubProvider {
    public NBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Pine wood
        dropSelf(NBlocks.PINE_LOG.get());
        dropOther(NBlocks.PEELING_PINE_LOG.get(), NItems.PINE_LOG_ITEM.get());
        dropOther(NBlocks.PEELED_PINE_LOG.get(), NItems.PINE_LOG_ITEM.get());
        dropSelf(NBlocks.PINE_PLANKS.get());
        dropSelf(NBlocks.PINE_STAIRS.get());
        add(NBlocks.PINE_SLAB.get(), this::createSlabItemTable);
        dropSelf(NBlocks.SLANTED_FENCE.get());
        add(NBlocks.PINE_LEAVES.get(), block -> createLeavesDrops(block, NBlocks.PINE_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(NBlocks.PINE_SAPLING.get());
        add(NBlocks.PINE_NEEDLES.get(), this::createPetalsDrops);

        dropSelf(NBlocks.BEARD_LICHEN.get());

        // Bog
        dropSelf(NBlocks.PEAT.get());
        dropSelf(NBlocks.PEAT_IRON_ORE.get());
        dropSelf(NBlocks.COTTON_GRASS.get());
        dropSelf(NBlocks.SUNDEW.get());

        // Plants
        add(NBlocks.LINGONBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, NItems.LINGONBERRIES_ITEM.get()));
        add(NBlocks.BLUEBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, NItems.BLUEBERRIES_ITEM.get()));
        add(NBlocks.CLOUDBERRY_SHRUB.get(), block -> createBerryShrubDrops(block, NItems.CLOUDBERRIES_ITEM.get()));
        dropOther(NBlocks.CHANTARELLE.get(), NItems.CHANTARELLE_ITEM.get());
        dropSelf(NBlocks.PINE_CONK.get());
        add(NBlocks.REED.get(), block -> createLowerHalfDrops(block, block));
        add(NBlocks.CATTAIL.get(), block -> createLowerHalfDrops(block, block));
        add(NBlocks.TALL_STINGING_NETTLE.get(), block -> createLowerHalfDrops(block, NItems.STINGING_NETTLE_ITEM.get()));
        dropSelf(NBlocks.STINGING_NETTLE.get());
        dropSelf(NBlocks.SNAKE_FERN.get());
        dropSelf(NBlocks.HEATHER.get());
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
        return Nordic.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
