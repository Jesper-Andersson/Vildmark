package io.github.jesperandersson.vildmark.client.renderer.entity;

import com.geckolib.constant.DefaultAnimations;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.entity.monster.BarrowWight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Renders the barrow wight's GeckoLib model. AutoGlowingGeoLayer draws the glowmask texture on
 * top at full brightness.
 *
 * <p>R is the render state: a snapshot of the entity taken each frame, which the renderer draws
 * from instead of reading the entity directly.
 */
public class BarrowWightRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<BarrowWight, R> {
    public BarrowWightRenderer(EntityRendererProvider.Context context) {
        // Resolves geckolib/models/entity/, geckolib/animations/entity/ and
        // textures/entity/barrow_wight.*
        super(context, VEntities.BARROW_WIGHT.get());
        this.shadowRadius = 0.2f;
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    // Turns the head bone toward where the entity looks
    @Override
    public void adjustModelBonesForRender(
            RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        DefaultAnimations.hardcodedHeadRotation(renderPassInfo, snapshots, "head");
    }
}
