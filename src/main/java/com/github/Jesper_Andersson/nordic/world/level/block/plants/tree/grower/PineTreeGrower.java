package com.github.Jesper_Andersson.nordic.world.level.block.plants.tree.grower;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PineTreeGrower extends AbstractTreeGrower {
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRand, boolean pHasFlowers) {
        if (pRand.nextInt(10) == 0) {
            return pHasFlowers ? TreeFeatures.FANCY_OAK_BEES_005 : TreeFeatures.FANCY_OAK;
        } else {
            return pHasFlowers ? TreeFeatures.OAK_BEES_005 : TreeFeatures.OAK;
        }
    }
}