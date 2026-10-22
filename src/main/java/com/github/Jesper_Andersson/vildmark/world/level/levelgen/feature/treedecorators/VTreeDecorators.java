package com.github.Jesper_Andersson.vildmark.world.level.levelgen.feature.treedecorators;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VTreeDecorators {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, Vildmark.MODID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PlaceOnGroundDecorator>> PLACE_ON_GROUND =
            TREE_DECORATOR_TYPES.register("place_on_ground", () -> new TreeDecoratorType<>(PlaceOnGroundDecorator.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PineBarkDecorator>> PINE_BARK =
            TREE_DECORATOR_TYPES.register("pine_bark", () -> new TreeDecoratorType<>(PineBarkDecorator.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<BeardLichenDecorator>> BEARD_LICHEN =
            TREE_DECORATOR_TYPES.register("beard_lichen", () -> new TreeDecoratorType<>(BeardLichenDecorator.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PineConkDecorator>> PINE_CONK =
            TREE_DECORATOR_TYPES.register("pine_conk", () -> new TreeDecoratorType<>(PineConkDecorator.CODEC));

    public static void register(IEventBus modEventBus) {
        TREE_DECORATOR_TYPES.register(modEventBus);
    }
}
