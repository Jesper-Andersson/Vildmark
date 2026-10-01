package com.github.Jesper_Andersson.nordic.world.entity;

import com.github.Jesper_Andersson.nordic.world.entity.animal.Pike;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Roach;
import com.github.Jesper_Andersson.nordic.world.entity.monster.BarrowWight;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.github.Jesper_Andersson.nordic.Nordic.ENTITY_TYPES;

public class NEntities {
    public static final DeferredHolder<EntityType<?>, EntityType<BarrowWight>> BARROW_WIGHT = ENTITY_TYPES.register("barrow_wight", () -> EntityType.Builder.of(BarrowWight::new, MobCategory.MONSTER).sized(0.7f, 2.3f).clientTrackingRange(8).build("barrow_wight"));
    public static final DeferredHolder<EntityType<?>, EntityType<Pike>> PIKE = ENTITY_TYPES.register("pike", () -> EntityType.Builder.of(Pike::new, MobCategory.WATER_AMBIENT).sized(0.9f, 0.4f).clientTrackingRange(4).build("pike"));
    public static final DeferredHolder<EntityType<?>, EntityType<Roach>> ROACH = ENTITY_TYPES.register("roach", () -> EntityType.Builder.of(Roach::new, MobCategory.WATER_AMBIENT).sized(0.5f, 0.3f).clientTrackingRange(4).build("roach"));

    public static void register() {}
}
