package com.github.Jesper_Andersson.vildmark.client.renderer.entity;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.github.Jesper_Andersson.vildmark.world.entity.VEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Vildmark.MODID, value = Dist.CLIENT)
public class VEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(VEntities.BARROW_WIGHT.get(), BarrowWightRenderer::new);
        event.registerEntityRenderer(VEntities.PIKE.get(), ctx -> new FishRenderer<>(ctx, "pike"));
        event.registerEntityRenderer(VEntities.ROACH.get(), ctx -> new FishRenderer<>(ctx, "roach"));
        event.registerEntityRenderer(VEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(VEntities.TOMTE.get(), TomteRenderer::new);
        event.registerEntityRenderer(VEntities.WILL_O_THE_WISP.get(), WillOTheWispRenderer::new);
    }
}
