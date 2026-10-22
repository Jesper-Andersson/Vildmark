package io.github.jesperandersson.vildmark.world.entity;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.animal.WillOTheWisp;
import io.github.jesperandersson.vildmark.world.entity.animal.Moose;
import io.github.jesperandersson.vildmark.world.entity.animal.Pike;
import io.github.jesperandersson.vildmark.world.entity.monster.BarrowWight;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = Vildmark.MODID)
public class VEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.BARROW_WIGHT.get(), BarrowWight.createAttributes().build());
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.WILL_O_THE_WISP.get(), WillOTheWisp.createAttributes().build());
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.PIKE.get(), Pike.createAttributes().build());
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.ROACH.get(), AbstractFish.createAttributes().build());
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(io.github.jesperandersson.vildmark.world.entity.VEntities.TOMTE.get(), Tomte.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(io.github.jesperandersson.vildmark.world.entity.VEntities.WILL_O_THE_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,Heightmap.Types.WORLD_SURFACE_WG,
                WillOTheWisp::checkWillOTheWispSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(io.github.jesperandersson.vildmark.world.entity.VEntities.BARROW_WIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(io.github.jesperandersson.vildmark.world.entity.VEntities.PIKE.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(io.github.jesperandersson.vildmark.world.entity.VEntities.ROACH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(io.github.jesperandersson.vildmark.world.entity.VEntities.MOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
