package io.github.jesperandersson.vildmark.world.entity;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.animal.WillOTheWisp;
import io.github.jesperandersson.vildmark.world.entity.animal.Moose;
import io.github.jesperandersson.vildmark.world.entity.animal.Pike;
import io.github.jesperandersson.vildmark.world.entity.monster.BarrowWight;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Gives each entity type its attributes (health, speed, ...) and the rules for where it can spawn.
 */
@EventBusSubscriber(modid = Vildmark.MODID)
public class VEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(VEntities.BARROW_WIGHT.get(), BarrowWight.createAttributes().build());
        event.put(VEntities.WILL_O_THE_WISP.get(), WillOTheWisp.createAttributes().build());
        event.put(VEntities.PIKE.get(), Pike.createAttributes().build());
        event.put(VEntities.ROACH.get(), AbstractFish.createAttributes().build());
        event.put(VEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(VEntities.TOMTE.get(), Tomte.createAttributes().build());
    }

    // Each entry: where the spawn position must be (on the ground, in water, anywhere), which
    // height map picks the spawn height, and a check that can still reject the spot
    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        RegisterSpawnPlacementsEvent.Operation replace =
                RegisterSpawnPlacementsEvent.Operation.REPLACE;
        event.register(VEntities.WILL_O_THE_WISP.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.WORLD_SURFACE_WG,
                WillOTheWisp::checkWillOTheWispSpawnRules, replace);
        event.register(VEntities.BARROW_WIGHT.get(),
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, replace);
        event.register(VEntities.PIKE.get(),
                SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, replace);
        event.register(VEntities.ROACH.get(),
                SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, replace);
        event.register(VEntities.MOOSE.get(),
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, replace);
    }
}
