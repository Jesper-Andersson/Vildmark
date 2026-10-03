package com.github.Jesper_Andersson.nordic.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

/**
 * Backport of 1.21.5's place_on_ground decorator: scatters a block (e.g. leaf litter) on the ground around the trunk.
 */
public class PlaceOnGroundDecorator extends TreeDecorator {
    public static final MapCodec<PlaceOnGroundDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(decorator -> decorator.tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").orElse(2).forGetter(decorator -> decorator.radius),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height").orElse(1).forGetter(decorator -> decorator.height),
            BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter(decorator -> decorator.blockStateProvider)
    ).apply(instance, PlaceOnGroundDecorator::new));

    private final int tries;
    private final int radius;
    private final int height;
    private final BlockStateProvider blockStateProvider;

    public PlaceOnGroundDecorator(int tries, int radius, int height, BlockStateProvider blockStateProvider) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return NTreeDecorators.PLACE_ON_GROUND.get();
    }

    @Override
    public void place(Context context) {
        List<BlockPos> base = lowestTrunkOrRoots(context);
        if (base.isEmpty()) return;

        int y = base.getFirst().getY();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : base) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        BoundingBox area = new BoundingBox(minX, y, minZ, maxX, y, maxZ).inflatedBy(radius, height, radius);
        RandomSource random = context.random();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < tries; i++) {
            pos.set(random.nextIntBetweenInclusive(area.minX(), area.maxX()),
                    random.nextIntBetweenInclusive(area.minY(), area.maxY()),
                    random.nextIntBetweenInclusive(area.minZ(), area.maxZ()));
            attemptToPlaceBlockAbove(context, pos);
        }
    }

    private void attemptToPlaceBlockAbove(Context context, BlockPos pos) {
        BlockPos above = pos.above();
        if (context.level().isStateAtPosition(above, state -> state.isAir() || state.is(Blocks.VINE))
                && context.level().isStateAtPosition(pos, state -> state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
                // Only on the open ground, not under overhangs or inside caves
                && context.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= above.getY()) {
            BlockState state = blockStateProvider.getState(context.random(), above);
            // Random rotation so the segments don't all line up
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.Plane.HORIZONTAL.getRandomDirection(context.random()));
            }
            context.setBlock(above, state);
        }
    }

    // Same base selection as AlterGroundDecorator: the bottom layer of the trunk, or the roots if they're lower
    private static List<BlockPos> lowestTrunkOrRoots(Context context) {
        List<BlockPos> roots = context.roots();
        List<BlockPos> logs = context.logs();
        List<BlockPos> candidates = roots.isEmpty() ? logs : roots;
        if (candidates.isEmpty()) return List.of();
        int lowest = candidates.getFirst().getY();
        return candidates.stream().filter(pos -> pos.getY() == lowest).toList();
    }
}
