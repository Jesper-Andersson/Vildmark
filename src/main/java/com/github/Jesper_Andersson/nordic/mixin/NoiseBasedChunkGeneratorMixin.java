package com.github.Jesper_Andersson.nordic.mixin;

import com.github.Jesper_Andersson.nordic.world.level.levelgen.lake.LakeBasin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    // Fix biomes that don't match the final terrain: raised bog and dry lake or river become forest, land biomes in
    // open water become ocean
    // Wraps the chunk's resolver rather than the biome source, since the lake basin reads the biome source
    @WrapOperation(method = "doCreateBiomes", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkAccess;fillBiomesFromNoise(Lnet/minecraft/world/level/biome/BiomeResolver;Lnet/minecraft/world/level/biome/Climate$Sampler;)V"))
    private void nordic$fixBiomes(ChunkAccess chunk, BiomeResolver resolver, Climate.Sampler sampler,
                                  Operation<Void> original, @Local(argsOnly = true) RandomState random) {
        original.call(chunk, LakeBasin.wrapBiomes(random, resolver), sampler);
    }
}
