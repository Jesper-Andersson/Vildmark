package io.github.jesperandersson.vildmark.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Arrays;

/**
 * A glacial boulder: a few overlapping, slightly flattened ellipsoids sunk a random amount into the
 * ground.
 */
public class BoulderFeature extends Feature<BoulderConfiguration> {
    // Writes are limited to the 3x3 chunks around the origin chunk
    private static final int MAX_REACH = 15;
    // Marks a column of the tops array that got no boulder blocks
    private static final int NO_TOP = Integer.MIN_VALUE;

    public BoulderFeature(Codec<BoulderConfiguration> codec) {
        super(codec);
    }

    /** An ellipsoid, as an offset from the boulder's centre and a radius along each axis. */
    private record Blob(
            float offsetX, float offsetY, float offsetZ,
            float radiusX, float radiusY, float radiusZ) {}

    @Override
    public boolean place(FeaturePlaceContext<BoulderConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BoulderConfiguration config = context.config();

        int radius = config.radius().sample(random);
        float radiusX = radius + random.nextFloat();
        float radiusY = 0.7f * radius + random.nextFloat();
        float radiusZ = radius + random.nextFloat();
        int sinkDepth = Mth.floor(radiusY * config.sink().sample(random));
        BlockPos centre = context.origin().below(sinkDepth);

        // Main body plus 1-2 smaller lumps so the outline isn't a perfect ellipsoid
        int lumpCount = 1 + random.nextInt(2);
        Blob[] blobs = new Blob[1 + lumpCount];
        blobs[0] = new Blob(0, 0, 0, radiusX, radiusY, radiusZ);
        for (int blobIdx = 1; blobIdx < blobs.length; blobIdx++) {
            float scale = 0.5f + random.nextFloat() * 0.25f;
            // Up to half the radius away from the centre
            blobs[blobIdx] = new Blob(
                    (random.nextFloat() * 2 - 1) * radius * 0.5f,
                    (random.nextFloat() - 0.5f) * radiusY * 0.5f,
                    (random.nextFloat() * 2 - 1) * radius * 0.5f,
                    radiusX * scale, radiusY * scale, radiusZ * scale);
        }

        // How far to scan from the centre: far enough to cover every blob
        int reach = Math.min(MAX_REACH, Mth.ceil(radius * 1.5f) + 1);
        int reachY = Mth.ceil(radiusY * 1.5f) + 1;
        int width = 2 * reach + 1;
        // Highest placed dy per column, for the top cover
        int[] tops = new int[width * width];
        Arrays.fill(tops, NO_TOP);
        boolean isPlaced = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reachY; dy <= reachY; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (!isInside(blobs, dx, dy, dz, random)) {
                        continue;
                    }
                    pos.setWithOffset(centre, dx, dy, dz);
                    if (level.isOutsideBuildHeight(pos)) {
                        continue;
                    }
                    BlockState existing = level.getBlockState(pos);
                    if (existing.is(Blocks.BEDROCK) || existing.hasBlockEntity()) {
                        continue;
                    }
                    setBlock(level, pos, config.state().getState(level, random, pos));
                    tops[columnIndex(dx, dz, reach)] = dy;
                    isPlaced = true;
                }
            }
        }

        if (isPlaced && config.top().isPresent() && random.nextFloat() < config.topChance()) {
            placeTopCover(level, random, config.top().get(), centre, tops, reach);
        }
        return isPlaced;
    }

    // Covers the top block of each column: full blocks replace it, anything else goes on top
    private void placeTopCover(
            WorldGenLevel level, RandomSource random, BlockStateProvider top, BlockPos centre,
            int[] tops, int reach) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int dy = tops[columnIndex(dx, dz, reach)];
                if (dy == NO_TOP) {
                    continue;
                }
                pos.setWithOffset(centre, dx, dy, dz);
                BlockPos above = pos.above();
                // Skip columns buried under terrain
                if (level.isOutsideBuildHeight(above) || !level.isEmptyBlock(above)) {
                    continue;
                }
                BlockState cover = top.getState(level, random, pos);
                if (cover.isAir()) {
                    continue;
                }
                if (cover.isSolidRender()) {
                    setBlock(level, pos, cover);
                } else if (cover.canSurvive(level, above)) {
                    setBlock(level, above, cover);
                }
            }
        }
    }

    // Index into the tops array of the column at offset (dx, dz) from the centre
    private static int columnIndex(int dx, int dz, int reach) {
        int width = 2 * reach + 1;
        return (dx + reach) * width + dz + reach;
    }

    private static boolean isInside(Blob[] blobs, int dx, int dy, int dz, RandomSource random) {
        // Slightly jittered edge for a rougher surface
        float threshold = 1.0f - random.nextFloat() * 0.15f;
        for (Blob blob : blobs) {
            // The offset scaled so the ellipsoid becomes a unit sphere
            float x = (dx - blob.offsetX()) / blob.radiusX();
            float y = (dy - blob.offsetY()) / blob.radiusY();
            float z = (dz - blob.offsetZ()) / blob.radiusZ();
            if (x * x + y * y + z * z <= threshold) {
                return true;
            }
        }
        return false;
    }
}
