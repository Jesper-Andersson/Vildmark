package com.github.Jesper_Andersson.nordic.world.level.levelgen.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRuleData {
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource MOSS_BLOCK = makeStateRule(Blocks.MOSS_BLOCK);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);



    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource isAtOrAboveWaterLevel = SurfaceRules.waterBlockCheck(-1, 0);

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

        return SurfaceRules.sequence(
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
