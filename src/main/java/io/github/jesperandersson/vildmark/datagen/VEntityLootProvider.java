package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.item.VItems;
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

import static io.github.jesperandersson.vildmark.Vildmark.ENTITY_TYPES;

public class VEntityLootProvider extends EntityLootSubProvider {
    public VEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        add(VEntities.BARROW_WIGHT.get(), LootTable.lootTable()
                .withPool(lootingPool(Items.BONE, 0.0F, 2.0F, 0f))
                .withPool(lootingPool(Items.ROTTEN_FLESH, 0.0F, 2.0F, 0f)));

        add(VEntities.MOOSE.get(), LootTable.lootTable()
                .withPool(lootingPool(Items.LEATHER, 1.0F, 3.0F, 0f))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.BEEF)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                .apply(SmeltItemFunction.smelted().when(shouldSmeltLoot()))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries, UniformGenerator.between(0.0F, 1.0F))))));

        add(VEntities.PIKE.get(), createFishTable(VItems.PIKE.get()));
        add(VEntities.ROACH.get(), createFishTable(VItems.ROACH.get()));

        // The tomte drops nothing
        add(VEntities.TOMTE.get(), LootTable.lootTable());

        add(VEntities.WILL_O_THE_WISP.get(),LootTable.lootTable().withPool(lootingPool(Items.LANTERN, 0.0f, 1,0f)));
    }

    private LootPool.Builder lootingPool(ItemLike item, float min, float max, float v) {
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
