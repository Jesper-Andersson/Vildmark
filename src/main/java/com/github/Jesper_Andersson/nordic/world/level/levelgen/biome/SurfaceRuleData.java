package com.github.Jesper_Andersson.nordic.world.level.levelgen.biome;

import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRuleData {
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource MOSS_BLOCK = makeStateRule(Blocks.MOSS_BLOCK);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource CLAY = makeStateRule(Blocks.CLAY);
    private static final SurfaceRules.RuleSource MUD = makeStateRule(Blocks.MUD);



    public static SurfaceRules.RuleSource makeRules() {
        // Modded blocks only exist after registration, so they are resolved here rather than in a static field
        SurfaceRules.RuleSource peat = makeStateRule(NBlocks.PEAT.get());
        SurfaceRules.ConditionSource isAtOrAboveWaterLevel = SurfaceRules.waterBlockCheck(-1, 0);

        // Lake bed: mix of sand, gravel and clay under water, sand underneath
        SurfaceRules.RuleSource underwaterBed = SurfaceRules.ifTrue(
                SurfaceRules.not(isAtOrAboveWaterLevel),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -1.0D, -0.3D), GRAVEL),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.4D, 1.0D), CLAY),
                                        SAND
                                )
                        ),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SAND)
                )
        );

        // Bog: peat beds in the pools; moss and mud on dry ground with a peat layer under the mud
        SurfaceRules.ConditionSource mossNoise = SurfaceRules.noiseCondition(Noises.SURFACE, -1.0D, 0.0D);
        SurfaceRules.ConditionSource secondLayer = SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR);
        SurfaceRules.RuleSource bog = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.not(isAtOrAboveWaterLevel),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, peat),
                                SurfaceRules.ifTrue(secondLayer, peat),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, DIRT)
                        )
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.sequence(SurfaceRules.ifTrue(mossNoise, MOSS_BLOCK), MUD)
                ),
                SurfaceRules.ifTrue(
                        secondLayer,
                        SurfaceRules.sequence(SurfaceRules.ifTrue(mossNoise, DIRT), peat)
                ),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, DIRT)
        );

        // Beach: sand and gravel on the dry shore, y 62..64 (1-3 blocks above sea level), then forest floor
        SurfaceRules.ConditionSource nearSeaLevel = SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(65), 0));
        SurfaceRules.RuleSource beach = SurfaceRules.ifTrue(
                nearSeaLevel,
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, -1.0D, -0.4D), GRAVEL),
                                        SAND
                                )
                        ),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SAND)
                )
        );

        // Create noise-based surface distribution
        SurfaceRules.RuleSource surfaceLayer = SurfaceRules.sequence(
                // If noise is < 0, use moss (50% chance)
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.SURFACE, -1.0D, 0.0D),
                        MOSS_BLOCK
                ),
                // If noise is > 0, use grass (50% chance)
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.SURFACE, 0.0D, 1.0D),
                        GRASS_BLOCK
                ),
                // Default to grass if somehow none of the above conditions are met
                GRASS_BLOCK
        );

        // Pine forests: narrower moss band, roughly 30% less moss than the default layer
        SurfaceRules.RuleSource pineForestSurfaceLayer = SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.noiseCondition(Noises.SURFACE, -1.0D, -0.15D),
                        MOSS_BLOCK
                ),
                GRASS_BLOCK
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.BOG), bog),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.LAKE, Biomes.RIVER), underwaterBed),
                // Beach only in the lake/river biome itself, so sand never spreads into the surrounding forest
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.LAKE, Biomes.RIVER),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0), beach)
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(Biomes.PINE_FOREST, Biomes.SPRUCE_PINE_FOREST),
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.ifTrue(isAtOrAboveWaterLevel, pineForestSurfaceLayer)
                        )
                ),
                // Apply the surface rules only if we're at the surface
                SurfaceRules.ifTrue(
                        SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(
                                isAtOrAboveWaterLevel,
                                surfaceLayer
                        )
                ),
                // Underground layers should be dirt
                SurfaceRules.ifTrue(
                        SurfaceRules.UNDER_FLOOR,
                        DIRT
                )
        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
