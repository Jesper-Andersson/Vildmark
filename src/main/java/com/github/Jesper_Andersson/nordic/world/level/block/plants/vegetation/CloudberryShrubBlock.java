package com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation;

import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CloudberryShrubBlock extends ShrubClass {
    public static final MapCodec<CloudberryShrubBlock> CODEC = simpleCodec(CloudberryShrubBlock::new);

    public CloudberryShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected Item berryItem() {
        return NItems.CLOUDBERRIES_ITEM.get();
    }

    // Only grows on wet bog ground
    @Override
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return pState.is(NBlocks.PEAT.get()) || pState.is(Blocks.MUD) || pState.is(Blocks.MOSS_BLOCK);
    }
}
