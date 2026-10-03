package com.github.Jesper_Andersson.nordic.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Optional;

/**
 * @param state     block for each boulder position
 * @param radius    horizontal radius; kept at 8 or less so the boulder stays inside the 3x3 chunk write area
 * @param sink      share of the vertical radius pushed below the surface
 * @param top       optional cover for the boulder's top surface, sampled per column; full blocks replace the top block,
 *                  anything else (carpets, plants) is placed on top of it
 * @param topChance chance that a boulder gets the top cover at all
 */
public record BoulderConfiguration(BlockStateProvider state, IntProvider radius, FloatProvider sink,
                                   Optional<BlockStateProvider> top, float topChance) implements FeatureConfiguration {
    public static final Codec<BoulderConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(BoulderConfiguration::state),
            IntProvider.codec(1, 8).fieldOf("radius").forGetter(BoulderConfiguration::radius),
            FloatProvider.codec(0.0f, 1.0f).fieldOf("sink").forGetter(BoulderConfiguration::sink),
            BlockStateProvider.CODEC.optionalFieldOf("top").forGetter(BoulderConfiguration::top),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("top_chance", 0.0f).forGetter(BoulderConfiguration::topChance)
    ).apply(instance, BoulderConfiguration::new));
}
