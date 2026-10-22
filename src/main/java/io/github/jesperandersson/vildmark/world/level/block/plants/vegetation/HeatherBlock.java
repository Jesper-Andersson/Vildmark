package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;

/** Heather (ljung); placeholder that reuses the vanilla short grass texture with a purple tint. */
public class HeatherBlock extends VegetationBlock {
    public static final MapCodec<HeatherBlock> CODEC = simpleCodec(HeatherBlock::new);

    public HeatherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
