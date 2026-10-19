package com.github.Jesper_Andersson.nordic.world.entity;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.entity.animal.WillOTheWisp;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Moose;
import com.github.Jesper_Andersson.nordic.world.entity.animal.Pike;
import com.github.Jesper_Andersson.nordic.world.entity.monster.BarrowWight;
import com.github.Jesper_Andersson.nordic.world.entity.npc.Tomte;
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

@EventBusSubscriber(modid = Nordic.MODID)
public class NEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(NEntities.BARROW_WIGHT.get(), BarrowWight.createAttributes().build());
        event.put(NEntities.WILL_O_THE_WISP.get(), WillOTheWisp.createAttributes().build());
        event.put(NEntities.PIKE.get(), Pike.createAttributes().build());
        event.put(NEntities.ROACH.get(), AbstractFish.createAttributes().build());
        event.put(NEntities.MOOSE.get(), Moose.createAttributes().build());
        event.put(NEntities.TOMTE.get(), Tomte.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(NEntities.BARROW_WIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NEntities.PIKE.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NEntities.ROACH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NEntities.MOOSE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
