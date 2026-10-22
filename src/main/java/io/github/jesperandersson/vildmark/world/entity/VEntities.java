package io.github.jesperandersson.vildmark.world.entity;

import io.github.jesperandersson.vildmark.world.entity.animal.WillOTheWisp;
import io.github.jesperandersson.vildmark.world.entity.animal.Moose;
import io.github.jesperandersson.vildmark.world.entity.animal.Pike;
import io.github.jesperandersson.vildmark.world.entity.animal.Roach;
import io.github.jesperandersson.vildmark.world.entity.monster.BarrowWight;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static io.github.jesperandersson.vildmark.Vildmark.ENTITY_TYPES;

public class VEntities {
    public static final DeferredHolder<EntityType<?>, EntityType<BarrowWight>> BARROW_WIGHT = ENTITY_TYPES.registerEntityType("barrow_wight", BarrowWight::new, MobCategory.MONSTER,
            builder -> builder
                    .sized(0.7f, 2.3f)
                    .clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<WillOTheWisp>> WILL_O_THE_WISP = ENTITY_TYPES.registerEntityType("willothewisp", WillOTheWisp::new, MobCategory.AMBIENT,
            builder -> builder
                    .sized(0.7f, 0.7f)
                    .clientTrackingRange(12));

    public static final DeferredHolder<EntityType<?>, EntityType<Pike>> PIKE = ENTITY_TYPES.registerEntityType("pike", Pike::new, MobCategory.WATER_AMBIENT,
            builder -> builder
                    .sized(0.9f, 0.4f)
                    .clientTrackingRange(4));

    public static final DeferredHolder<EntityType<?>, EntityType<Roach>> ROACH = ENTITY_TYPES.registerEntityType("roach", Roach::new, MobCategory.WATER_AMBIENT,
            builder -> builder
                    .sized(0.5f, 0.3f)
                    .clientTrackingRange(4));

    public static final DeferredHolder<EntityType<?>, EntityType<Moose>> MOOSE = ENTITY_TYPES.registerEntityType("moose", Moose::new, MobCategory.CREATURE,
            builder -> builder
                    .sized(1.5f, 3.3f)
                    .clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<Tomte>> TOMTE = ENTITY_TYPES.registerEntityType("tomte", Tomte::new, MobCategory.CREATURE,
            builder -> builder
                    .sized(0.4f, 0.75f)
                    .clientTrackingRange(10));

    public static void register() {}
}
