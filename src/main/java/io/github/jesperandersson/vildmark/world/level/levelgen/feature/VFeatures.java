package io.github.jesperandersson.vildmark.world.level.levelgen.feature;

import io.github.jesperandersson.vildmark.Vildmark;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Vildmark.MODID);

    public static final DeferredHolder<Feature<?>, BoulderFeature> BOULDER = FEATURES.register("boulder", () -> new BoulderFeature(BoulderConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, FallenTreeFeature> FALLEN_TREE = FEATURES.register("fallen_tree", () -> new FallenTreeFeature(FallenTreeConfiguration.CODEC));

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
