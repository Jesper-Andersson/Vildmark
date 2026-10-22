package io.github.jesperandersson.vildmark.client.renderer.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.AbstractFish;

/** Renders a GeckoLib fish with the same swaying and flopping as vanilla fish. */
public class FishRenderer<
                T extends AbstractFish & GeoEntity,
                R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<T, R> {
    public FishRenderer(
            EntityRendererProvider.Context context, EntityType<? extends T> entityType) {
        super(context, entityType);
        this.shadowRadius = 0.3f;
    }

    // Same as vanilla SalmonRenderer.setupRotations: body sway, and lying on its side out of water
    @Override
    protected void applyRotations(
            RenderPassInfo<R> renderPassInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);
        R renderState = renderPassInfo.renderState();
        boolean isInWater = renderState.isInWater;
        // Out of water the fish sways wider and faster
        float amplitude = isInWater ? 1.0f : 1.3f;
        float speed = isInWater ? 1.0f : 1.7f;
        float swayDegrees = amplitude * 4.3f * Mth.sin(speed * 0.6f * renderState.ageInTicks);
        poseStack.mulPose(Axis.YP.rotationDegrees(swayDegrees));
        if (!isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        }
    }
}
