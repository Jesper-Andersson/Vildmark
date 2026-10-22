package io.github.jesperandersson.vildmark.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

/**
 * Scots pine bark: the lower part of the trunk keeps its rough bark, then one transition log, then peeled red logs
 * up to the crown.
 */
public class PineBarkDecorator extends TreeDecorator {
    public static final MapCodec<PineBarkDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("transition_provider").forGetter(decorator -> decorator.transitionProvider),
            BlockStateProvider.CODEC.fieldOf("peeled_provider").forGetter(decorator -> decorator.peeledProvider),
            Codec.floatRange(0.0f, 1.0f).fieldOf("bark_fraction").forGetter(decorator -> decorator.barkFraction),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("variation").orElse(1).forGetter(decorator -> decorator.variation)
    ).apply(instance, PineBarkDecorator::new));

    private final BlockStateProvider transitionProvider;
    private final BlockStateProvider peeledProvider;
    private final float barkFraction;
    private final int variation;

    public PineBarkDecorator(BlockStateProvider transitionProvider, BlockStateProvider peeledProvider, float barkFraction, int variation) {
        this.transitionProvider = transitionProvider;
        this.peeledProvider = peeledProvider;
        this.barkFraction = barkFraction;
        this.variation = variation;
    }

    /**
     * How many logs from the base keep their bark: the fraction of the height, give or take {@code variation}, but
     * always leaving room for at least one bark log, the transition log and one peeled log. Needs a height of 3 or more.
     */
    public static int barkLength(int height, float fraction, int variation, RandomSource random) {
        int bark = Math.round(height * fraction) + random.nextIntBetweenInclusive(-variation, variation);
        return Mth.clamp(bark, 1, height - 2);
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return VTreeDecorators.PINE_BARK.get();
    }

    @Override
    public void place(Context context) {
        // logs() also holds the grass the trunk placer turned into dirt under the trunk, so keep only real logs
        List<BlockPos> logs = context.logs().stream()
                .filter(pos -> context.level().isStateAtPosition(pos, state -> state.is(BlockTags.LOGS)))
                .toList();
        if (logs.isEmpty()) return;

        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (BlockPos pos : logs) {
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
        }
        int height = maxY - minY + 1;
        if (height < 3) return;

        RandomSource random = context.random();
        int bark = barkLength(height, barkFraction, variation, random);
        for (BlockPos pos : logs) {
            int rel = pos.getY() - minY;
            if (rel < bark) continue;
            BlockState state = (rel == bark ? transitionProvider : peeledProvider).getState(random, pos);
            context.setBlock(pos, copyAxis(context, pos, state));
        }
    }

    // The level is only readable through isStateAtPosition, so find the replaced log's axis by testing each one
    private static BlockState copyAxis(Context context, BlockPos pos, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.AXIS)) return state;
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (context.level().isStateAtPosition(pos, existing -> existing.hasProperty(BlockStateProperties.AXIS)
                    && existing.getValue(BlockStateProperties.AXIS) == axis)) {
                return state.setValue(BlockStateProperties.AXIS, axis);
            }
        }
        return state;
    }
}
