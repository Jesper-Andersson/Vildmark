package io.github.jesperandersson.vildmark.client.model;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

// Procedural player-style animation, ported from vanilla HumanoidModel.setupAnim.
// GeckoLib bones use (-x, -y, z) relative to vanilla ModelPart rotations (see RenderUtil.matchModelPartRot),
// so vanilla's X and Y values are negated here.
public class TomteModel extends HeadTurningGeoModel<Tomte> {
    public TomteModel() {
        // The model's head bone is "Head", not GeckoLib's default lowercase "head"
        super(ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, "tomte"), "Head");
    }

    @Override
    public void setCustomAnimations(Tomte animatable, long instanceId, AnimationState<Tomte> state) {
        super.setCustomAnimations(animatable, instanceId, state);

        GeoBone rightArm = getAnimationProcessor().getBone("RightArm");
        GeoBone leftArm = getAnimationProcessor().getBone("LeftArm");
        GeoBone rightLeg = getAnimationProcessor().getBone("RightLeg");
        GeoBone leftLeg = getAnimationProcessor().getBone("LeftLeg");
        if (rightArm == null || leftArm == null || rightLeg == null || leftLeg == null)
            return;

        float limbSwing = state.getLimbSwing();
        float amount = state.getLimbSwingAmount();

        float rightArmX = Mth.cos(limbSwing * 0.6662F + Mth.PI) * amount;
        float leftArmX = Mth.cos(limbSwing * 0.6662F) * amount;
        float rightLegX = Mth.cos(limbSwing * 0.6662F) * 1.4F * amount;
        float leftLegX = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * amount;
        float rightLegY = 0;
        float leftLegY = 0;

        if (animatable.isPassenger()) {
            rightArmX += -Mth.PI / 5F;
            leftArmX += -Mth.PI / 5F;
            rightLegX = -1.4137167F;
            leftLegX = -1.4137167F;
            rightLegY = Mth.PI / 10F;
            leftLegY = -Mth.PI / 10F;
        }

        // Idle arm bob (AnimationUtils.bobModelPart)
        float age = animatable.tickCount + state.getPartialTick();
        float bobZ = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        float bobX = Mth.sin(age * 0.067F) * 0.05F;
        rightArmX += bobX;
        leftArmX -= bobX;

        rightArm.setRotX(-rightArmX);
        rightArm.setRotZ(bobZ);
        leftArm.setRotX(-leftArmX);
        leftArm.setRotZ(-bobZ);
        rightLeg.setRotX(-rightLegX);
        rightLeg.setRotY(-rightLegY);
        leftLeg.setRotX(-leftLegX);
        leftLeg.setRotY(-leftLegY);
    }
}
