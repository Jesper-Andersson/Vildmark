package com.github.Jesper_Andersson.nordic.client.renderer.entity;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.entity.NEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Nordic.MODID, value = Dist.CLIENT)
public class NEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NEntities.BARROW_WIGHT.get(), BarrowWightRenderer::new);
        event.registerEntityRenderer(NEntities.PIKE.get(), ctx -> new FishRenderer<>(ctx, "pike"));
        event.registerEntityRenderer(NEntities.ROACH.get(), ctx -> new FishRenderer<>(ctx, "roach"));
        event.registerEntityRenderer(NEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(NEntities.TOMTE.get(), TomteRenderer::new);
    }
}
