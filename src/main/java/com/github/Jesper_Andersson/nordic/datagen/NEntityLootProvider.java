package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.world.entity.NEntities;
import com.github.Jesper_Andersson.nordic.world.item.NItems;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

import static com.github.Jesper_Andersson.nordic.Nordic.ENTITY_TYPES;

public class NEntityLootProvider extends EntityLootSubProvider {
    public NEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(NEntities.BARROW_WIGHT.get(), LootTable.lootTable()
                .withPool(lootingPool(Items.BONE, 0.0F, 2.0F))
                .withPool(lootingPool(Items.ROTTEN_FLESH, 0.0F, 2.0F)));

        add(NEntities.MOOSE.get(), LootTable.lootTable()
                .withPool(lootingPool(Items.LEATHER, 1.0F, 3.0F))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.BEEF)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                .apply(SmeltItemFunction.smelted().when(shouldSmeltLoot()))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F))))));

        add(NEntities.PIKE.get(), createFishTable(NItems.PIKE.get()));
        add(NEntities.ROACH.get(), createFishTable(NItems.ROACH.get()));

        // The tomte drops nothing
        add(NEntities.TOMTE.get(), LootTable.lootTable());
    }

    private LootPool.Builder lootingPool(ItemLike item, float min, float max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F))));
    }

    // Like vanilla cod, but only cooked when the fish itself was burning
    private static LootTable.Builder createFishTable(ItemLike fish) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(fish)
                                .apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                        EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)))))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.BONE_MEAL))
                        .when(LootItemRandomChanceCondition.randomChance(0.05F)));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ENTITY_TYPES.getEntries().stream().map(Holder::value);
    }
}
