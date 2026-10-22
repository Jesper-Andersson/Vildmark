package com.github.Jesper_Andersson.vildmark.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Optional;

/**
 * @param log               block for the trunk and stump; rotated to the trunk axis if it has an axis property
 * @param length            trunk length; kept at 10 or less so the trunk stays inside the 3x3 chunk write area
 * @param stumpChance       chance of an upright stump left behind at the origin
 * @param decoration        block placed on top of some trunk logs
 * @param decorationChance  chance per trunk log of getting a decoration
 * @param transitionLog     optional bark transition log; with {@code peeledLog} the trunk blends like a standing pine
 * @param peeledLog         optional peeled log for the trunk past the transition, toward the crown end
 * @param barkFraction      share of the trunk from the stump end that keeps its bark when blending
 * @param conkChance        chance per open side of the trunk and stump of growing a pine conk
 * @param largeConkChance   chance of a placed conk being fully grown
 */
public record FallenTreeConfiguration(BlockStateProvider log, IntProvider length, float stumpChance,
                                      BlockStateProvider decoration, float decorationChance,
                                      Optional<BlockStateProvider> transitionLog, Optional<BlockStateProvider> peeledLog,
                                      float barkFraction, float conkChance, float largeConkChance) implements FeatureConfiguration {
    public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("log").forGetter(FallenTreeConfiguration::log),
            IntProvider.codec(3, 10).fieldOf("length").forGetter(FallenTreeConfiguration::length),
            Codec.floatRange(0.0f, 1.0f).fieldOf("stump_chance").forGetter(FallenTreeConfiguration::stumpChance),
            BlockStateProvider.CODEC.fieldOf("decoration").forGetter(FallenTreeConfiguration::decoration),
            Codec.floatRange(0.0f, 1.0f).fieldOf("decoration_chance").forGetter(FallenTreeConfiguration::decorationChance),
            BlockStateProvider.CODEC.optionalFieldOf("transition_log").forGetter(FallenTreeConfiguration::transitionLog),
            BlockStateProvider.CODEC.optionalFieldOf("peeled_log").forGetter(FallenTreeConfiguration::peeledLog),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("bark_fraction", 1.0f).forGetter(FallenTreeConfiguration::barkFraction),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("conk_chance", 0.0f).forGetter(FallenTreeConfiguration::conkChance),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("large_conk_chance", 0.5f).forGetter(FallenTreeConfiguration::largeConkChance)
    ).apply(instance, FallenTreeConfiguration::new));
}
