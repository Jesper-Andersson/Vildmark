package io.github.jesperandersson.vildmark.world.level.levelgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.List;
import java.util.function.Consumer;

import static terrablender.api.ParameterUtils.*;

/**
 * Vildmark's TerraBlender region: where in climate space (temperature, humidity, continentalness,
 * erosion, weirdness) each Vildmark biome is placed. TerraBlender picks this region for some areas
 * of the world, and inside them these points replace vanilla's.
 */
public class BiomeRegion extends Region {
    // Forests are given a small offset so the lake wins wherever the boxes overlap
    private static final float FOREST_OFFSET = 0.01F;

    // Outer forest box, split by humidity into plains, birch pine, pine and spruce pine forest
    private static final Climate.Parameter FOREST_TEMPERATURE =
            Temperature.span(Temperature.COOL, Temperature.NEUTRAL);
    private static final Climate.Parameter FOREST_CONTINENTALNESS =
            Continentalness.span(Continentalness.NEAR_INLAND, Continentalness.FAR_INLAND);
    private static final Climate.Parameter FOREST_EROSION =
            Erosion.span(Erosion.EROSION_2, Erosion.EROSION_5);
    private static final List<Climate.Parameter> FOREST_WEIRDNESS = List.of(
            Weirdness.span(
                    Weirdness.HIGH_SLICE_NORMAL_DESCENDING, Weirdness.LOW_SLICE_NORMAL_DESCENDING),
            Weirdness.span(
                    Weirdness.LOW_SLICE_VARIANT_ASCENDING, Weirdness.HIGH_SLICE_VARIANT_ASCENDING));

    // Inner lake box, strictly inside the forest box on every axis so the lake is always ringed by
    // forest. The margin to the forest bounds decides how thick the forest ring is, the spans
    // decide how large the lake is.
    private static final Climate.Parameter LAKE_TEMPERATURE = Climate.Parameter.span(-0.33F, 0.08F);
    private static final Climate.Parameter LAKE_HUMIDITY = Climate.Parameter.span(-0.03F, 0.23F);
    private static final Climate.Parameter LAKE_CONTINENTALNESS =
            Climate.Parameter.span(0.03F, 0.6F);
    private static final Climate.Parameter LAKE_EROSION = Climate.Parameter.span(0.0F, 0.4F);
    private static final List<Climate.Parameter> LAKE_WEIRDNESS = List.of(
            Climate.Parameter.span(-0.48F, -0.14F),
            Climate.Parameter.span(0.14F, 0.48F));

    private static final Climate.Parameter TUNDRA_HUMIDITY =
            Humidity.span(Humidity.ARID, Humidity.NEUTRAL);

    private static final Climate.Parameter RIVER_HUMIDITY =
            Humidity.span(Humidity.NEUTRAL, Humidity.WET);
    // Rivers stay out of high (low-erosion) mountain terrain
    private static final Climate.Parameter RIVER_EROSION =
            Erosion.span(Erosion.EROSION_3, Erosion.EROSION_5);

    // Bogs take the low, flat, near-sea-level band vanilla swamps use, so LakeBasin sees the bog at
    // sea level
    private static final Climate.Parameter BOG_EROSION = Erosion.EROSION_6.parameter();

    // Like vanilla surface biomes: at the surface depth and the floor depth just below it
    private static final List<Climate.Parameter> DEPTHS =
            List.of(Depth.SURFACE.parameter(), Depth.FLOOR.parameter());

    public BiomeRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(
            Registry<Biome> registry,
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Starts from vanilla's points, so biomes Vildmark doesn't replace stay where they are
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        for (Climate.Parameter depth : DEPTHS) {
            for (Climate.Parameter weirdness : FOREST_WEIRDNESS) {
                addForestBand(builder, FOREST_TEMPERATURE, Humidity.NEUTRAL.parameter(), depth,
                        weirdness, Biomes.PINE_FOREST);
                addForestBand(builder, FOREST_TEMPERATURE, Humidity.WET.parameter(), depth,
                        weirdness, Biomes.SPRUCE_PINE_FOREST);
                addForestBand(builder, FOREST_TEMPERATURE, Humidity.DRY.parameter(), depth,
                        weirdness, Biomes.BIRCH_PINE_FOREST);
                addForestBand(builder, FOREST_TEMPERATURE, Humidity.ARID.parameter(), depth,
                        weirdness, Biomes.PLAINS);
                // Tundra takes the cold, drier part beyond the forests. Vanilla (frozen) rivers may
                // still cross it, as the Vildmark river box only covers the forest temperature
                addForestBand(builder, Temperature.ICY.parameter(), TUNDRA_HUMIDITY, depth,
                        weirdness, Biomes.TUNDRA);
            }
            // Bog replaces vanilla swamps in the forest's temperature range, using the swamp's
            // footprint
            builder.add(Climate.parameters(FOREST_TEMPERATURE, Humidity.FULL_RANGE.parameter(),
                    FOREST_CONTINENTALNESS, BOG_EROSION, depth, Weirdness.FULL_RANGE.parameter(),
                    /* offset= */ 0.0F), Biomes.BOG);
            // River occupies the valley band the forest boxes skip, so vanilla rivers don't cut
            // through forests
            builder.add(Climate.parameters(FOREST_TEMPERATURE, RIVER_HUMIDITY,
                    FOREST_CONTINENTALNESS, RIVER_EROSION, depth, Weirdness.VALLEY.parameter(),
                    /* offset= */ 0.0F), Biomes.RIVER);
            for (Climate.Parameter weirdness : LAKE_WEIRDNESS) {
                builder.add(Climate.parameters(LAKE_TEMPERATURE, LAKE_HUMIDITY,
                        LAKE_CONTINENTALNESS, LAKE_EROSION, depth, weirdness, /* offset= */ 0.0F),
                        Biomes.LAKE);
            }
        }

        builder.build().forEach(mapper::accept);
    }

    // A slice of the forest box: forest continentalness and erosion, the given temperature and
    // humidity, and the forest offset
    private static void addForestBand(
            VanillaParameterOverlayBuilder builder, Climate.Parameter temperature,
            Climate.Parameter humidity, Climate.Parameter depth, Climate.Parameter weirdness,
            ResourceKey<Biome> biome) {
        builder.add(Climate.parameters(temperature, humidity, FOREST_CONTINENTALNESS,
                FOREST_EROSION, depth, weirdness, FOREST_OFFSET), biome);
    }

}
