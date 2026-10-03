package com.github.Jesper_Andersson.nordic.world.level.levelgen.biome;

import com.github.Jesper_Andersson.nordic.Config;
import com.github.Jesper_Andersson.nordic.Nordic;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Which overworld biomes are turned off. The config is read once when a world starts, so it stays fixed while the
 * world runs, and the menu changes only apply on the next world load.
 */
@EventBusSubscriber(modid = Nordic.MODID)
public final class BiomeToggles {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final List<ResourceKey<Biome>> NORDIC_BIOMES = List.of(
            Biomes.PINE_FOREST, Biomes.SPRUCE_PINE_FOREST, Biomes.LAKE, Biomes.RIVER, Biomes.BOG,
            Biomes.BIRCH_PINE_FOREST, Biomes.PLAINS, Biomes.TUNDRA);

    private static volatile Set<ResourceKey<Biome>> disabled = Set.of();
    // Bumped on every snapshot, so biome sources know to rebuild their filtered parameter lists
    private static volatile int version;

    private BiomeToggles() {}

    public static void snapshot() {
        Set<ResourceKey<Biome>> keys = new HashSet<>();
        for (String id : Config.DISABLED_BIOMES.get()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) keys.add(ResourceKey.create(Registries.BIOME, location));
        }
        disabled = Set.copyOf(keys);
        version++;
        if (!keys.isEmpty()) LOGGER.info("Disabled overworld biomes: {}", keys.stream().map(ResourceKey::location).toList());
    }

    public static int version() {
        return version;
    }

    public static boolean anyDisabled() {
        return !disabled.isEmpty();
    }

    public static boolean isDisabled(Holder<Biome> biome) {
        Set<ResourceKey<Biome>> keys = disabled;
        return !keys.isEmpty() && biome.unwrapKey().map(keys::contains).orElse(false);
    }

    /** Overworld biomes from the last loaded world, so the menu can list biomes from other mods. */
    public static Path knownBiomesFile() {
        return FMLPaths.CONFIGDIR.get().resolve("nordic-known-biomes.json");
    }

    /** Vanilla, Nordic, cached and currently disabled biome ids, so a disabled biome can always be turned back on. */
    public static Set<ResourceLocation> knownBiomes() {
        Set<ResourceLocation> ids = new TreeSet<>();
        MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.usedBiomes().forEach(key -> ids.add(key.location()));
        NORDIC_BIOMES.forEach(key -> ids.add(key.location()));
        Path file = knownBiomesFile();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                    ResourceLocation location = ResourceLocation.tryParse(element.getAsString());
                    if (location != null) ids.add(location);
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Couldn't read {}", file, e);
            }
        }
        for (String id : Config.DISABLED_BIOMES.get()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) ids.add(location);
        }
        return ids;
    }

    @SubscribeEvent
    static void onServerAboutToStart(ServerAboutToStartEvent event) {
        snapshot();
    }

    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        JsonArray ids = new JsonArray();
        overworld.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
                .flatMap(biome -> biome.unwrapKey().stream())
                .map(key -> key.location().toString())
                .sorted()
                .forEach(ids::add);
        Path file = knownBiomesFile();
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(ids, writer);
        } catch (IOException e) {
            LOGGER.warn("Couldn't write {}", file, e);
        }
    }
}
