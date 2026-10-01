package com.github.Jesper_Andersson.nordic.world.level.levelgen.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.List;
import java.util.function.Consumer;

import static terrablender.api.ParameterUtils.*;

public class BiomeRegion extends Region {
    // Forests are given a small offset so the lake wins wherever the boxes overlap
    private static final float FOREST_OFFSET = 0.01F;

    // Outer forest box, split by humidity between the two forests
    private static final Climate.Parameter FOREST_TEMPERATURE = Temperature.span(Temperature.COOL, Temperature.NEUTRAL);
    private static final Climate.Parameter FOREST_CONTINENTALNESS = Continentalness.span(Continentalness.NEAR_INLAND, Continentalness.FAR_INLAND);
    private static final Climate.Parameter FOREST_EROSION = Erosion.span(Erosion.EROSION_2, Erosion.EROSION_5);
    private static final List<Climate.Parameter> FOREST_WEIRDNESS = List.of(
            Weirdness.span(Weirdness.HIGH_SLICE_NORMAL_DESCENDING, Weirdness.LOW_SLICE_NORMAL_DESCENDING),
            Weirdness.span(Weirdness.LOW_SLICE_VARIANT_ASCENDING, Weirdness.HIGH_SLICE_VARIANT_ASCENDING));

    // Inner lake box, strictly inside the forest box on every axis so the lake is always ringed by forest.
    // The margin to the forest bounds decides how thick the forest ring is, the spans decide how large the lake is.
    private static final Climate.Parameter LAKE_TEMPERATURE = Climate.Parameter.span(-0.33F, 0.08F);
    private static final Climate.Parameter LAKE_HUMIDITY = Climate.Parameter.span(-0.03F, 0.23F);
    private static final Climate.Parameter LAKE_CONTINENTALNESS = Climate.Parameter.span(0.03F, 0.6F);
    private static final Climate.Parameter LAKE_EROSION = Climate.Parameter.span(0.0F, 0.4F);
    private static final List<Climate.Parameter> LAKE_WEIRDNESS = List.of(
            Climate.Parameter.span(-0.48F, -0.14F),
            Climate.Parameter.span(0.14F, 0.48F));

    private static final Climate.Parameter RIVER_HUMIDITY = Humidity.span(Humidity.NEUTRAL, Humidity.WET);
    // Rivers stay out of high (low-erosion) mountain terrain
    private static final Climate.Parameter RIVER_EROSION = Erosion.span(Erosion.EROSION_3, Erosion.EROSION_5);

    // Bogs take the low, flat, near-sea-level band vanilla swamps use, so LakeBasin sees the bog at sea level
    private static final Climate.Parameter BOG_EROSION = Erosion.EROSION_6.parameter();

    private static final List<Climate.Parameter> DEPTHS = List.of(Depth.SURFACE.parameter(), Depth.FLOOR.parameter());

    public BiomeRegion(ResourceLocation name, int weight){
        super(name, RegionType.OVERWORLD, weight);
    }

    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper)
    {
        VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

        for (Climate.Parameter depth : DEPTHS) {
            for (Climate.Parameter weirdness : FOREST_WEIRDNESS) {
                builder.add(Climate.parameters(FOREST_TEMPERATURE, Humidity.NEUTRAL.parameter(), FOREST_CONTINENTALNESS,
                        FOREST_EROSION, depth, weirdness, FOREST_OFFSET), Biomes.PINE_FOREST);
                builder.add(Climate.parameters(FOREST_TEMPERATURE, Humidity.WET.parameter(), FOREST_CONTINENTALNESS,
                        FOREST_EROSION, depth, weirdness, FOREST_OFFSET), Biomes.SPRUCE_PINE_FOREST);
            }
            // Bog replaces vanilla swamps in the forest's temperature range, using the swamp's footprint
            builder.add(Climate.parameters(FOREST_TEMPERATURE, Humidity.FULL_RANGE.parameter(), FOREST_CONTINENTALNESS,
                    BOG_EROSION, depth, Weirdness.FULL_RANGE.parameter(), 0.0F), Biomes.BOG);
            // River occupies the valley band the forest boxes skip, so vanilla rivers don't cut through forests
            builder.add(Climate.parameters(FOREST_TEMPERATURE, RIVER_HUMIDITY, FOREST_CONTINENTALNESS,
                    RIVER_EROSION, depth, Weirdness.VALLEY.parameter(), 0.0F), Biomes.RIVER);
            for (Climate.Parameter weirdness : LAKE_WEIRDNESS) {
                builder.add(Climate.parameters(LAKE_TEMPERATURE, LAKE_HUMIDITY, LAKE_CONTINENTALNESS,
                        LAKE_EROSION, depth, weirdness, 0.0F), Biomes.LAKE);
            }
        }

        // Add our points to the mapper
        builder.build().forEach(mapper::accept);
    }

}
