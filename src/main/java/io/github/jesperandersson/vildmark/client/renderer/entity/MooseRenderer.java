package io.github.jesperandersson.vildmark.client.renderer.entity;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.animal.Moose;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MooseRenderer extends GeoEntityRenderer<Moose> {
    public MooseRenderer(EntityRendererProvider.Context context) {
        // The model's head bone is "Head", not GeckoLib's default lowercase "head"
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, "moose"), "Head"));
        this.shadowRadius = 0.9f;
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, Moose animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // GeckoLib doesn't shrink babies on its own
        if (animatable.isBaby()) {
            widthScale *= 0.5f;
            heightScale *= 0.5f;
        }

        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
