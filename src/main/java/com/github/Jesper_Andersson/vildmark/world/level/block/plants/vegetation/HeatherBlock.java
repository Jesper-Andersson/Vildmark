package com.github.Jesper_Andersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;

/** Heather (ljung); placeholder that reuses the vanilla short grass texture with a purple tint. */
public class HeatherBlock extends BushBlock {
    public static final MapCodec<HeatherBlock> CODEC = simpleCodec(HeatherBlock::new);

    public HeatherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
