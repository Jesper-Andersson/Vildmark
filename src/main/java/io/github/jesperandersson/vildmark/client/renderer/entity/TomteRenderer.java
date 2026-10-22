package io.github.jesperandersson.vildmark.client.renderer.entity;

import com.geckolib.constant.DefaultAnimations;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.github.jesperandersson.vildmark.world.entity.VEntities;
import io.github.jesperandersson.vildmark.world.entity.npc.Tomte;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Renders the tomte with procedural player-style limb animation, ported from vanilla
 * HumanoidModel.setupAnim. GeckoLib bones use (-x, -y, z) relative to vanilla ModelPart rotations,
 * so vanilla's X and Y values are negated here.
 */
public class TomteRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<Tomte, R> {
    // Carries isPassenger from the entity to the render state, which has no field for it
    private static final DataTicket<Boolean> IS_PASSENGER =
            DataTicket.create("vildmark_is_passenger", Boolean.class);
    // Radians of limb swing per block walked, from vanilla HumanoidModel
    private static final float LIMB_SWING_FREQUENCY = 0.6662F;
    // A sitting player's legs point forward and slightly down: -0.45 * PI radians
    private static final float RIDING_LEG_PITCH = -1.4137167F;

    public TomteRenderer(EntityRendererProvider.Context context) {
        super(context, VEntities.TOMTE.get());
        this.shadowRadius = 0.25f;
    }

    @Override
    public void addRenderData(
            Tomte animatable, Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(IS_PASSENGER, animatable.isPassenger());
    }

    @Override
    public void adjustModelBonesForRender(
            RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        // The model's head bone is "Head", not GeckoLib's usual lowercase "head"
        DefaultAnimations.hardcodedHeadRotation(renderPassInfo, snapshots, "Head");

        R renderState = renderPassInfo.renderState();
        // How far it has walked, and how fast it walks right now (0 standing, 1 full speed)
        float walkDistance = renderState.walkAnimationPos;
        float walkSpeed = renderState.walkAnimationSpeed;
        boolean isPassenger = renderPassInfo.getOrDefaultGeckolibData(IS_PASSENGER, false);

        // Idle arm bob (AnimationUtils.bobModelPart)
        float bobZ = Mth.cos(renderState.ageInTicks * 0.09F) * 0.05F + 0.05F;
        float bobX = Mth.sin(renderState.ageInTicks * 0.067F) * 0.05F;

        // Riding: arms forward, legs raised and spread like a sitting player
        float armRideOffset = isPassenger ? -Mth.PI / 5F : 0;
        // Each arm swings with the opposite leg; adding PI puts a limb half a step out of phase
        float swingPhase = walkDistance * LIMB_SWING_FREQUENCY;
        float rightArmX = Mth.cos(swingPhase + Mth.PI) * walkSpeed + armRideOffset + bobX;
        float leftArmX = Mth.cos(swingPhase) * walkSpeed + armRideOffset - bobX;
        float rightLegX = isPassenger ? RIDING_LEG_PITCH : Mth.cos(swingPhase) * 1.4F * walkSpeed;
        float leftLegX = isPassenger
                ? RIDING_LEG_PITCH
                : Mth.cos(swingPhase + Mth.PI) * 1.4F * walkSpeed;
        float legSpreadY = isPassenger ? Mth.PI / 10F : 0;

        snapshots.ifPresent("RightArm", bone -> bone.setRotX(-rightArmX).setRotZ(bobZ));
        snapshots.ifPresent("LeftArm", bone -> bone.setRotX(-leftArmX).setRotZ(-bobZ));
        snapshots.ifPresent("RightLeg", bone -> bone.setRotX(-rightLegX).setRotY(-legSpreadY));
        snapshots.ifPresent("LeftLeg", bone -> bone.setRotX(-leftLegX).setRotY(legSpreadY));
    }
}
