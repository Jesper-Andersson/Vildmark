package io.github.jesperandersson.vildmark.world.entity.animal;

import io.github.jesperandersson.vildmark.core.particles.VParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

/**
 * A glowing light that drifts slowly through the dark. Based on the bat for its spawning and
 * ambient mob category, with its own aimless flight instead of the bat's.
 */
public class WillOTheWisp extends Bat implements GeoEntity {
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("hover");
    // On average one in this many ticks picks a new flight target even when the old one is fine
    private static final int NEW_TARGET_CHANCE = 200;
    // Blocks per tick it tries to fly at. Air drag (x0.91 per tick) holds it at about half of
    // this, so 0.1 cruises at about 1 block per second
    private static final double CRUISE_SPEED = 0.1;
    // How quickly the velocity turns toward the target: 0.1 closes a tenth of the gap per tick
    private static final double STEERING_STRENGTH = 0.1;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private BlockPos flightTarget;

    public WillOTheWisp(EntityType<? extends Bat> entityType, Level level) {
        super(entityType, level);
        this.setResting(false);
        this.setNoGravity(true);
    }

    // Spawns under open sky, more often the darker it is: the light level must be at most a
    // random 0-6, so light 7 and above never spawns one
    public static boolean checkWillOTheWispSpawnRules(
            EntityType<WillOTheWisp> entityType, LevelAccessor level, EntitySpawnReason spawnReason,
            BlockPos pos, RandomSource random) {
        if (!level.canSeeSky(pos)) {
            return false;
        }
        int lightLevel = level.getMaxLocalRawBrightness(pos);
        int maxLightLevel = random.nextInt(7);
        return lightLevel <= maxLightLevel && level.isEmptyBlock(pos);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            spawnParticles();
        }
        super.aiStep();
    }

    // A flame in the middle and smoke rising around it
    private void spawnParticles() {
        if (random.nextInt(7) == 0) {
            double flameX = this.getRandomX(0.25F);
            double flameY = this.getRandomY();
            double flameZ = this.getRandomZ(0.25F);
            this.level().addParticle(VParticleTypes.WISP_FLAME.get(),
                    flameX, flameY, flameZ, 0.0F, 0.0F, 0.0F);
        }
        if (random.nextInt(4) == 0) {
            double smokeX = this.getRandomX(0.5F);
            double smokeY = this.getRandomY();
            double smokeZ = this.getRandomZ(0.5F);
            this.level().addParticle(ParticleTypes.SMOKE,
                    smokeX, smokeY, smokeZ, 0.0F, /* speedY= */ 0.1F, 0.0F);
        }
    }

    // Replaces the bat's flight: drifts toward a random nearby target, and picks a new one when it
    // arrives, when the target gets blocked, or at random
    @Override
    protected void customServerAiStep(ServerLevel level) {
        boolean needsNewTarget = flightTarget == null
                || !level().isEmptyBlock(flightTarget)
                || random.nextInt(NEW_TARGET_CHANCE) == 0
                || flightTarget.closerToCenterThan(position(), 1.0);
        if (needsNewTarget) {
            // Up to 4 blocks away sideways, and 1 up or down
            int offsetX = random.nextInt(9) - 4;
            int offsetY = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(9) - 4;
            flightTarget =
                    BlockPos.containing(getX() + offsetX, getY() + offsetY, getZ() + offsetZ);
        }

        Vec3 towardTarget = Vec3.atCenterOf(flightTarget).subtract(position()).normalize();
        Vec3 desiredVelocity = towardTarget.scale(CRUISE_SPEED);
        Vec3 velocity = getDeltaMovement();
        Vec3 steering = desiredVelocity.subtract(velocity).scale(STEERING_STRENGTH);
        setDeltaMovement(velocity.add(steering));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Always hover
        controllers.add(new AnimationController<>("movement", /* transitionTicks= */ 0,
                state -> state.setAndContinue(HOVER)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
