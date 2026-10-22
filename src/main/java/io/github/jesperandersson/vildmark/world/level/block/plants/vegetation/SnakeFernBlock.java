package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.VegetationBlock;

/** Snake fern; grows on the default dirt tag. */
public class SnakeFernBlock extends VegetationBlock {
    public static final MapCodec<SnakeFernBlock> CODEC = simpleCodec(SnakeFernBlock::new);

    public SnakeFernBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }
}
