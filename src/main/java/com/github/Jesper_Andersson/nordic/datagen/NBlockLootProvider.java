package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class NBlockLootProvider extends BlockLootSubProvider {
    public NBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(NBlocks.PINE_STAIRS.get());
        add(NBlocks.PINE_SLAB.get(),this::createSlabItemTable);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(NBlocks.PINE_STAIRS.get(), NBlocks.PINE_SLAB.get());
    }
}
