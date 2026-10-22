package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.Vildmark;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Vildmark.MODID)
public class VDataGenerators {
    // The clientData run only fires the Client event, so server data (loot tables, recipes) is added here too
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(VModelProvider::new);
        event.createProvider((output, lookupProvider) -> new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(VBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(VEntityLootProvider::new, LootContextParamSets.ENTITY)), lookupProvider));
        event.createProvider(VRecipeProvider.Runner::new);
    }
}
