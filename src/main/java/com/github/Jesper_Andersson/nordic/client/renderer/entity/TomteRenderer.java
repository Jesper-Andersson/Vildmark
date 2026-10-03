package com.github.Jesper_Andersson.nordic.client.renderer.entity;

import com.github.Jesper_Andersson.nordic.client.model.TomteModel;
import com.github.Jesper_Andersson.nordic.world.entity.npc.Tomte;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TomteRenderer extends GeoEntityRenderer<Tomte> {
    public TomteRenderer(EntityRendererProvider.Context context) {
        super(context, new TomteModel());
        this.shadowRadius = 0.25f;
    }
}
