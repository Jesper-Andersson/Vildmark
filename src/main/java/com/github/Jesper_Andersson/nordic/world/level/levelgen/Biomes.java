package com.github.Jesper_Andersson.nordic.world.level.levelgen;

import com.github.Jesper_Andersson.nordic.Nordic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class Biomes {
    public static final ResourceKey<Biome> PINE_FOREST = register("pine_forest");

    private static ResourceKey<Biome> register(String name)
    {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(Nordic.MODID, name));
    }
}
