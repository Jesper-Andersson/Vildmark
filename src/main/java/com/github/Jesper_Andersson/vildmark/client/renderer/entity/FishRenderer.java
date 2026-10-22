package com.github.Jesper_Andersson.vildmark.client.renderer.entity;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.AbstractFish;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FishRenderer<T extends AbstractFish & GeoEntity> extends GeoEntityRenderer<T> {
    public FishRenderer(EntityRendererProvider.Context context, String name) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, name)));
        this.shadowRadius = 0.3f;
    }

    // Same as vanilla SalmonRenderer.setupRotations: body sway, and lying on its side out of water
    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        boolean inWater = animatable.isInWater();
        float amplitude = inWater ? 1.0f : 1.3f;
        float speed = inWater ? 1.0f : 1.7f;
        poseStack.mulPose(Axis.YP.rotationDegrees(amplitude * 4.3f * Mth.sin(speed * 0.6f * ageInTicks)));
        if (!inWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        }
    }
}
