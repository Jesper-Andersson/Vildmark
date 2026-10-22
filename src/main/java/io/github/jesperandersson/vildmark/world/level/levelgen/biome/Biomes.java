package io.github.jesperandersson.vildmark.world.level.levelgen.biome;

import io.github.jesperandersson.vildmark.Vildmark;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public class Biomes {
    public static final ResourceKey<Biome> PINE_FOREST = register("pine_forest");
    public static final ResourceKey<Biome> SPRUCE_PINE_FOREST = register("spruce_pine_forest");
    public static final ResourceKey<Biome> LAKE = register("lake");
    public static final ResourceKey<Biome> RIVER = register("river");
    public static final ResourceKey<Biome> BOG = register("bog");
    public static final ResourceKey<Biome> BIRCH_PINE_FOREST = register("birch_pine_forest");
    public static final ResourceKey<Biome> PLAINS = register("plains");
    public static final ResourceKey<Biome> TUNDRA = register("tundra");

    // Only creates the key. The biomes themselves are defined in the data pack
    // (data/vildmark/worldgen/biome)
    private static ResourceKey<Biome> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Vildmark.MODID, name);
        return ResourceKey.create(Registries.BIOME, id);
    }
}
