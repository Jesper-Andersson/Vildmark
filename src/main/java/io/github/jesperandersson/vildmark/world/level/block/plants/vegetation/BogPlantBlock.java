package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A small plant that grows on bog ground (peat, mud, moss) and the dirt blocks around it. */
public class BogPlantBlock extends BushBlock {
    public static final MapCodec<BogPlantBlock> CODEC = simpleCodec(BogPlantBlock::new);

    public BogPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return pState.is(VBlocks.PEAT.get()) || pState.is(Blocks.MUD) || pState.is(Blocks.MOSS_BLOCK)
                || pState.is(Blocks.GRASS_BLOCK) || pState.is(Blocks.DIRT) || pState.is(Blocks.PODZOL);
    }
}
