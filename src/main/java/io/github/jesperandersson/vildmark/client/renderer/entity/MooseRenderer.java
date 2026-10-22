package io.github.jesperandersson.vildmark.client.renderer.entity;

import com.geckolib.constant.DefaultAnimations;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.entity.animal.Moose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class MooseRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<Moose, R> {
    public MooseRenderer(EntityRendererProvider.Context context) {
        super(context, VEntities.MOOSE.get());
        this.shadowRadius = 0.9f;
    }

    @Override
    public void scaleModelForRender(
            RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        // GeckoLib doesn't shrink babies on its own
        if (renderPassInfo.renderState().isBaby) {
            widthScale *= 0.5f;
            heightScale *= 0.5f;
        }

        super.scaleModelForRender(renderPassInfo, widthScale, heightScale);
    }

    @Override
    public void adjustModelBonesForRender(
            RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        // The model's head bone is "Head", not GeckoLib's usual lowercase "head"
        DefaultAnimations.hardcodedHeadRotation(renderPassInfo, snapshots, "Head");
    }
}
