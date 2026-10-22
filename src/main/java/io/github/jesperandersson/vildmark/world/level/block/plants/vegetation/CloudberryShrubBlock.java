package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CloudberryShrubBlock extends ShrubClass {
    public static final MapCodec<CloudberryShrubBlock> CODEC =
            simpleCodec(CloudberryShrubBlock::new);

    public CloudberryShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected Item berryItem() {
        return VItems.CLOUDBERRIES_ITEM.get();
    }

    // Only grows on wet bog ground
    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos groundPos) {
        return ground.is(VBlocks.PEAT.get())
                || ground.is(Blocks.MUD)
                || ground.is(Blocks.MOSS_BLOCK);
    }

    // TODO: Bone meal doesn't grow cloudberries yet, see ShrubClass.performBonemeal
}
