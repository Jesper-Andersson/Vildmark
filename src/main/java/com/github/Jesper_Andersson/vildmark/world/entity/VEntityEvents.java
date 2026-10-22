package com.github.Jesper_Andersson.vildmark.world.entity;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.github.Jesper_Andersson.vildmark.world.entity.animal.WillOTheWisp;
import com.github.Jesper_Andersson.vildmark.world.entity.animal.Moose;
import com.github.Jesper_Andersson.vildmark.world.entity.animal.Pike;
import com.github.Jesper_Andersson.vildmark.world.entity.monster.BarrowWight;
import com.github.Jesper_Andersson.vildmark.world.entity.npc.Tomte;
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
        event.put(VEntities.BARROW_WIGHT.get(), BarrowWight.createAttributes().build());
        event.put(VEntities.WILL_O_THE_WISP.get(), WillOTheWisp.createAttributes().build());
        event.put(VEntities.PIKE.get(), Pike.createAttributes().build());
        event.put(VEntities.ROACH.get(), AbstractFish.createAttributes().build());
        event.put(VEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(VEntities.TOMTE.get(), Tomte.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(VEntities.WILL_O_THE_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS,Heightmap.Types.WORLD_SURFACE_WG,
                WillOTheWisp::checkWillOTheWispSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VEntities.BARROW_WIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VEntities.PIKE.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VEntities.ROACH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VEntities.MOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
