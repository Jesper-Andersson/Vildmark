package io.github.jesperandersson.vildmark.world.level.levelgen.feature.treedecorators;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BeardLichenBlock;
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
 * Hangs strands of beard lichen under the tree's leaves and down the sides of its trunk.
 */
public class BeardLichenDecorator extends TreeDecorator {
    // Reads the decorator's settings from the tree's configured feature JSON
    public static final MapCodec<BeardLichenDecorator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0f, 1.0f).fieldOf("leaves_probability")
                            .forGetter(decorator -> decorator.leavesProbability),
                    Codec.floatRange(0.0f, 1.0f).fieldOf("logs_probability")
                            .forGetter(decorator -> decorator.logsProbability),
                    Codec.intRange(1, BeardLichenBlock.MAX_LENGTH).fieldOf("max_length")
                            .orElse(BeardLichenBlock.MAX_LENGTH)
                            .forGetter(decorator -> decorator.maxLength)
            ).apply(instance, BeardLichenDecorator::new));

    private final float leavesProbability;
    private final float logsProbability;
    private final int maxLength;

    public BeardLichenDecorator(float leavesProbability, float logsProbability, int maxLength) {
        this.leavesProbability = leavesProbability;
        this.logsProbability = logsProbability;
        this.maxLength = maxLength;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return VTreeDecorators.BEARD_LICHEN.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        for (BlockPos leaf : context.leaves()) {
            BlockPos below = leaf.below();
            if (random.nextFloat() < leavesProbability && context.isAir(below)) {
                hangStrand(context, below, Direction.UP);
            }
        }
        for (BlockPos log : context.logs()) {
            // logs() also holds the dirt under the trunk
            if (!context.level().isStateAtPosition(log, state -> state.is(BlockTags.LOGS))) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = log.relative(direction);
                if (random.nextFloat() < logsProbability && context.isAir(side)) {
                    // The log is opposite the offset, so the lichen faces back toward it
                    hangStrand(context, side, direction.getOpposite());
                }
            }
        }
    }

    // Hangs a strand of random length down from top, cut short where it would hit a block
    private void hangStrand(Context context, BlockPos top, Direction facing) {
        int wantedLength = 1 + context.random().nextInt(maxLength);
        BlockPos.MutableBlockPos pos = top.mutable();
        int length = 0;
        while (length < wantedLength && context.isAir(pos)) {
            length++;
            pos.move(Direction.DOWN);
        }

        pos.set(top);
        BlockState lichen = VBlocks.BEARD_LICHEN.get().defaultBlockState()
                .setValue(BeardLichenBlock.FACING, facing);
        for (int blockIdx = 0; blockIdx < length; blockIdx++) {
            boolean isTip = blockIdx == length - 1;
            context.setBlock(pos.immutable(), lichen.setValue(BeardLichenBlock.TIP, isTip));
            pos.move(Direction.DOWN);
        }
    }
}
