package io.github.jesperandersson.vildmark.world.level.levelgen.lake;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.compat.BiomePickCompat;
import io.github.jesperandersson.vildmark.world.level.levelgen.biome.Biomes;
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
 * Reshapes the overworld terrain wherever the biome source places {@link Biomes#LAKE} or
 * {@link Biomes#RIVER}, so the aquifer fills it with water, and flattens {@link Biomes#BOG} into
 * mud ground flush with the water mixed with small pools, about half of it water, and a few one
 * block peaks. Biomes don't shape terrain on their own, hence the router wrapping. Once the terrain
 * is final the chunk biomes are fixed up: bog quarts whose final surface rises more than 2 blocks
 * above the water anywhere the quart's biome reaches (hills, cliffs, slopes) and lake or river
 * quarts left without any water get a forest biome, and Vildmark land biomes that ended up in open
 * water get a cold vanilla ocean.
 * <p>
 * Every quart column gets a target surface height. Inside the water it sits below sea level and
 * gets deeper further from the shore. In a bog it is flat ground level with the water surface.
 * Outside both it is a beach rim just above sea level that blends back into the original surface
 * height. The terrain density is then pulled towards a surface between the original height and that
 * height, by the blend weight. In the bog interior {@link BogRelief} then digs pools a few blocks
 * wide into it per block and raises the middle of the dry patches by one block. Water and bog
 * report a preliminary surface well below sea level, so the aquifer fills the basins and pools with
 * sea level water.
 * <p>
 * A quart is a 4x4 block column, the resolution biomes and the terrain noise are sampled at.
 * Quart coordinates are block coordinates divided by 4.
 */
public final class LakeBasin {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>>
            DENSITY_FUNCTION_TYPES =
                    DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, Vildmark.MODID);

    // Only ever created in code, but density function types need a registered codec
    private static final MapCodec<ShoreView> LAKE_FLOOR_CODEC = MapCodec.unit(() -> {
        throw new UnsupportedOperationException("vildmark:lake_floor can't be created from data");
    });
    private static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<ShoreView>>
            LAKE_FLOOR = DENSITY_FUNCTION_TYPES.register("lake_floor", () -> LAKE_FLOOR_CODEC);
    private static final MapCodec<BogRelief> BOG_RELIEF_CODEC = MapCodec.unit(() -> {
        throw new UnsupportedOperationException("vildmark:bog_relief can't be created from data");
    });
    private static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<BogRelief>>
            BOG_RELIEF = DENSITY_FUNCTION_TYPES.register("bog_relief", () -> BOG_RELIEF_CODEC);

    // Humidity where Humidity.WET starts, the forest fallback is spruce pine forest from here on
    private static final double WET_HUMIDITY = 0.1;

    // Shore field of each wrapped RandomState, so chunk biome filling can see the final terrain
    // Weak keys, so the field is dropped along with its world
    private static final Map<RandomState, ShoreField> FIELDS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private LakeBasin() {}

    // Vanilla's biome keys. Vildmark's own Biomes class takes the short name in this file
    private static final class VanillaBiomes {
        static final ResourceKey<Biome> COLD_OCEAN =
                net.minecraft.world.level.biome.Biomes.COLD_OCEAN;
        static final ResourceKey<Biome> FROZEN_OCEAN =
                net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN;
    }

    public static void register(IEventBus modEventBus) {
        DENSITY_FUNCTION_TYPES.register(modEventBus);
    }

    /**
     * Returns a copy of the overworld's noise router whose terrain density and preliminary surface
     * are pulled toward the lake, river and bog shapes. Every other noise is passed through.
     */
    public static NoiseRouter wrapRouter(
            RandomState randomState, BiomeSource biomeSource, NoiseSettings noiseSettings,
            int seaLevel) {
        NoiseRouter router = randomState.router();
        int minY = noiseSettings.minY();
        int maxY = minY + noiseSettings.height();
        NormalNoise floorNoise = randomState.getOrCreateNoise(Noises.SURFACE);
        ShoreField field = new ShoreField(biomeSource, randomState.sampler(), floorNoise,
                router.finalDensity(), router.preliminarySurfaceLevel(), minY, maxY, seaLevel);
        FIELDS.put(randomState, field);
        DensityFunction floorHeight = new ShoreView(field, ShoreView.Mode.FLOOR);
        DensityFunction surface = new ShoreView(field, ShoreView.Mode.SURFACE);
        DensityFunction weight = new ShoreView(field, ShoreView.Mode.WEIGHT);

        // (H - y) / 8: solid below the target surface, empty above it
        DensityFunction gradient =
                DensityFunctions.yClampedGradient(minY, maxY, -minY / 8.0, -maxY / 8.0);
        DensityFunction target =
                DensityFunctions.add(DensityFunctions.flatCache(floorHeight), gradient);

        // The flat cache is constant per quart column, so interpolate the target for the final
        // blend
        DensityFunction smoothTarget = DensityFunctions.interpolated(target);

        // Fade the blend out below the target so caves stay: 1 down to 8 blocks below, 0 from 24
        // below. The final blend fades on the interpolated target, otherwise the per-quart flat
        // cache cuts it into 4x4 columns
        DensityFunction flatWeight = DensityFunctions.flatCache(weight);
        DensityFunction finalWeight =
                DensityFunctions.mul(DensityFunctions.interpolated(flatWeight), fade(smoothTarget));

        // Per-block pools and peaks in the bog interior. The mask goes first, as MUL skips the
        // second argument where the first is 0, so the swamp noise only runs inside the bog
        DensityFunction bogView = new ShoreView(field, ShoreView.Mode.BOG);
        DensityFunction bogMask =
                DensityFunctions.interpolated(DensityFunctions.flatCache(bogView));
        BogRelief bogRelief = new BogRelief(randomState.getOrCreateNoise(Noises.SWAMP), seaLevel);
        DensityFunction relief = DensityFunctions.mul(bogMask, bogRelief);

        DensityFunction aquiferWeight = new ShoreView(field, ShoreView.Mode.AQUIFER);
        DensityFunction surfaceInBlocks =
                DensityFunctions.mul(surface, DensityFunctions.constant(8.0));
        DensityFunction shapedDensity = DensityFunctions.add(smoothTarget, relief);

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
                // Keep the preliminary surface in the basin, otherwise the aquifer won't use sea
                // level water there. In water and bog it is pushed below sea level so the aquifer
                // floods the basins and shallow pools too. This is a 2D height in blocks, while the
                // SURFACE view is in density units (blocks / 8)
                DensityFunctions.lerp(
                        aquiferWeight, router.preliminarySurfaceLevel(), surfaceInBlocks),
                DensityFunctions.lerp(finalWeight, router.finalDensity(), shapedDensity),
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap()
        );
    }

    /**
     * Fixes the chunk biomes once the final terrain is known, per quart and in this order:
     * <ul>
     *   <li>A Vildmark land biome whose final surface is more than 3 blocks below sea level
     *       everywhere the quart's biome reaches sits in open water, so it becomes frozen ocean
     *       (tundra) or cold ocean (the rest).
     *   <li>A lake or river whose final surface is at or above sea level everywhere the quart's
     *       biome reaches has no water (high ground the basin isn't dug into, or edges the weight
     *       limit leaves standing), so it becomes forest.
     *   <li>A bog whose final surface is more than 2 blocks above the water anywhere the quart's
     *       biome reaches (hills, cliffs and slopes the basin leaves standing) becomes forest,
     *       so it doesn't get bog ground and vegetation.
     * </ul>
     * The forest is pine forest, or spruce pine forest where wet. Lake and river humidity only
     * covers those two bands. The biome source itself stays unchanged, as the shore field reads it.
     */
    public static BiomeResolver wrapBiomes(RandomState randomState, BiomeResolver resolver) {
        ShoreField field = FIELDS.get(randomState);
        if (field == null) {
            return resolver;
        }
        Holder<Biome> pineForest = findBiome(field.biomeSource, Biomes.PINE_FOREST);
        Holder<Biome> sprucePineForest = findBiome(field.biomeSource, Biomes.SPRUCE_PINE_FOREST);
        Holder<Biome> coldOcean = findBiome(field.biomeSource, VanillaBiomes.COLD_OCEAN);
        Holder<Biome> frozenOcean = findBiome(field.biomeSource, VanillaBiomes.FROZEN_OCEAN);
        boolean hasForests = pineForest != null && sprucePineForest != null;
        if (!hasForests && coldOcean == null && frozenOcean == null) {
            return resolver;
        }
        double maxOceanHeight = field.seaLevel - ShoreField.OCEAN_DEPTH;
        double maxBogHeight = field.seaLevel + ShoreField.MAX_BOG_HEIGHT;
        // The resolver is called with quart coordinates
        return (quartX, quartY, quartZ, sampler) -> {
            Holder<Biome> biome = resolver.getNoiseBiome(quartX, quartY, quartZ, sampler);
            boolean isWater = biome.is(Biomes.LAKE) || biome.is(Biomes.RIVER);
            boolean isBog = biome.is(Biomes.BOG);
            boolean isTundra = biome.is(Biomes.TUNDRA);
            boolean isForestOrPlains = biome.is(Biomes.PINE_FOREST)
                    || biome.is(Biomes.SPRUCE_PINE_FOREST)
                    || biome.is(Biomes.BIRCH_PINE_FOREST)
                    || biome.is(Biomes.PLAINS);
            boolean isLand = isBog || isTundra || isForestOrPlains;
            if (!isWater && !isLand) {
                return biome;
            }

            ShoreField.BiomeHeights heights = field.biomeHeights(quartX, quartZ);
            boolean isInOpenWater = isLand && heights.max() < maxOceanHeight;
            if (isInOpenWater) {
                Holder<Biome> ocean = isTundra ? frozenOcean : coldOcean;
                if (ocean != null) {
                    return ocean;
                }
            }
            if (!hasForests) {
                return biome;
            }

            boolean isDryWater = isWater && heights.min() >= field.seaLevel;
            boolean isRaisedBog = isBog && heights.max() > maxBogHeight;
            if (!isDryWater && !isRaisedBog) {
                return biome;
            }
            long quantizedHumidity = sampler.sample(quartX, quartY, quartZ).humidity();
            double humidity = Climate.unquantizeCoord(quantizedHumidity);
            return forestFor(humidity, pineForest, sprucePineForest, biome);
        };
    }

    // Pine forest, spruce pine forest where wet. Doesn't bring back a disabled forest, uses the
    // other one, or keeps the original biome if both are disabled
    private static Holder<Biome> forestFor(
            double humidity, Holder<Biome> pineForest, Holder<Biome> sprucePineForest,
            Holder<Biome> original) {
        Holder<Biome> forest = humidity >= WET_HUMIDITY ? sprucePineForest : pineForest;
        if (BiomePickCompat.isDisabled(forest)) {
            forest = forest == pineForest ? sprucePineForest : pineForest;
        }
        return BiomePickCompat.isDisabled(forest) ? original : forest;
    }

    private static Holder<Biome> findBiome(BiomeSource biomeSource, ResourceKey<Biome> key) {
        for (Holder<Biome> biome : biomeSource.possibleBiomes()) {
            if (biome.is(key)) {
                return biome;
            }
        }
        return null;
    }

    // 1.5 - target / 2, clamped to 0..1. The target is in blocks / 8 below the surface, so this is
    // 1 down to 8 blocks below the target surface and 0 from 24 below
    private static DensityFunction fade(DensityFunction target) {
        DensityFunction halfTarget = DensityFunctions.mul(target, DensityFunctions.constant(-0.5));
        return DensityFunctions.add(halfTarget, DensityFunctions.constant(1.5)).clamp(0.0, 1.0);
    }

    /**
     * Target surface height (already blended between the original height and the shore target by
     * the weight), preliminary surface height for the aquifer, how strongly terrain is pulled
     * towards them, whether pools and peaks are shaped here (1 in the bog interior, 0 elsewhere),
     * and the weight the preliminary surface is pulled towards surfaceY with.
     */
    private record Shore(
            double floorY, double surfaceY, double weight, double bog, double aquifer) {}

    /**
     * Per-quart shore data, from the distance in quarts to the nearest bog or water quart (on
     * land), land or bog quart (in water) or land quart (in a bog).
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
        // Height difference to the steepest direct neighbour in blocks per quart over which the
        // shore blend fades out, so cliffs keep their original shape instead of being pulled onto
        // per-quart planes
        private static final double RELIEF_LOW = 4.0;
        private static final double RELIEF_HIGH = 12.0;
        // Bog ground surface relative to sea level, so the top block is flush with the water
        // surface block. The aquifer still won't flood it, as the air above it is at fluid level
        private static final double BOG_RISE = -0.5;
        // Water and bog quarts report a preliminary surface this far below sea level, so the
        // aquifer fills the basins and pools with sea level water. Has to be more than 8 below, as
        // the aquifer adds 8 to it
        private static final double AQUIFER_DEPTH = 16.0;
        // Pools stay at least this many quarts away from the surrounding land
        private static final double POOL_MARGIN = 1.0;
        // Lake, river and bog quarts whose original surface is higher than this above sea level
        // count as land, as the biome source places them at any depth, under hills too
        private static final double MAX_WATER_HEIGHT = 16.0;
        // Where the final surface is this far below the cave-free preliminary surface, a cave opens
        // to the surface there, so the preliminary height is used instead
        private static final double CAVE_GAP = 8.0;
        // The blend weight may change by at most 1 / (WEIGHT_RADIUS + 1) per quart, so quarts that
        // keep their original shape (cliffs, high ground) slope into the blended terrain around
        // them instead of standing up as pillars
        private static final int WEIGHT_RADIUS = 3;
        // Bog quarts whose final surface is more than this above sea level anywhere their biome
        // reaches get a forest biome instead. The one block peaks of the bog relief stay below it
        static final double MAX_BOG_HEIGHT = 2.0;
        // Land biome quarts whose final surface is more than this below sea level everywhere their
        // biome reaches get a vanilla ocean instead. Shallow puddles and flooded forest valleys
        // keep their biome
        static final double OCEAN_DEPTH = 3.0;
        // Block offsets from a quart's corner to sample its biome reach at. BiomeManager's fuzzy
        // zoom lets blocks 4q-2 to 4q+5 read quart q, and the bilinear surface peaks at those ends
        // or on the quart lines 4q and 4q+4
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
        // Preliminary surface height of the unwrapped router, has no caves, used to spot cave
        // openings in the final density
        private final DensityFunction preliminarySurface;
        private final int minY;
        private final int maxY;
        private final int seaLevel;
        private final int sampleQuartY;
        private final int upperSampleQuartY;
        // Per-quart caches, keyed by the packed quart coordinates. Each value is expensive to
        // compute and read many times by neighbouring quarts
        private final Cache<Long, Byte> waterCache = newCache(1 << 18);
        private final Cache<Long, Double> heightCache = newCache(1 << 18);
        private final Cache<Long, Shore> shoreCache = newCache(1 << 16);
        // Shores before the weight limit
        private final Cache<Long, Shore> rawCache = newCache(1 << 16);
        private final Cache<Long, BiomeHeights> biomeHeightCache = newCache(1 << 16);

        ShoreField(
                BiomeSource biomeSource, Climate.Sampler sampler, NormalNoise floorNoise,
                DensityFunction originalDensity, DensityFunction preliminarySurface,
                int minY, int maxY, int seaLevel) {
            this.biomeSource = biomeSource;
            this.sampler = sampler;
            this.floorNoise = floorNoise;
            this.originalDensity = originalDensity;
            this.preliminarySurface = preliminarySurface;
            this.minY = minY;
            this.maxY = maxY;
            this.seaLevel = seaLevel;
            this.sampleQuartY = QuartPos.fromBlock(seaLevel);
            this.upperSampleQuartY = QuartPos.fromBlock(seaLevel + 16);
        }

        private static <V> Cache<Long, V> newCache(int maxEntries) {
            return CacheBuilder.newBuilder().maximumSize(maxEntries).build();
        }

        Shore shoreAt(int quartX, int quartZ) {
            long key = ChunkPos.pack(quartX, quartZ);
            Shore cached = shoreCache.getIfPresent(key);
            if (cached == null) {
                cached = compute(quartX, quartZ);
                shoreCache.put(key, cached);
            }
            return cached;
        }

        private Shore compute(int quartX, int quartZ) {
            Shore raw = rawShore(quartX, quartZ);
            double weight = raw.weight();
            double bog = raw.bog();
            if (weight > 0) {
                // Min-plus dilation of the raw weights, a quart's weight is at most a neighbour's
                // plus the allowed change over the distance between them
                double weightLimit = weight;
                double maxDistance = WEIGHT_RADIUS + 1;
                for (int dx = -WEIGHT_RADIUS; dx <= WEIGHT_RADIUS; dx++) {
                    for (int dz = -WEIGHT_RADIUS; dz <= WEIGHT_RADIUS; dz++) {
                        if (dx == 0 && dz == 0) {
                            continue;
                        }
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        if (distance > maxDistance) {
                            continue;
                        }
                        double neighbourWeight = rawShore(quartX + dx, quartZ + dz).weight();
                        double allowedChange = distance / maxDistance;
                        weightLimit = Math.min(weightLimit, neighbourWeight + allowedChange);
                    }
                }
                if (weightLimit < weight) {
                    weight = weightLimit;
                    // No bog pools on a slope that is blending into the original terrain
                    bog = 0.0;
                }
            }
            // The target height is blended with the original height by the weight before the
            // densities are mixed. The vanilla density is too weak near the surface to compete with
            // a distant target, so mixing a far away target in at a small weight still dropped the
            // surface most of the way and made walls on quart lines
            boolean isLand = waterKind(quartX, quartZ) == LAND;
            // An unweighted quart is still a corner of the interpolated target in cells with
            // weighted corners, so it must sit on the original surface there. Unweighted land far
            // from those keeps its raw values
            if (weight <= 0 && isLand && !hasWeightedNeighbour(quartX, quartZ)) {
                return raw;
            }
            double floorY = Mth.lerp(weight, originalHeight(quartX, quartZ), raw.floorY());
            if (isLand) {
                return new Shore(floorY, floorY, weight, bog, weight);
            }
            // Where the blended surface ends up below sea level the aquifer sees the deep basin, so
            // all air below sea level there gets sea water. Above it the aquifer sees the original
            // surface, so a dry slope-limited edge doesn't flood the caves under it
            double aquifer = floorY < seaLevel ? 1.0 : 0.0;
            return new Shore(floorY, raw.surfaceY(), weight, bog, aquifer);
        }

        /**
         * Final surface height before the per-block bog relief. Unweighted land keeps its original
         * height, its raw shore only holds a placeholder.
         */
        double finalHeight(int quartX, int quartZ) {
            Shore shore = shoreAt(quartX, quartZ);
            return shore.weight() > 0 ? shore.floorY() : originalHeight(quartX, quartZ);
        }

        /**
         * Lowest and highest final surface over the blocks that can read the biome of quart
         * (quartX, quartZ), with the surface bilinear between the quart corners like the
         * interpolated density.
         */
        BiomeHeights biomeHeights(int quartX, int quartZ) {
            long key = ChunkPos.pack(quartX, quartZ);
            BiomeHeights cached = biomeHeightCache.getIfPresent(key);
            if (cached != null) {
                return cached;
            }
            // Corner heights of quarts quartX-1 to quartX+2, quartZ-1 to quartZ+2
            double[][] corners = new double[4][4];
            for (int cornerXIdx = 0; cornerXIdx < 4; cornerXIdx++) {
                for (int cornerZIdx = 0; cornerZIdx < 4; cornerZIdx++) {
                    corners[cornerXIdx][cornerZIdx] =
                            finalHeight(quartX - 1 + cornerXIdx, quartZ - 1 + cornerZIdx);
                }
            }
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (int offsetX : BIOME_REACH) {
                // The quart the offset falls in, relative to this one, and how far into it (0..1)
                int cellX = Math.floorDiv(offsetX, 4);
                double fractionX = (offsetX - cellX * 4) / 4.0;
                for (int offsetZ : BIOME_REACH) {
                    int cellZ = Math.floorDiv(offsetZ, 4);
                    double fractionZ = (offsetZ - cellZ * 4) / 4.0;
                    double height = Mth.lerp2(fractionX, fractionZ,
                            corners[cellX + 1][cellZ + 1], corners[cellX + 2][cellZ + 1],
                            corners[cellX + 1][cellZ + 2], corners[cellX + 2][cellZ + 2]);
                    min = Math.min(min, height);
                    max = Math.max(max, height);
                }
            }
            BiomeHeights heights = new BiomeHeights(min, max);
            biomeHeightCache.put(key, heights);
            return heights;
        }

        record BiomeHeights(double min, double max) {}

        private boolean hasWeightedNeighbour(int quartX, int quartZ) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean isSelf = dx == 0 && dz == 0;
                    if (!isSelf && rawShore(quartX + dx, quartZ + dz).weight() > 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        private Shore rawShore(int quartX, int quartZ) {
            long key = ChunkPos.pack(quartX, quartZ);
            Shore cached = rawCache.getIfPresent(key);
            if (cached != null) {
                return cached;
            }
            Shore raw = computeRaw(quartX, quartZ);
            rawCache.put(key, raw);
            return raw;
        }

        private Shore computeRaw(int quartX, int quartZ) {
            byte kind = waterKind(quartX, quartZ);
            boolean isWater = kind >= RIVER;
            // Squared distance to the nearest quart on the other side of the shore: shaped (bog or
            // water) for land, land or bog for water, land for bog
            int nearestAcrossSquared = Integer.MAX_VALUE;
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    int distanceSquared = dx * dx + dz * dz;
                    boolean isOutsideRadius = distanceSquared > RADIUS * RADIUS;
                    if (distanceSquared >= nearestAcrossSquared || isOutsideRadius) {
                        continue;
                    }
                    byte otherKind = waterKind(quartX + dx, quartZ + dz);
                    boolean isAcrossShore = switch (kind) {
                        case LAND -> otherKind != LAND;
                        case BOG -> otherKind == LAND;
                        default -> otherKind < RIVER;
                    };
                    if (isAcrossShore) {
                        nearestAcrossSquared = distanceSquared;
                    }
                }
            }
            boolean hasShoreInRange = nearestAcrossSquared != Integer.MAX_VALUE;
            // In quarts. Inside water or bog it's how far in from the shore, on land how far out
            double distanceToShore = hasShoreInRange ? Math.sqrt(nearestAcrossSquared) : RADIUS;

            if (kind == BOG) {
                // Flat ground flush with the water, BogRelief shapes pools and peaks into the
                // interior per block
                double gate = terrainGate(quartX, quartZ);
                double poolMask = distanceToShore >= POOL_MARGIN ? 1.0 : 0.0;
                // The aquifer weight is set in compute
                return new Shore(
                        seaLevel + BOG_RISE, seaLevel - AQUIFER_DEPTH, gate, poolMask, gate);
            }

            if (isWater) {
                double maxDepth = RIVER_DEPTH;
                if (kind == LAKE) {
                    // Varies lake depth between 3/4 and full MAX_DEPTH
                    double rawNoise = floorNoise.getValue(
                            QuartPos.toBlock(quartX), 0.0, QuartPos.toBlock(quartZ));
                    double noise = Mth.clamp(rawNoise, -1.0, 1.0);
                    maxDepth = MAX_DEPTH * (0.75 + 0.25 * noise);
                }
                // Edge quarts stay about 1 block deep
                double depthRamp = Mth.clamp((distanceToShore - 1.0) / DEPTH_RAMP, 0.0, 1.0);
                double depth = maxDepth * Mth.smoothstep(depthRamp);
                double floorY = seaLevel - 1 - depth;
                // Dug fully regardless of the terrain gate, high ground is already land and the
                // weight limit slopes the edges next to kept terrain. The aquifer weight is set in
                // compute
                return new Shore(floorY, seaLevel - AQUIFER_DEPTH, 1.0, 0.0, 1.0);
            }

            // Land: a beach rim that fades back into the original terrain away from the water
            Shore untouchedLand = new Shore(seaLevel + 1, seaLevel + 1, 0.0, 0.0, 0.0);
            if (!hasShoreInRange) {
                return untouchedLand;
            }
            // 1 next to the water, falling to 0 SHORE_WIDTH quarts out
            double shoreProgress = Mth.clamp((distanceToShore - 1.0) / SHORE_WIDTH, 0.0, 1.0);
            double shoreFalloff = 1.0 - Mth.smoothstep(shoreProgress);
            double gate = shoreFalloff > 0 ? terrainGate(quartX, quartZ) : 0.0;
            if (gate <= 0) {
                return untouchedLand;
            }
            // Steep terrain keeps its shape
            double height = originalHeight(quartX, quartZ);
            double steepness = relief(quartX, quartZ, height);
            double reliefProgress =
                    Mth.clamp((steepness - RELIEF_LOW) / (RELIEF_HIGH - RELIEF_LOW), 0.0, 1.0);
            gate *= 1.0 - Mth.smoothstep(reliefProgress);
            if (gate <= 0) {
                return untouchedLand;
            }
            double beachProgress = Mth.clamp((distanceToShore - 1.0) / BEACH_WIDTH, 0.0, 1.0);
            double rimY = seaLevel + 1 + BEACH_RISE * Mth.smoothstep(beachProgress);
            // Pulls low land up to the rim and slopes high land down to it, compute blends the rim
            // with the original height by the weight
            double weight = shoreFalloff * gate;
            return new Shore(rimY, rimY, weight, 0.0, weight);
        }

        /** Largest original height difference to the 4 direct neighbours, in blocks. */
        private double relief(int quartX, int quartZ, double height) {
            double relief = Math.abs(originalHeight(quartX + 1, quartZ) - height);
            relief = Math.max(relief, Math.abs(originalHeight(quartX - 1, quartZ) - height));
            relief = Math.max(relief, Math.abs(originalHeight(quartX, quartZ + 1) - height));
            return Math.max(relief, Math.abs(originalHeight(quartX, quartZ - 1) - height));
        }

        /**
         * Original surface height. Where a cave opens to the surface, the final density's topmost
         * crossing is the cave floor, so the cave-free preliminary surface is used there instead.
         */
        private double originalHeight(int quartX, int quartZ) {
            long key = ChunkPos.pack(quartX, quartZ);
            Double cached = heightCache.getIfPresent(key);
            if (cached == null) {
                int x = QuartPos.toBlock(quartX);
                int z = QuartPos.toBlock(quartZ);
                double finalHeight = scanHeight(originalDensity, x, z);
                double preliminaryHeight =
                        preliminarySurface.compute(new DensityFunction.SinglePointContext(x, 0, z));
                boolean isCaveOpening = finalHeight < preliminaryHeight - CAVE_GAP;
                cached = isCaveOpening ? preliminaryHeight : finalHeight;
                heightCache.put(key, cached);
            }
            return cached;
        }

        /**
         * Surface height with sub-block precision: the topmost crossing of the density from air (at
         * most 0) to solid (above 0). Scans in 8-block steps first, then per block between the last
         * two samples.
         */
        private double scanHeight(DensityFunction density, int x, int z) {
            for (int y = maxY; y >= minY; y -= 8) {
                if (densityAt(density, x, y, z) > 0.0) {
                    // y is solid and y + 8 was air (or above the world), find the crossing in
                    // between
                    int top = Math.min(y + 8, maxY);
                    double densityAbove = densityAt(density, x, top, z);
                    double height = top;
                    for (int blockY = top - 1; blockY >= y && densityAbove <= 0.0; blockY--) {
                        double blockDensity = densityAt(density, x, blockY, z);
                        // Linear crossing between the solid block and the air block above it
                        height = blockDensity > 0.0
                                ? blockY + blockDensity / (blockDensity - densityAbove)
                                : (double) blockY;
                        densityAbove = blockDensity;
                    }
                    return height;
                }
            }
            return minY;
        }

        private static double densityAt(DensityFunction density, int x, int y, int z) {
            return density.compute(new DensityFunction.SinglePointContext(x, y, z));
        }

        /**
         * 0 for high/rugged (low erosion) terrain, 1 for flat terrain, so lakes only reshape low
         * ground.
         */
        private double terrainGate(int quartX, int quartZ) {
            long quantizedErosion = sampler.sample(quartX, sampleQuartY, quartZ).erosion();
            double erosion = Climate.unquantizeCoord(quantizedErosion);
            double flatness = Mth.clamp((erosion - GATE_LOW) / (GATE_HIGH - GATE_LOW), 0.0, 1.0);
            return Mth.smoothstep(flatness);
        }

        /**
         * LAKE, RIVER, BOG or LAND, sampled at sea level and a bit above it. Lake wins over river,
         * river over bog. High ground is always land.
         */
        private byte waterKind(int quartX, int quartZ) {
            long key = ChunkPos.pack(quartX, quartZ);
            Byte cached = waterCache.getIfPresent(key);
            if (cached == null) {
                byte kindAtSeaLevel = kindAt(quartX, sampleQuartY, quartZ);
                byte kindAbove = kindAt(quartX, upperSampleQuartY, quartZ);
                cached = (byte) Math.max(kindAtSeaLevel, kindAbove);
                // Checked second, since finding the original height is expensive
                if (cached != LAND
                        && originalHeight(quartX, quartZ) > seaLevel + MAX_WATER_HEIGHT) {
                    cached = LAND;
                }
                waterCache.put(key, cached);
            }
            return cached;
        }

        private byte kindAt(int quartX, int quartY, int quartZ) {
            Holder<Biome> biome = biomeSource.getNoiseBiome(quartX, quartY, quartZ, sampler);
            if (biome.is(Biomes.LAKE)) {
                return LAKE;
            }
            if (biome.is(Biomes.RIVER)) {
                return RIVER;
            }
            if (biome.is(Biomes.BOG)) {
                return BOG;
            }
            return LAND;
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
     * 2D view of the shore field: the target surface height divided by 8, the preliminary surface
     * height divided by 8, the blend weight (0..1), the bog pool mask (0..1), or the preliminary
     * surface weight (0..1).
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
            int quartX = QuartPos.fromBlock(x);
            int quartZ = QuartPos.fromBlock(z);
            // Block position inside the quart, 0-3
            int blockInQuartX = x & 3;
            int blockInQuartZ = z & 3;
            if (blockInQuartX == 0 && blockInQuartZ == 0) {
                return valueAt(quartX, quartZ);
            }
            // Blend between quart corners for callers that sample single blocks
            return Mth.lerp2(blockInQuartX / 4.0, blockInQuartZ / 4.0,
                    valueAt(quartX, quartZ), valueAt(quartX + 1, quartZ),
                    valueAt(quartX, quartZ + 1), valueAt(quartX + 1, quartZ + 1));
        }

        private double valueAt(int quartX, int quartZ) {
            Shore shore = field.shoreAt(quartX, quartZ);
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
            return isZeroToOne() ? 0.0 : field.minFloor() / 8.0;
        }

        @Override
        public double maxValue() {
            return isZeroToOne() ? 1.0 : field.maxFloor() / 8.0;
        }

        // Weights and masks, as opposed to heights
        private boolean isZeroToOne() {
            return mode == Mode.WEIGHT || mode == Mode.BOG || mode == Mode.AQUIFER;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return KeyDispatchDataCodec.of(LAKE_FLOOR_CODEC);
        }
    }

    /**
     * Per-block bog relief relative to the flat ground, in density units (blocks / 8). The swamp
     * noise digs pools 1 to 1 + POOL_DEPTH blocks deep where it is high, leaves flat ground in
     * between, and raises one block peaks where it is lowest, in the middle of the dry patches away
     * from the pools.
     */
    private static final class BogRelief implements DensityFunction.SimpleFunction {
        // Noise frequency multiplier (sets patch size; higher gives smaller pools and dry patches),
        // share of the bog interior that is water, noise range over which pools deepen, and full
        // depth below the 1 block edge depth
        private static final double POOL_SCALE = 1.0;
        private static final double POOL_COVERAGE = 0.5;
        private static final double POOL_RAMP = 0.25;
        private static final double POOL_DEPTH = 2.0;
        // Share of the bog interior raised by one block
        private static final double PEAK_COVERAGE = 0.12;
        // Pools are dug at full depth from TAPER_LOW to TAPER_HIGH blocks relative to sea level,
        // fading to 0 over TAPER_WIDTH blocks on each side. The slope stays below the 1/8 per block
        // gradient, so density stays monotonic in y
        private static final int TAPER_LOW = -5;
        private static final int TAPER_HIGH = 2;
        private static final double TAPER_WIDTH = 6.0;
        // Grid the thresholds are estimated from: points per side and spacing in blocks
        private static final int POOL_SAMPLES = 64;
        private static final int POOL_SAMPLE_SPACING = 148;

        private final NormalNoise poolNoise;
        // Noise values exceeded by POOL_COVERAGE and undercut by PEAK_COVERAGE of the bog, the
        // swamp noise isn't spread evenly over -1..1
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
            int sampleIdx = 0;
            for (int gridX = 0; gridX < POOL_SAMPLES; gridX++) {
                for (int gridZ = 0; gridZ < POOL_SAMPLES; gridZ++) {
                    // A grid centered on 0, 0
                    int blockX = (gridX - POOL_SAMPLES / 2) * POOL_SAMPLE_SPACING;
                    int blockZ = (gridZ - POOL_SAMPLES / 2) * POOL_SAMPLE_SPACING;
                    samples[sampleIdx++] =
                            poolNoise.getValue(blockX * POOL_SCALE, 0.0, blockZ * POOL_SCALE);
                }
            }
            Arrays.sort(samples);
            return samples;
        }

        @Override
        public double compute(FunctionContext context) {
            int heightAboveSea = context.blockY() - seaLevel;
            double taperBottom = TAPER_LOW - TAPER_WIDTH;
            double taperTop = TAPER_HIGH + TAPER_WIDTH;
            if (heightAboveSea <= taperBottom || heightAboveSea >= taperTop) {
                return 0.0;
            }
            double noise = poolNoise.getValue(
                    context.blockX() * POOL_SCALE, 0.0, context.blockZ() * POOL_SCALE);
            double relief;
            if (noise < peakThreshold) {
                // One block up: the top block sits at sea level, above the water surface
                relief = 1.0 / 8.0;
            } else if (noise > poolThreshold) {
                // Deeper the further the noise is above the threshold
                double poolProgress = Mth.clamp((noise - poolThreshold) / POOL_RAMP, 0.0, 1.0);
                double poolFloor = seaLevel - 1 - POOL_DEPTH * Mth.smoothstep(poolProgress);
                relief = (poolFloor - (seaLevel + ShoreField.BOG_RISE)) / 8.0;
            } else {
                return 0.0;
            }
            // Distance into the taper band from its nearest edge, as a fraction of its width
            double distanceFromTaperEdge =
                    Math.min(heightAboveSea - taperBottom, taperTop - heightAboveSea);
            double taper = Mth.clamp(distanceFromTaperEdge / TAPER_WIDTH, 0.0, 1.0);
            return relief * taper;
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
