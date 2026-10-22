package com.github.Jesper_Andersson.vildmark.client.renderer.entity;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.github.Jesper_Andersson.vildmark.world.entity.monster.BarrowWight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BarrowWightRenderer extends GeoEntityRenderer<BarrowWight> {
    public BarrowWightRenderer(EntityRendererProvider.Context context) {
        // Resolves geo/entity/, animations/entity/ and textures/entity/barrow_wight.*; true = rotate the "head" bone
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, "barrow_wight"), true));
        this.shadowRadius = 0.2f;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
