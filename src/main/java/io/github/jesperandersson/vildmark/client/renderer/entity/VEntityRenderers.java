package io.github.jesperandersson.vildmark.client.renderer.entity;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Vildmark.MODID, value = Dist.CLIENT)
public class VEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(VEntities.BARROW_WIGHT.get(), BarrowWightRenderer::new);
        event.registerEntityRenderer(VEntities.PIKE.get(),
                context -> new FishRenderer<>(context, VEntities.PIKE.get()));
        event.registerEntityRenderer(VEntities.ROACH.get(),
                context -> new FishRenderer<>(context, VEntities.ROACH.get()));
        event.registerEntityRenderer(VEntities.MOOSE.get(), MooseRenderer::new);
        event.registerEntityRenderer(VEntities.TOMTE.get(), TomteRenderer::new);
        event.registerEntityRenderer(VEntities.WILL_O_THE_WISP.get(), WillOTheWispRenderer::new);
    }
}
