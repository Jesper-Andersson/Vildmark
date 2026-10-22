package io.github.jesperandersson.vildmark.mixin;

import io.github.jesperandersson.vildmark.world.level.levelgen.lake.LakeBasin;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
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

    // Carves lake basins into the overworld terrain once the dimension's RandomState exists.
    // Runs after Lithostitched wraps the router, since it reads the wired randomState.router()
    @Inject(method = "<init>", at = @At("TAIL"))
    private void vildmark$addLakeBasins(CallbackInfo callbackInfo) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!(generator() instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            return;
        }
        NoiseGeneratorSettings settings = noiseGenerator.generatorSettings().value();
        NoiseRouter basinRouter = LakeBasin.wrapRouter(randomState, noiseGenerator.getBiomeSource(),
                settings.noiseSettings(), settings.seaLevel());
        // RandomState has no setter for its router, so the accessor mixin adds one. The cast
        // through Object is needed because the compiler doesn't know the mixin adds the interface
        ((RandomStateAccessor) (Object) randomState).vildmark$setRouter(basinRouter);
    }
}
