package com.github.Jesper_Andersson.nordic.mixin;

import com.github.Jesper_Andersson.nordic.world.level.levelgen.lake.LakeBasin;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow @Final ServerLevel level;
    @Shadow @Final private RandomState randomState;

    @Shadow protected abstract ChunkGenerator generator();

    // Carve lake basins into the overworld terrain once the dimension's RandomState exists
    // Runs after Lithostitched wraps the router, since it reads the wired randomState.router()
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nordic$addLakeBasins(CallbackInfo ci) {
        if (level.dimension() != Level.OVERWORLD) return;
        if (generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
            ((RandomStateAccessor) (Object) randomState).nordic$setRouter(LakeBasin.wrapRouter(
                    randomState, noiseGenerator.getBiomeSource(), noiseGenerator.generatorSettings().value().noiseSettings(),
                    noiseGenerator.generatorSettings().value().seaLevel()));
        }
    }
}
