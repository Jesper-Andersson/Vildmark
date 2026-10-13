package com.github.Jesper_Andersson.nordic.world.level.levelgen.lake;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.compat.BiomePickCompat;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.Biomes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Reshapes the overworld terrain wherever the biome source places {@link Biomes#LAKE} or {@link Biomes#RIVER},
 * so the aquifer fills it with water, and flattens {@link Biomes#BOG} into mud ground flush with the water
 * mixed with small pools, about half of it water, and a few one block peaks. Biomes don't shape terrain on their own,
 * hence the router wrapping.
 * Once the terrain is final the chunk biomes are fixed up: bog quarts whose final surface rises more than 2 blocks
 * above the water anywhere the quart's biome reaches (hills, cliffs, slopes) and lake or river quarts left without any
 * water get a forest biome, and Nordic land biomes that ended up in open water get a cold vanilla ocean.
 * <p>
 * Every quart column gets a target surface height. Inside the water it sits below sea level and gets deeper
 * further from the shore. In a bog it is flat ground level with the water surface.
 * Outside both it is a beach rim just above sea level that blends back into the original surface height.
 * The terrain density is then pulled towards a surface between the original height and that height, by the blend weight.
 * In the bog interior {@link BogRelief} then digs pools a few blocks wide into it per block and raises the middle of the
 * dry patches by one block. Water and bog report a preliminary surface well below sea level, so the aquifer fills the
 * basins and pools with sea level water.
 */
public final class LakeBasin {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Nordic.MODID);

    // Only ever created in code, but density function types need a registered codec
    private static final MapCodec<ShoreView> LAKE_FLOOR_CODEC = MapCodec.unit(() -> {
        throw new UnsupportedOperationException("nordic:lake_floor can't be created from data");
    });
    private static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ShoreView>> LAKE_FLOOR =
            DENSITY_FUNCTION_TYPES.register("lake_floor", () -> LAKE_FLOOR_CODEC);
    private static final MapCodec<BogRelief> BOG_RELIEF_CODEC = MapCodec.unit(() -> {
        throw new UnsupportedOperationException("nordic:bog_relief can't be created from data");
    });
    private static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<BogRelief>> BOG_RELIEF =
            DENSITY_FUNCTION_TYPES.register("bog_relief", () -> BOG_RELIEF_CODEC);

    // Threshold NoiseChunk uses when searching for the preliminary surface level
    private static final double SURFACE_THRESHOLD = 0.390625;

    // Humidity where Humidity.WET starts, the forest fallback is spruce pine forest from here on
    private static final double WET_HUMIDITY = 0.1;

    // Shore field of each wrapped RandomState, so chunk biome filling can see the final terrain
    private static final Map<RandomState, ShoreField> FIELDS = Collections.synchronizedMap(new WeakHashMap<>());

    private LakeBasin() {}

    public static void register(IEventBus modEventBus) {
        DENSITY_FUNCTION_TYPES.register(modEventBus);
    }

    public static NoiseRouter wrapRouter(RandomState randomState, BiomeSource biomeSource, NoiseSettings noiseSettings, int seaLevel) {
        NoiseRouter router = randomState.router();
        int minY = noiseSettings.minY();
        int maxY = minY + noiseSettings.height();
        ShoreField field = new ShoreField(biomeSource, randomState.sampler(), randomState.getOrCreateNoise(Noises.SURFACE),
                router.finalDensity(), router.initialDensityWithoutJaggedness(), minY, maxY, seaLevel);
        FIELDS.put(randomState, field);
        DensityFunction height = new ShoreView(field, ShoreView.Mode.FLOOR);
        DensityFunction surface = new ShoreView(field, ShoreView.Mode.SURFACE);
        DensityFunction weight = new ShoreView(field, ShoreView.Mode.WEIGHT);

        // (H - y) / 8: solid below the target surface, empty above it
        DensityFunction gradient = DensityFunctions.yClampedGradient(minY, maxY, -minY / 8.0, -maxY / 8.0);
        DensityFunction target = DensityFunctions.add(DensityFunctions.flatCache(height), gradient);
        // Same for the preliminary surface the aquifer sees, which sits below sea level in water and bog
        DensityFunction surfaceTarget = DensityFunctions.add(DensityFunctions.flatCache(surface), gradient);

        // The flat cache is constant per quart column, so interpolate the target for the final blend
        DensityFunction smoothTarget = DensityFunctions.interpolated(target);

        // Fade the blend out below the target so caves stay: 1 down to 8 blocks below, 0 from 24 below.
        // The final blend fades on the interpolated target, otherwise the per-quart flat cache cuts it into 4x4 columns
        DensityFunction flatWeight = DensityFunctions.flatCache(weight);
        DensityFunction surfaceWeight = DensityFunctions.mul(
                DensityFunctions.flatCache(new ShoreView(field, ShoreView.Mode.AQUIFER)), fade(surfaceTarget));
        DensityFunction finalWeight = DensityFunctions.mul(DensityFunctions.interpolated(flatWeight), fade(smoothTarget));

        // Per-block pools and peaks in the bog interior. The mask goes first, as MUL skips the second argument where
        // the first is 0, so the swamp noise only runs inside the bog
        DensityFunction bogMask = DensityFunctions.interpolated(DensityFunctions.flatCache(new ShoreView(field, ShoreView.Mode.BOG)));
        DensityFunction relief = DensityFunctions.mul(bogMask, new BogRelief(randomState.getOrCreateNoise(Noises.SWAMP), seaLevel));

        return new NoiseRouter(
                router.barrierNoise(),
                router.fluidLevelFloodednessNoise(),
                router.fluidLevelSpreadNoise(),
                router.lavaNoise(),
                router.temperature(),
                router.vegetation(),
                router.continents(),
                router.erosion(),
                router.depth(),
                router.ridges(),
                // Keep the preliminary surface in the basin, otherwise the aquifer won't use sea level water there.
                // In water and bog it is pushed below sea level so the aquifer floods the basins and shallow pools too
                DensityFunctions.lerp(surfaceWeight, router.initialDensityWithoutJaggedness(),
                        DensityFunctions.add(surfaceTarget, DensityFunctions.constant(SURFACE_THRESHOLD))),
                DensityFunctions.lerp(finalWeight, router.finalDensity(), DensityFunctions.add(smoothTarget, relief)),
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap()
        );
    }

    /**
     * Fixes the chunk biomes once the final terrain is known, per quart and in this order:
     * <ul>
     * <li>A Nordic land biome whose final surface is more than 3 blocks below sea level everywhere the quart's biome
     * reaches sits in open water, so it becomes frozen ocean (tundra) or cold ocean (the rest).</li>
     * <li>A lake or river whose final surface is at or above sea level everywhere the quart's biome reaches has no
     * water (high ground the basin isn't dug into, or edges the weight limit leaves standing), so it becomes forest.</li>
     * <li>A bog whose final surface is more than 2 blocks above the water anywhere the quart's biome reaches (hills,
     * cliffs and slopes the basin leaves standing) becomes forest, so it doesn't get bog ground and vegetation.</li>
     * </ul>
     * The forest is pine forest, or spruce pine forest where wet. Lake and river humidity only covers those two bands.
     * The biome source itself stays unchanged, as the shore field reads it.
     */
    public static BiomeResolver wrapBiomes(RandomState randomState, BiomeResolver resolver) {
        ShoreField field = FIELDS.get(randomState);
        if (field == null) return resolver;
        Holder<Biome> pineForest = findBiome(field.biomeSource, Biomes.PINE_FOREST);
        Holder<Biome> sprucePineForest = findBiome(field.biomeSource, Biomes.SPRUCE_PINE_FOREST);
        Holder<Biome> coldOcean = findBiome(field.biomeSource, net.minecraft.world.level.biome.Biomes.COLD_OCEAN);
        Holder<Biome> frozenOcean = findBiome(field.biomeSource, net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN);
        boolean hasForests = pineForest != null && sprucePineForest != null;
        if (!hasForests && coldOcean == null && frozenOcean == null) return resolver;
        double maxOceanHeight = field.seaLevel - ShoreField.OCEAN_DEPTH;
        double maxBogHeight = field.seaLevel + ShoreField.MAX_BOG_HEIGHT;
        return (x, y, z, sampler) -> {
            Holder<Biome> biome = resolver.getNoiseBiome(x, y, z, sampler);
            boolean water = biome.is(Biomes.LAKE) || biome.is(Biomes.RIVER);
            boolean bog = biome.is(Biomes.BOG);
            boolean tundra = biome.is(Biomes.TUNDRA);
            boolean land = bog || tundra || biome.is(Biomes.PINE_FOREST) || biome.is(Biomes.SPRUCE_PINE_FOREST)
                    || biome.is(Biomes.BIRCH_PINE_FOREST) || biome.is(Biomes.PLAINS);
            if (!water && !land) return biome;
            ShoreField.BiomeHeights heights = field.biomeHeights(x, z);
            // Open water
            if (land && heights.max() < maxOceanHeight) {
                Holder<Biome> ocean = tundra ? frozenOcean : coldOcean;
                if (ocean != null) return ocean;
            }
            if (!hasForests) return biome;
            boolean dry = water && heights.min() >= field.seaLevel;
            boolean raised = bog && heights.max() > maxBogHeight;
            if (!dry && !raised) return biome;
            double humidity = Climate.unquantizeCoord(sampler.sample(x, y, z).humidity());
            return forestFor(humidity, pineForest, sprucePineForest, biome);
        };
    }

    // Pine forest, spruce pine forest where wet. Doesn't bring back a disabled forest, uses the other one, or keeps
    // the original biome if both are disabled
    private static Holder<Biome> forestFor(double humidity, Holder<Biome> pineForest, Holder<Biome> sprucePineForest, Holder<Biome> original) {
        Holder<Biome> forest = humidity >= WET_HUMIDITY ? sprucePineForest : pineForest;
        if (BiomePickCompat.isDisabled(forest)) forest = forest == pineForest ? sprucePineForest : pineForest;
        return BiomePickCompat.isDisabled(forest) ? original : forest;
    }

    private static Holder<Biome> findBiome(BiomeSource biomeSource, ResourceKey<Biome> key) {
        for (Holder<Biome> biome : biomeSource.possibleBiomes()) {
            if (biome.is(key)) return biome;
        }
        return null;
    }

    private static DensityFunction fade(DensityFunction target) {
        return DensityFunctions.add(DensityFunctions.mul(target, DensityFunctions.constant(-0.5)), DensityFunctions.constant(1.5))
                .clamp(0.0, 1.0);
    }

    /**
     * Target surface height (already blended between the original height and the shore target by the weight),
     * preliminary surface height for the aquifer, how strongly terrain is pulled towards them,
     * whether pools and peaks are shaped here (1 in the bog interior, 0 elsewhere), and the weight the preliminary
     * surface is pulled towards surfaceY with.
     */
    private record Shore(double floorY, double surfaceY, double weight, double bog, double aquifer) {}

    /**
     * Per-quart shore data, from the distance in quarts to the nearest bog or water quart (on land),
     * land or bog quart (in water) or land quart (in a bog).
     */
    private static final class ShoreField {
        // Search radius in quarts (4 blocks), must be larger than SHORE_WIDTH + 1
        private static final int RADIUS = 10;
        private static final double MAX_DEPTH = 14.0;
        private static final double RIVER_DEPTH = 5.0;
        // Distance from the shore over which the water deepens from 1 block to full depth
        private static final double DEPTH_RAMP = 4.0;
        // Distance from the water over which the land blends back into its original height
        private static final double SHORE_WIDTH = 8.0;
        // How far the beach rises above sea level + 1, reached BEACH_WIDTH away from the water
        private static final double BEACH_RISE = 2.0;
        private static final double BEACH_WIDTH = 3.0;
        // Erosion range over which the terrain gate goes from 0 (mountain) to 1 (flat)
        private static final double GATE_LOW = -0.3;
        private static final double GATE_HIGH = 0.0;
        // Height difference to the steepest direct neighbour in blocks per quart over which the shore blend fades
        // out, so cliffs keep their original shape instead of being pulled onto per-quart planes
        private static final double RELIEF_LOW = 4.0;
        private static final double RELIEF_HIGH = 12.0;
        // Bog ground surface relative to sea level, so the top block is flush with the water surface block.
        // The aquifer still won't flood it, as the air above it is at fluid level
        private static final double BOG_RISE = -0.5;
        // Water and bog quarts report a preliminary surface this far below sea level, so the aquifer fills the basins
        // and pools with sea level water. Has to be more than 8 below, as the surface scan is quantized to 8-block
        // cells and the aquifer adds 8 to it
        private static final double AQUIFER_DEPTH = 16.0;
        // Pools stay at least this many quarts away from the surrounding land
        private static final double POOL_MARGIN = 1.0;
        // Lake, river and bog quarts whose original surface is higher than this above sea level count as land, as the
        // biome source places them at any depth, under hills too
        private static final double MAX_WATER_HEIGHT = 16.0;
        // Where the final surface is this far below the cave-free preliminary surface, a cave opens to the surface there,
        // so the preliminary height is used instead
        private static final double CAVE_GAP = 8.0;
        // The blend weight may change by at most 1 / (WEIGHT_RADIUS + 1) per quart, so quarts that keep their original
        // shape (cliffs, high ground) slope into the blended terrain around them instead of standing up as pillars
        private static final int WEIGHT_RADIUS = 3;
        // Bog quarts whose final surface is more than this above sea level anywhere their biome reaches get a forest
        // biome instead. The one block peaks of the bog relief stay below it
        static final double MAX_BOG_HEIGHT = 2.0;
        // Land biome quarts whose final surface is more than this below sea level everywhere their biome reaches get
        // a vanilla ocean instead. Shallow puddles and flooded forest valleys keep their biome
        static final double OCEAN_DEPTH = 3.0;
        // Block offsets from a quart's corner to sample its biome reach at. BiomeManager's fuzzy zoom lets blocks
        // 4q-2 to 4q+5 read quart q, and the bilinear surface peaks at those ends or on the quart lines 4q and 4q+4
        private static final int[] BIOME_REACH = {-2, 0, 4, 5};

        // Ordered by priority, the higher kind wins where the two sample heights disagree
        private static final byte LAND = 0;
        private static final byte BOG = 1;
        private static final byte RIVER = 2;
        private static final byte LAKE = 3;

        private final BiomeSource biomeSource;
        private final Climate.Sampler sampler;
        private final NormalNoise floorNoise;
        // Final density of the unwrapped router, used to find the original surface height
        private final DensityFunction originalDensity;
        // Preliminary density of the unwrapped router, has no caves, used to spot cave openings in the final density
        private final DensityFunction preliminaryDensity;
        private final int minY;
        private final int maxY;
        private final int seaLevel;
        private final int sampleQuartY;
        private final int upperSampleQuartY;
        private final Cache<Long, Byte> waterCache = CacheBuilder.newBuilder().maximumSize(1 << 18).build();
        private final Cache<Long, Double> heightCache = CacheBuilder.newBuilder().maximumSize(1 << 18).build();
        private final Cache<Long, Shore> shoreCache = CacheBuilder.newBuilder().maximumSize(1 << 16).build();
        // Shores before the weight limit
        private final Cache<Long, Shore> rawCache = CacheBuilder.newBuilder().maximumSize(1 << 16).build();
        private final Cache<Long, BiomeHeights> biomeHeightCache = CacheBuilder.newBuilder().maximumSize(1 << 16).build();

        ShoreField(BiomeSource biomeSource, Climate.Sampler sampler, NormalNoise floorNoise, DensityFunction originalDensity,
                   DensityFunction preliminaryDensity, int minY, int maxY, int seaLevel) {
            this.biomeSource = biomeSource;
            this.sampler = sampler;
            this.floorNoise = floorNoise;
            this.originalDensity = originalDensity;
            this.preliminaryDensity = preliminaryDensity;
            this.minY = minY;
            this.maxY = maxY;
            this.seaLevel = seaLevel;
            this.sampleQuartY = QuartPos.fromBlock(seaLevel);
            this.upperSampleQuartY = QuartPos.fromBlock(seaLevel + 16);
        }

        Shore shoreAt(int qx, int qz) {
            long key = ChunkPos.asLong(qx, qz);
            Shore cached = shoreCache.getIfPresent(key);
            if (cached == null) {
                cached = compute(qx, qz);
                shoreCache.put(key, cached);
            }
            return cached;
        }

        private Shore compute(int qx, int qz) {
            Shore raw = rawShore(qx, qz);
            double weight = raw.weight();
            double bog = raw.bog();
            if (weight > 0) {
                // Min-plus dilation of the raw weights, a quart's weight is at most a neighbour's plus the allowed change
                // over the distance between them
                double limit = weight;
                double maxDistance = WEIGHT_RADIUS + 1;
                for (int dx = -WEIGHT_RADIUS; dx <= WEIGHT_RADIUS; dx++) {
                    for (int dz = -WEIGHT_RADIUS; dz <= WEIGHT_RADIUS; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d > maxDistance) continue;
                        limit = Math.min(limit, rawShore(qx + dx, qz + dz).weight() + d / maxDistance);
                    }
                }
                if (limit < weight) {
                    weight = limit;
                    // No bog pools on a slope that is blending into the original terrain
                    bog = 0.0;
                }
            }
            // The target height is blended with the original height by the weight before the densities are mixed. The
            // vanilla density is too weak near the surface to compete with a distant target, so mixing a far away
            // target in at a small weight still dropped the surface most of the way and made walls on quart lines
            boolean land = waterKind(qx, qz) == LAND;
            // An unweighted quart is still a corner of the interpolated target in cells with weighted corners, so it
            // must sit on the original surface there. Unweighted land far from those keeps its raw values
            if (weight <= 0 && land && !hasWeightedNeighbour(qx, qz)) return raw;
            double floorY = Mth.lerp(weight, originalHeight(qx, qz), raw.floorY());
            if (land) {
                return new Shore(floorY, floorY, weight, bog, weight);
            }
            // Where the blended surface ends up below sea level the aquifer sees the deep basin, so all air below sea
            // level there gets sea water. Above it the aquifer sees the original surface, so a dry slope-limited edge
            // doesn't flood the caves under it
            double aquifer = floorY < seaLevel ? 1.0 : 0.0;
            return new Shore(floorY, raw.surfaceY(), weight, bog, aquifer);
        }

        /**
         * Final surface height before the per-block bog relief. Unweighted land keeps its original height, its raw
         * shore only holds a placeholder.
         */
        double finalHeight(int qx, int qz) {
            Shore shore = shoreAt(qx, qz);
            return shore.weight() > 0 ? shore.floorY() : originalHeight(qx, qz);
        }

        /**
         * Lowest and highest final surface over the blocks that can read quart (qx, qz)'s biome, with the surface
         * bilinear between the quart corners like the interpolated density.
         */
        BiomeHeights biomeHeights(int qx, int qz) {
            long key = ChunkPos.asLong(qx, qz);
            BiomeHeights cached = biomeHeightCache.getIfPresent(key);
            if (cached != null) return cached;
            // Corner heights of quarts qx-1 to qx+2, qz-1 to qz+2
            double[][] corners = new double[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    corners[i][j] = finalHeight(qx - 1 + i, qz - 1 + j);
                }
            }
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (int ox : BIOME_REACH) {
                int cx = Math.floorDiv(ox, 4);
                double fx = (ox - cx * 4) / 4.0;
                for (int oz : BIOME_REACH) {
                    int cz = Math.floorDiv(oz, 4);
                    double fz = (oz - cz * 4) / 4.0;
                    double height = Mth.lerp2(fx, fz, corners[cx + 1][cz + 1], corners[cx + 2][cz + 1],
                            corners[cx + 1][cz + 2], corners[cx + 2][cz + 2]);
                    min = Math.min(min, height);
                    max = Math.max(max, height);
                }
            }
            BiomeHeights heights = new BiomeHeights(min, max);
            biomeHeightCache.put(key, heights);
            return heights;
        }

        record BiomeHeights(double min, double max) {}

        private boolean hasWeightedNeighbour(int qx, int qz) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if ((dx != 0 || dz != 0) && rawShore(qx + dx, qz + dz).weight() > 0) return true;
                }
            }
            return false;
        }

        private Shore rawShore(int qx, int qz) {
            long key = ChunkPos.asLong(qx, qz);
            Shore cached = rawCache.getIfPresent(key);
            if (cached != null) return cached;
            Shore raw = computeRaw(qx, qz);
            rawCache.put(key, raw);
            return raw;
        }

        private Shore computeRaw(int qx, int qz) {
            byte kind = waterKind(qx, qz);
            boolean water = kind >= RIVER;
            // Squared distance to the nearest quart on the other side of the shore: shaped (bog or water) for
            // land, land or bog for water, land for bog
            int best = Integer.MAX_VALUE;
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 >= best || d2 > RADIUS * RADIUS) continue;
                    byte other = waterKind(qx + dx, qz + dz);
                    boolean across = switch (kind) {
                        case LAND -> other != LAND;
                        case BOG -> other == LAND;
                        default -> other < RIVER;
                    };
                    if (across) best = d2;
                }
            }

            if (kind == BOG) {
                double dIn = best == Integer.MAX_VALUE ? RADIUS : Math.sqrt(best);
                // Flat ground flush with the water, BogRelief shapes pools and peaks into the interior per block
                double gate = terrainGate(qx, qz);
                // The aquifer weight is set in compute
                return new Shore(seaLevel + BOG_RISE, seaLevel - AQUIFER_DEPTH, gate, dIn >= POOL_MARGIN ? 1.0 : 0.0, gate);
            }

            if (water) {
                double dIn = best == Integer.MAX_VALUE ? RADIUS : Math.sqrt(best);
                double maxDepth = RIVER_DEPTH;
                if (kind == LAKE) {
                    double noise = Mth.clamp(floorNoise.getValue(QuartPos.toBlock(qx), 0.0, QuartPos.toBlock(qz)), -1.0, 1.0);
                    maxDepth = MAX_DEPTH * (0.75 + 0.25 * noise);
                }
                // Edge quarts stay about 1 block deep
                double depth = maxDepth * Mth.smoothstep(Mth.clamp((dIn - 1.0) / DEPTH_RAMP, 0.0, 1.0));
                double floorY = seaLevel - 1 - depth;
                // Dug fully regardless of the terrain gate, high ground is already land and the weight limit slopes
                // the edges next to kept terrain. The aquifer weight is set in compute
                return new Shore(floorY, seaLevel - AQUIFER_DEPTH, 1.0, 0.0, 1.0);
            }

            if (best == Integer.MAX_VALUE) return new Shore(seaLevel + 1, seaLevel + 1, 0.0, 0.0, 0.0);
            double dOut = Math.sqrt(best);
            double s = 1.0 - Mth.smoothstep(Mth.clamp((dOut - 1.0) / SHORE_WIDTH, 0.0, 1.0));
            double gate = s > 0 ? terrainGate(qx, qz) : 0.0;
            if (gate <= 0) return new Shore(seaLevel + 1, seaLevel + 1, 0.0, 0.0, 0.0);
            double height = originalHeight(qx, qz);
            gate *= 1.0 - Mth.smoothstep(Mth.clamp((relief(qx, qz, height) - RELIEF_LOW) / (RELIEF_HIGH - RELIEF_LOW), 0.0, 1.0));
            if (gate <= 0) return new Shore(seaLevel + 1, seaLevel + 1, 0.0, 0.0, 0.0);
            double rimY = seaLevel + 1 + BEACH_RISE * Mth.smoothstep(Mth.clamp((dOut - 1.0) / BEACH_WIDTH, 0.0, 1.0));
            // Pulls low land up to the rim and slopes high land down to it, compute blends the rim with the original
            // height by the weight
            double weight = s * gate;
            return new Shore(rimY, rimY, weight, 0.0, weight);
        }

        /** Largest original height difference to the 4 direct neighbours, in blocks. */
        private double relief(int qx, int qz, double height) {
            double relief = Math.abs(originalHeight(qx + 1, qz) - height);
            relief = Math.max(relief, Math.abs(originalHeight(qx - 1, qz) - height));
            relief = Math.max(relief, Math.abs(originalHeight(qx, qz + 1) - height));
            return Math.max(relief, Math.abs(originalHeight(qx, qz - 1) - height));
        }

        /**
         * Original surface height. Where a cave opens to the surface, the final density's topmost crossing is the cave
         * floor, so the cave-free preliminary surface is used there instead.
         */
        private double originalHeight(int qx, int qz) {
            long key = ChunkPos.asLong(qx, qz);
            Double cached = heightCache.getIfPresent(key);
            if (cached == null) {
                int x = QuartPos.toBlock(qx);
                int z = QuartPos.toBlock(qz);
                double finalHeight = scanHeight(originalDensity, 0.0, x, z);
                double preliminaryHeight = scanHeight(preliminaryDensity, SURFACE_THRESHOLD, x, z);
                cached = finalHeight < preliminaryHeight - CAVE_GAP ? preliminaryHeight : finalHeight;
                heightCache.put(key, cached);
            }
            return cached;
        }

        /**
         * Surface height with sub-block precision: the topmost crossing of the density from air (at most threshold)
         * to solid (above threshold). Scans in 8-block steps first, then per block between the last two samples.
         */
        private double scanHeight(DensityFunction density, double threshold, int x, int z) {
            for (int y = maxY; y >= minY; y -= 8) {
                if (densityAt(density, x, y, z) > threshold) {
                    // y is solid and y + 8 was air (or above the world), find the crossing in between
                    int top = Math.min(y + 8, maxY);
                    double above = densityAt(density, x, top, z) - threshold;
                    double height = top;
                    for (int yi = top - 1; yi >= y && above <= 0.0; yi--) {
                        double value = densityAt(density, x, yi, z) - threshold;
                        // Linear crossing between the solid block and the air block above it
                        height = value > 0.0 ? yi + value / (value - above) : (double) yi;
                        above = value;
                    }
                    return height;
                }
            }
            return minY;
        }

        private static double densityAt(DensityFunction density, int x, int y, int z) {
            return density.compute(new DensityFunction.SinglePointContext(x, y, z));
        }

        /** 0 for high/rugged (low erosion) terrain, 1 for flat terrain, so lakes only reshape low ground. */
        private double terrainGate(int qx, int qz) {
            double erosion = Climate.unquantizeCoord(sampler.sample(qx, sampleQuartY, qz).erosion());
            return Mth.smoothstep(Mth.clamp((erosion - GATE_LOW) / (GATE_HIGH - GATE_LOW), 0.0, 1.0));
        }

        /**
         * LAKE, RIVER, BOG or LAND, sampled at sea level and a bit above it. Lake wins over river, river over bog.
         * High ground is always land.
         */
        private byte waterKind(int qx, int qz) {
            long key = ChunkPos.asLong(qx, qz);
            Byte cached = waterCache.getIfPresent(key);
            if (cached == null) {
                cached = (byte) Math.max(kindAt(qx, sampleQuartY, qz), kindAt(qx, upperSampleQuartY, qz));
                if (cached != LAND && originalHeight(qx, qz) > seaLevel + MAX_WATER_HEIGHT) cached = LAND;
                waterCache.put(key, cached);
            }
            return cached;
        }

        private byte kindAt(int qx, int qy, int qz) {
            Holder<Biome> biome = biomeSource.getNoiseBiome(qx, qy, qz, sampler);
            return biome.is(Biomes.LAKE) ? LAKE : biome.is(Biomes.RIVER) ? RIVER : biome.is(Biomes.BOG) ? BOG : LAND;
        }

        // Land targets blend towards the original height, so they can be anywhere in the world
        double minFloor() {
            return minY;
        }

        double maxFloor() {
            return maxY;
        }
    }

    /**
     * 2D view of the shore field: the target surface height divided by 8, the preliminary surface height divided
     * by 8, the blend weight (0..1), the bog pool mask (0..1), or the preliminary surface weight (0..1).
     */
    private static final class ShoreView implements DensityFunction.SimpleFunction {
        enum Mode { FLOOR, SURFACE, WEIGHT, BOG, AQUIFER }

        private final ShoreField field;
        private final Mode mode;

        ShoreView(ShoreField field, Mode mode) {
            this.field = field;
            this.mode = mode;
        }

        @Override
        public double compute(FunctionContext context) {
            int x = context.blockX();
            int z = context.blockZ();
            int qx = QuartPos.fromBlock(x);
            int qz = QuartPos.fromBlock(z);
            int fx = x & 3;
            int fz = z & 3;
            if (fx == 0 && fz == 0) {
                return valueAt(qx, qz);
            }
            // Blend between quart corners for callers that sample single blocks
            return Mth.lerp2(fx / 4.0, fz / 4.0,
                    valueAt(qx, qz), valueAt(qx + 1, qz),
                    valueAt(qx, qz + 1), valueAt(qx + 1, qz + 1));
        }

        private double valueAt(int qx, int qz) {
            Shore shore = field.shoreAt(qx, qz);
            return switch (mode) {
                case FLOOR -> shore.floorY() / 8.0;
                case SURFACE -> shore.surfaceY() / 8.0;
                case WEIGHT -> shore.weight();
                case BOG -> shore.bog();
                case AQUIFER -> shore.aquifer();
            };
        }

        @Override
        public double minValue() {
            return mode == Mode.WEIGHT || mode == Mode.BOG || mode == Mode.AQUIFER ? 0.0 : field.minFloor() / 8.0;
        }

        @Override
        public double maxValue() {
            return mode == Mode.WEIGHT || mode == Mode.BOG || mode == Mode.AQUIFER ? 1.0 : field.maxFloor() / 8.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return KeyDispatchDataCodec.of(LAKE_FLOOR_CODEC);
        }
    }

    /**
     * Per-block bog relief relative to the flat ground, in density units (blocks / 8).
     * The swamp noise digs pools 1 to 1 + POOL_DEPTH blocks deep where it is high, leaves flat ground in between,
     * and raises one block peaks where it is lowest, in the middle of the dry patches away from the pools.
     */
    private static final class BogRelief implements DensityFunction.SimpleFunction {
        // Noise frequency multiplier (sets patch size; higher gives smaller pools and dry patches), share of the bog
        // interior that is water, noise range over which pools deepen, and full depth below the 1 block edge depth
        private static final double POOL_SCALE = 1.0;
        private static final double POOL_COVERAGE = 0.5;
        private static final double POOL_RAMP = 0.25;
        private static final double POOL_DEPTH = 2.0;
        // Share of the bog interior raised by one block
        private static final double PEAK_COVERAGE = 0.12;
        // Pools are dug at full depth from TAPER_LOW to TAPER_HIGH blocks relative to sea level, fading to 0 over
        // TAPER_WIDTH blocks on each side. The slope stays below the 1/8 per block gradient, so density stays
        // monotonic in y
        private static final int TAPER_LOW = -5;
        private static final int TAPER_HIGH = 2;
        private static final double TAPER_WIDTH = 6.0;
        // Grid the thresholds are estimated from: points per side and spacing in blocks
        private static final int POOL_SAMPLES = 64;
        private static final int POOL_SAMPLE_SPACING = 148;

        private final NormalNoise poolNoise;
        // Noise values exceeded by POOL_COVERAGE and undercut by PEAK_COVERAGE of the bog, the swamp noise isn't
        // spread evenly over -1..1
        private final double poolThreshold;
        private final double peakThreshold;
        private final int seaLevel;

        BogRelief(NormalNoise poolNoise, int seaLevel) {
            this.poolNoise = poolNoise;
            double[] samples = sampleNoise(poolNoise);
            this.poolThreshold = samples[(int) ((1.0 - POOL_COVERAGE) * (samples.length - 1))];
            this.peakThreshold = samples[(int) (PEAK_COVERAGE * (samples.length - 1))];
            this.seaLevel = seaLevel;
        }

        // Sorted swamp noise values over a wide grid
        private static double[] sampleNoise(NormalNoise poolNoise) {
            double[] samples = new double[POOL_SAMPLES * POOL_SAMPLES];
            int i = 0;
            for (int x = 0; x < POOL_SAMPLES; x++) {
                for (int z = 0; z < POOL_SAMPLES; z++) {
                    int bx = (x - POOL_SAMPLES / 2) * POOL_SAMPLE_SPACING;
                    int bz = (z - POOL_SAMPLES / 2) * POOL_SAMPLE_SPACING;
                    samples[i++] = poolNoise.getValue(bx * POOL_SCALE, 0.0, bz * POOL_SCALE);
                }
            }
            Arrays.sort(samples);
            return samples;
        }

        @Override
        public double compute(FunctionContext context) {
            int dy = context.blockY() - seaLevel;
            if (dy <= TAPER_LOW - TAPER_WIDTH || dy >= TAPER_HIGH + TAPER_WIDTH) return 0.0;
            double n = poolNoise.getValue(context.blockX() * POOL_SCALE, 0.0, context.blockZ() * POOL_SCALE);
            double value;
            if (n < peakThreshold) {
                // One block up: the top block sits at sea level, above the water surface
                value = 1.0 / 8.0;
            } else if (n > poolThreshold) {
                double poolFloor = seaLevel - 1 - POOL_DEPTH * Mth.smoothstep(Mth.clamp((n - poolThreshold) / POOL_RAMP, 0.0, 1.0));
                value = (poolFloor - (seaLevel + ShoreField.BOG_RISE)) / 8.0;
            } else {
                return 0.0;
            }
            double taper = Mth.clamp(Math.min(dy - (TAPER_LOW - TAPER_WIDTH), (TAPER_HIGH + TAPER_WIDTH) - dy) / TAPER_WIDTH, 0.0, 1.0);
            return value * taper;
        }

        @Override
        public double minValue() {
            return (-1 - POOL_DEPTH - ShoreField.BOG_RISE) / 8.0;
        }

        @Override
        public double maxValue() {
            return 1.0 / 8.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return KeyDispatchDataCodec.of(BOG_RELIEF_CODEC);
        }
    }
}
