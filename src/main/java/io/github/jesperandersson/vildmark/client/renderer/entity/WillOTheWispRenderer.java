package io.github.jesperandersson.vildmark.client.renderer.entity;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.entity.animal.WillOTheWisp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class WillOTheWispRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<WillOTheWisp, R> {
    public WillOTheWispRenderer(EntityRendererProvider.Context context) {
        // Resolves geckolib/models/entity/, geckolib/animations/entity/ and
        // textures/entity/willothewisp.*
        super(context, VEntities.WILL_O_THE_WISP.get());
        this.shadowRadius = 0.0f;
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
