package com.github.Jesper_Andersson.vildmark.world.level.levelgen.feature;

import com.github.Jesper_Andersson.vildmark.world.level.levelgen.feature.treedecorators.PineBarkDecorator;
import com.github.Jesper_Andersson.vildmark.world.level.levelgen.feature.treedecorators.PineConkDecorator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/** A fallen trunk lying along X or Z, optionally with the stump it broke off from, moss or mushrooms on top and conks on the sides. */
public class FallenTreeFeature extends Feature<FallenTreeConfiguration> {
    // Gap between the stump and the start of the trunk
    private static final int STUMP_GAP = 2;

    public FallenTreeFeature(Codec<FallenTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        FallenTreeConfiguration config = context.config();
        BlockPos origin = context.origin();

        boolean stump = random.nextFloat() < config.stumpChance();
        int length = config.length().sample(random);
        if (stump && isBlocked(level, origin)) return false;

        // Try the four directions from a random start, so trees and slopes in one direction don't stop the trunk
        Direction direction = null;
        Direction candidate = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int turn = 0; turn < 4 && direction == null; turn++, candidate = candidate.getClockWise()) {
            if (fits(level, stump ? origin.relative(candidate, STUMP_GAP) : origin, candidate, length)) {
                direction = candidate;
            }
        }
        if (direction == null) return false;
        BlockPos start = stump ? origin.relative(direction, STUMP_GAP) : origin;

        if (stump) {
            level.setBlock(origin, config.log().getState(random, origin), Block.UPDATE_CLIENTS);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                placeConk(level, origin, side, config, random);
            }
        }
        // Index of the bark transition log, counted from the stump end; -1 when the trunk doesn't blend
        boolean blend = config.transitionLog().isPresent() && config.peeledLog().isPresent() && length >= 3;
        int bark = blend ? PineBarkDecorator.barkLength(length, config.barkFraction(), 1, random) : -1;
        for (int i = 0; i < length; i++) {
            BlockPos pos = start.relative(direction, i);
            BlockStateProvider provider = bark < 0 || i < bark ? config.log()
                    : i == bark ? config.transitionLog().get() : config.peeledLog().get();
            BlockState log = provider.getState(random, pos);
            if (log.hasProperty(BlockStateProperties.AXIS)) {
                log = log.setValue(BlockStateProperties.AXIS, direction.getAxis());
            }
            level.setBlock(pos, log, Block.UPDATE_CLIENTS);

            BlockPos above = pos.above();
            if (random.nextFloat() < config.decorationChance() && level.isEmptyBlock(above)) {
                level.setBlock(above, config.decoration().getState(random, above), Block.UPDATE_CLIENTS);
            }
            // Only the long sides; the ends are the stump gap and the crown
            placeConk(level, pos, direction.getClockWise(), config, random);
            placeConk(level, pos, direction.getCounterClockWise(), config, random);
        }
        return true;
    }

    private static void placeConk(WorldGenLevel level, BlockPos log, Direction side, FallenTreeConfiguration config, RandomSource random) {
        BlockPos pos = log.relative(side);
        if (random.nextFloat() < config.conkChance() && level.isEmptyBlock(pos)) {
            level.setBlock(pos, PineConkDecorator.conk(side.getOpposite(), config.largeConkChance(), random), Block.UPDATE_CLIENTS);
        }
    }

    /** Whether the whole trunk fits, checked up front so it is never left floating or half placed. */
    private static boolean fits(WorldGenLevel level, BlockPos start, Direction direction, int length) {
        for (int i = 0; i < length; i++) {
            if (isBlocked(level, start.relative(direction, i))) return false;
        }
        return true;
    }

    /** Whether a log can't go here: the spot is taken, or there's no solid ground under it. */
    private static boolean isBlocked(WorldGenLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).canBeReplaced()) return true;
        BlockPos below = pos.below();
        return !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
