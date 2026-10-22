package io.github.jesperandersson.vildmark.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

// GeckoLib only turns a bone named "head", so this does the same for models whose head bone is named differently
public class HeadTurningGeoModel<T extends GeoAnimatable> extends DefaultedEntityGeoModel<T> {
    private final String headBone;

    public HeadTurningGeoModel(ResourceLocation assetSubpath, String headBone) {
        super(assetSubpath, false);
        this.headBone = headBone;
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        GeoBone head = getAnimationProcessor().getBone(this.headBone);
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
