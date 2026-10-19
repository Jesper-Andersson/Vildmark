package com.github.Jesper_Andersson.nordic.client.renderer.entity;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.entity.animal.WillOTheWisp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class WillOTheWispRenderer extends GeoEntityRenderer<WillOTheWisp> {
    public WillOTheWispRenderer(EntityRendererProvider.Context context) {
        // Resolves geo/entity/, animations/entity/ and textures/entity/barrow_wight.*; true = rotate the "head" bone
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Nordic.MODID, "willothewisp"), false));
        this.shadowRadius = 0.0f;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
