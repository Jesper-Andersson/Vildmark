package com.github.Jesper_Andersson.nordic.world.level.levelgen.feature.treedecorators;

import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.PineConkMushroomBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Grows pine conks on the sides of the tree's trunk, some of them already fully grown.
 */
public class PineConkDecorator extends TreeDecorator {
    public static final MapCodec<PineConkDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0.0f, 1.0f).fieldOf("probability").forGetter(decorator -> decorator.probability),
            Codec.floatRange(0.0f, 1.0f).fieldOf("large_probability").orElse(0.5f).forGetter(decorator -> decorator.largeProbability)
    ).apply(instance, PineConkDecorator::new));

    private final float probability;
    private final float largeProbability;

    public PineConkDecorator(float probability, float largeProbability) {
        this.probability = probability;
        this.largeProbability = largeProbability;
    }

    /** A conk stuck to the log on its {@code log} side, large with the given chance. */
    public static BlockState conk(Direction log, float largeProbability, RandomSource random) {
        return NBlocks.PINE_CONK.get().defaultBlockState()
                .setValue(PineConkMushroomBlock.FACING, log)
                .setValue(PineConkMushroomBlock.AGE, random.nextFloat() < largeProbability ? PineConkMushroomBlock.MAX_AGE : 0);
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return NTreeDecorators.PINE_CONK.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        for (BlockPos log : context.logs()) {
            // logs() also holds the dirt under the trunk
            if (!context.level().isStateAtPosition(log, state -> state.is(BlockTags.LOGS))) continue;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = log.relative(direction);
                if (random.nextFloat() < probability && context.isAir(side)) {
                    // The log is opposite the offset, so the conk faces back toward it
                    context.setBlock(side, conk(direction.getOpposite(), largeProbability, random));
                }
            }
        }
    }
}
