package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NBlockStateProvider extends BlockStateProvider {
    public NBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper){
        super(output, Nordic.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation planks = blockTexture(NBlocks.PINE_PLANKS.get());

        stairsBlock(NBlocks.PINE_STAIRS.get(), planks);
        slabBlock(NBlocks.PINE_SLAB.get(), planks, planks);

        itemModels().withExistingParent("pine_stairs", modLoc("block/pine_stairs"));
        itemModels().withExistingParent("pine_slab", modLoc("block/pine_slab"));
    }
}
