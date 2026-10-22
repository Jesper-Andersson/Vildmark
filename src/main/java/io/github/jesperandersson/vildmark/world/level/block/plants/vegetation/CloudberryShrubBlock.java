package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
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
        return VItems.CLOUDBERRIES_ITEM.get();
    }

    // Only grows on wet bog ground
    @Override
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return pState.is(VBlocks.PEAT.get()) || pState.is(Blocks.MUD) || pState.is(Blocks.MOSS_BLOCK);
    }

    //TODO: Fix bonemealing
}
