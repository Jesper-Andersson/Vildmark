package com.github.Jesper_Andersson.nordic.mixin;

import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeToggleSource;
import com.github.Jesper_Andersson.nordic.world.level.levelgen.biome.BiomeToggles;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin implements BiomeToggleSource {
    @Shadow protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Unique private volatile boolean nordic$overworld;
    // Parameters without the disabled biomes, null if nothing would be left. Rebuilt when the toggles change
    @Unique private volatile Climate.ParameterList<Holder<Biome>> nordic$filtered;
    @Unique private volatile int nordic$filteredVersion = -1;

    @Override
    public void nordic$setOverworld() {
        nordic$overworld = true;
    }

    // Replace disabled biomes with the nearest enabled biome by climate. Wraps the whole method, so biomes picked by
    // TerraBlender regions are replaced too. possibleBiomes() stays unchanged
    @WrapMethod(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;")
    private Holder<Biome> nordic$replaceDisabled(int x, int y, int z, Climate.Sampler sampler, Operation<Holder<Biome>> original) {
        Holder<Biome> biome = original.call(x, y, z, sampler);
        if (!nordic$overworld || !BiomeToggles.isDisabled(biome)) return biome;
        return nordic$replace(biome, sampler.sample(x, y, z));
    }

    @Override
    public Holder<Biome> nordic$replace(Holder<Biome> biome, Climate.TargetPoint point) {
        if (!nordic$overworld || !BiomeToggles.isDisabled(biome)) return biome;
        Climate.ParameterList<Holder<Biome>> filtered = nordic$filtered();
        return filtered == null ? biome : filtered.findValue(point);
    }

    @Unique
    private Climate.ParameterList<Holder<Biome>> nordic$filtered() {
        int version = BiomeToggles.version();
        if (nordic$filteredVersion != version) {
            List<Pair<Climate.ParameterPoint, Holder<Biome>>> values = parameters().values().stream()
                    .filter(entry -> !BiomeToggles.isDisabled(entry.getSecond()))
                    .toList();
            nordic$filtered = values.isEmpty() ? null : new Climate.ParameterList<>(values);
            nordic$filteredVersion = version;
        }
        return nordic$filtered;
    }
}
