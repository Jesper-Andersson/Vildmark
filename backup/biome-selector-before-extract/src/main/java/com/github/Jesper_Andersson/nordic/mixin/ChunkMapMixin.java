package com.github.Jesper_Andersson.nordic.mixin;

import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeToggleSource;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeToggles;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.lake.LakeBasin;
import com.mojang.logging.LogUtils;
import dev.worldgen.lithostitched.impl.worldgen.biomeinjector.internal.InjectorBiomeSource;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Unique private static final Logger nordic$LOGGER = LogUtils.getLogger();
    @Unique private static boolean nordic$warnedUnmarked;

    @Shadow @Final ServerLevel level;
    @Shadow @Final private RandomState randomState;

    @Shadow protected abstract ChunkGenerator generator();

    // Carve lake basins into the overworld terrain once the dimension's RandomState exists
    // Runs after Lithostitched wraps the router, since it reads the wired randomState.router()
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nordic$addLakeBasins(CallbackInfo ci) {
        if (level.dimension() != Level.OVERWORLD) return;
        // Mark the overworld biome source first, so disabled biomes are replaced, also where the lake basin reads them
        // Wrapping sources call through to the marked source, so mark the multi-noise source inside them
        BiomeSource biomeSource = nordic$rootSource(generator().getBiomeSource());
        if (biomeSource instanceof BiomeToggleSource source) {
            source.nordic$setOverworld();
        } else if (BiomeToggles.anyDisabled() && !nordic$warnedUnmarked) {
            nordic$warnedUnmarked = true;
            nordic$LOGGER.warn("Overworld biome source {} is not a multi-noise source, disabled biomes will still generate",
                    biomeSource.getClass().getName());
        }
        if (generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
            ((RandomStateAccessor) (Object) randomState).nordic$setRouter(LakeBasin.wrapRouter(
                    randomState, noiseGenerator.getBiomeSource(), noiseGenerator.generatorSettings().value().noiseSettings(),
                    noiseGenerator.generatorSettings().value().seaLevel()));
        }
    }

    // Unwrap Lithostitched's InjectorBiomeSource (added when any datapack has a biome injector) and Blueprint's
    // ModdedBiomeSource, like Lithostitched's own InjectorBiomeSource.getRootSource
    @Unique
    private static BiomeSource nordic$rootSource(BiomeSource biomeSource) {
        while (true) {
            if (biomeSource instanceof InjectorBiomeSource injector) {
                biomeSource = injector.rootDelegate();
            } else if (biomeSource.getClass().getSimpleName().equals("ModdedBiomeSource")) {
                try {
                    Field field = biomeSource.getClass().getDeclaredField("originalSource");
                    field.setAccessible(true);
                    biomeSource = (BiomeSource) field.get(biomeSource);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    nordic$LOGGER.warn("Could not unwrap biome source {}", biomeSource.getClass().getName(), e);
                    return biomeSource;
                }
            } else {
                return biomeSource;
            }
        }
    }
}
