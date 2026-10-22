package io.github.jesperandersson.vildmark.compat;

//import com.github.Jesper_Andersson.biomepicknchoose.common.BiomeToggles;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;

/** Optional Biome Pick'n'Choose support. Its classes are only touched when it is installed. */
public final class BiomePickCompat {
    public static final String MODID = "biomepicknchoose";

    private BiomePickCompat() {}

    /**
     * Whether the biome is turned off in Biome Pick'n'Choose. Always false when it isn't installed.
     */
    public static boolean isDisabled(Holder<Biome> biome) {
        return Loaded.LOADED && Toggles.isDisabled(biome);
    }

    // Holder class, so ModList is only asked once, the first time isDisabled runs
    private static final class Loaded {
        static final boolean LOADED = ModList.get().isLoaded(MODID);
    }

    // Separate class, so BiomeToggles is only resolved when the mod is installed
    private static final class Toggles {
        static boolean isDisabled(Holder<Biome> biome) {
            // TODO: restore once Biome Pick'n'Choose has a 1.21.4 build (re-enable it in
            // build.gradle too)
            //return BiomeToggles.isDisabled(biome);
            return false;
        }
    }
}
