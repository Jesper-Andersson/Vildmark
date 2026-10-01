package com.github.Jesper_Andersson.nordic.world.level.levelgen.biome;

import com.github.Jesper_Andersson.nordic.Nordic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class Biomes {
    public static final ResourceKey<Biome> PINE_FOREST = register("pine_forest");
    public static final ResourceKey<Biome> SPRUCE_PINE_FOREST = register("spruce_pine_forest");
    public static final ResourceKey<Biome> LAKE = register("lake");
    public static final ResourceKey<Biome> RIVER = register("river");
    public static final ResourceKey<Biome> BOG = register("bog");

    private static ResourceKey<Biome> register(String name)
    {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Nordic.MODID, name));
    }
}
