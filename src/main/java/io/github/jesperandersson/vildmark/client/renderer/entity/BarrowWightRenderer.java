package io.github.jesperandersson.vildmark.client.renderer.entity;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.monster.BarrowWight;
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
