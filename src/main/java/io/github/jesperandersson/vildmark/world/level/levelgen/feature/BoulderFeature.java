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

/** A glacial boulder: a few overlapping, slightly flattened ellipsoids sunk a random amount into the ground. */
public class BoulderFeature extends Feature<BoulderConfiguration> {
    // Writes are limited to the 3x3 chunks around the origin chunk
    private static final int MAX_REACH = 15;

    public BoulderFeature(Codec<BoulderConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BoulderConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BoulderConfiguration config = context.config();

        int r = config.radius().sample(random);
        float rx = r + random.nextFloat();
        float ry = 0.7f * r + random.nextFloat();
        float rz = r + random.nextFloat();
        BlockPos centre = context.origin().below(Mth.floor(ry * config.sink().sample(random)));

        // Main body plus 1-2 smaller lumps so the outline isn't a perfect ellipsoid
        int lumps = 1 + random.nextInt(2);
        float[][] blobs = new float[1 + lumps][];
        blobs[0] = new float[]{0, 0, 0, rx, ry, rz};
        for (int i = 1; i < blobs.length; i++) {
            float scale = 0.5f + random.nextFloat() * 0.25f;
            blobs[i] = new float[]{
                    (random.nextFloat() * 2 - 1) * r * 0.5f,
                    (random.nextFloat() - 0.5f) * ry * 0.5f,
                    (random.nextFloat() * 2 - 1) * r * 0.5f,
                    rx * scale, ry * scale, rz * scale};
        }

        int reach = Math.min(MAX_REACH, Mth.ceil(r * 1.5f) + 1);
        int reachY = Mth.ceil(ry * 1.5f) + 1;
        int width = 2 * reach + 1;
        // Highest placed dy per column, for the top cover
        int[] tops = new int[width * width];
        Arrays.fill(tops, Integer.MIN_VALUE);
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reachY; dy <= reachY; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (!inside(blobs, dx, dy, dz, random)) continue;
                    pos.setWithOffset(centre, dx, dy, dz);
                    if (level.isOutsideBuildHeight(pos)) continue;
                    BlockState existing = level.getBlockState(pos);
                    if (existing.is(Blocks.BEDROCK) || existing.hasBlockEntity()) continue;
                    setBlock(level, pos, config.state().getState(random, pos));
                    tops[(dx + reach) * width + dz + reach] = dy;
                    placed = true;
                }
            }
        }

        if (placed && config.top().isPresent() && random.nextFloat() < config.topChance()) {
            BlockStateProvider top = config.top().get();
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    int dy = tops[(dx + reach) * width + dz + reach];
                    if (dy == Integer.MIN_VALUE) continue;
                    pos.setWithOffset(centre, dx, dy, dz);
                    BlockPos above = pos.above();
                    // Skip columns buried under terrain
                    if (level.isOutsideBuildHeight(above) || !level.isEmptyBlock(above)) continue;
                    BlockState cover = top.getState(random, pos);
                    if (cover.isAir()) continue;
                    if (cover.isSolidRender(level, pos)) {
                        setBlock(level, pos, cover);
                    } else if (cover.canSurvive(level, above)) {
                        setBlock(level, above, cover);
                    }
                }
            }
        }
        return placed;
    }

    private static boolean inside(float[][] blobs, int dx, int dy, int dz, RandomSource random) {
        // Slightly jittered edge for a rougher surface
        float threshold = 1.0f - random.nextFloat() * 0.15f;
        for (float[] b : blobs) {
            float x = (dx - b[0]) / b[3];
            float y = (dy - b[1]) / b[4];
            float z = (dz - b[2]) / b[5];
            if (x * x + y * y + z * z <= threshold) return true;
        }
        return false;
    }
}
