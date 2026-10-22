package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A small plant that grows on bog ground (peat, mud, moss) and the dirt blocks around it. */
public class BogPlantBlock extends VegetationBlock {
    public static final MapCodec<BogPlantBlock> CODEC = simpleCodec(BogPlantBlock::new);

    public BogPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos groundPos) {
        boolean isBogGround = ground.is(VBlocks.PEAT.get())
                || ground.is(Blocks.MUD)
                || ground.is(Blocks.MOSS_BLOCK);
        boolean isDirtGround = ground.is(Blocks.GRASS_BLOCK)
                || ground.is(Blocks.DIRT)
                || ground.is(Blocks.PODZOL);
        return isBogGround || isDirtGround;
    }
}
