package io.github.jesperandersson.vildmark.client.renderer.entity;

import io.github.jesperandersson.vildmark.client.model.TomteModel;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TomteRenderer extends GeoEntityRenderer<Tomte> {
    public TomteRenderer(EntityRendererProvider.Context context) {
        super(context, new TomteModel());
        this.shadowRadius = 0.25f;
    }
}
