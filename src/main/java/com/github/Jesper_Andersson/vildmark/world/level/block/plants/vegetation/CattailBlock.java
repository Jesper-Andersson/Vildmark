package com.github.Jesper_Andersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.DoublePlantBlock;

/** Cattail (kaveldun); grows like reed, in shallow water or on wet soil. */
public class CattailBlock extends ReedBlock {
    public static final MapCodec<CattailBlock> CODEC = simpleCodec(CattailBlock::new);

    public CattailBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }
}
