package com.github.Jesper_Andersson.nordic.world.entity;

import com.github.Jesper_Andersson.nordic.world.entity.animal.WillOTheWisp;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Moose;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Pike;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Roach;
import com.github.Jesper_Andersson.nordic.world.entity.monster.BarrowWight;
import com.github.Jesper_Andersson.nordic.world.entity.npc.Tomte;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.github.Jesper_Andersson.nordic.Nordic.ENTITY_TYPES;

public class NEntities {
    public static final DeferredHolder<EntityType<?>, EntityType<BarrowWight>> BARROW_WIGHT = ENTITY_TYPES.register("barrow_wight",
            () -> EntityType.Builder.of(BarrowWight::new, MobCategory.MONSTER)
                    .sized(0.7f, 2.3f)
                    .clientTrackingRange(8)
                    .build("barrow_wight"));

    public static final DeferredHolder<EntityType<?>, EntityType<WillOTheWisp>> WILL_O_THE_WISP = ENTITY_TYPES.register("willothewisp",
            () -> EntityType.Builder.of(WillOTheWisp::new, MobCategory.AMBIENT)
                    .sized(0.7f, 0.7f)
                    .clientTrackingRange(12)
                    .build("willothewisp"));

    public static final DeferredHolder<EntityType<?>, EntityType<Pike>> PIKE = ENTITY_TYPES.register("pike",
            () -> EntityType.Builder.of(Pike::new, MobCategory.WATER_AMBIENT)
                    .sized(0.9f, 0.4f)
                    .clientTrackingRange(4)
                    .build("pike"));

    public static final DeferredHolder<EntityType<?>, EntityType<Roach>> ROACH = ENTITY_TYPES.register("roach",
            () -> EntityType.Builder.of(Roach::new, MobCategory.WATER_AMBIENT)
                    .sized(0.5f, 0.3f)
                    .clientTrackingRange(4)
                    .build("roach"));

    public static final DeferredHolder<EntityType<?>, EntityType<Moose>> MOOSE = ENTITY_TYPES.register("moose",
            () -> EntityType.Builder.of(Moose::new, MobCategory.CREATURE)
                    .sized(1.5f, 3.3f)
                    .clientTrackingRange(10)
                    .build("moose"));

    public static final DeferredHolder<EntityType<?>, EntityType<Tomte>> TOMTE = ENTITY_TYPES.register("tomte",
            () -> EntityType.Builder.of(Tomte::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.75f)
                    .clientTrackingRange(10)
                    .build("tomte"));

    public static void register() {}
}
