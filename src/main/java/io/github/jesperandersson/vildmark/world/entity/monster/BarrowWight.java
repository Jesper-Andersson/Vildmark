package io.github.jesperandersson.vildmark.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.behaviour.base.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.base.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtil;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * An undead guardian of burial mounds. Its AI runs on a SmartBrainLib brain: sensors fill the
 * brain's memories (nearby players, who hurt it), and behaviours act on them. Always-running
 * behaviours tick every tick, idle behaviours run without a target, and fighting behaviours run
 * once it has one.
 */
public class BarrowWight extends Monster implements GeoEntity, SmartBrainOwner<BarrowWight> {
    // Only looks at players this close, in blocks
    private static final double LOOK_AT_PLAYER_RANGE = 8;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BarrowWight(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void registerGoals() {
        // AI runs on the SmartBrainLib brain instead of vanilla goals
    }

    /*
    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }
    */

    @Override
    public List<? extends ExtendedSensor<?>> getSensors(BarrowWight owner) {
        return List.of(
                new NearbyPlayersSensor<>(),
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>());
    }

    @Override
    public List<? extends BehaviorControl<?>> getAlwaysRunningBehaviours(BarrowWight owner) {
        return List.of(
                new FloatToSurfaceOfFluid<>(),
                new LookAtTarget<>(),
                new MoveToWalkTarget<>());
    }

    @Override
    public List<? extends BehaviorControl<?>> getIdleBehaviours(BarrowWight owner) {
        return List.of(
                new FirstApplicableBehaviour<>(
                        // Hunts visible players and retaliates against anything that hurts it
                        new TargetOrRetaliate<BarrowWight>()
                                .useMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                                .canRetaliateAgainst(this::canTarget),
                        new SetPlayerLookTarget<BarrowWight>()
                                .canLookAt(BarrowWight::canSeeNearby),
                        new SetRandomLookTarget<>()),
                new OneRandomBehaviour<BarrowWight>(
                        new SetRandomWalkTarget<>()));
                        //new Idle<>().runForBetween(30, 60)));
    }

    @Override
    public List<? extends BehaviorControl<?>> getFightingBehaviours(BarrowWight owner) {
        return List.of(
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<>(),
                // No attack animation yet; set the delay to its hit frame once one is added
                new AnimatableMeleeAttack<>(0));
    }

    private static boolean canSeeNearby(BarrowWight self, Player player) {
        // Compares squared distances, which avoids a square root
        double maxDistanceSquared = LOOK_AT_PLAYER_RANGE * LOOK_AT_PLAYER_RANGE;
        boolean isNearby = self.distanceToSqr(player) <= maxDistanceSquared;
        return self.getSensing().hasLineOfSight(player) && isNearby;
    }

    // Any player who isn't in creative or spectator mode, or whatever hurt it last
    private boolean canTarget(LivingEntity entity) {
        boolean isInvulnerablePlayer =
                entity instanceof Player player && player.getAbilities().invulnerable;
        if (!entity.isAlive() || isInvulnerablePlayer) {
            return false;
        }
        LivingEntity lastAttacker = BrainUtil.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
        return entity instanceof Player || entity == lastAttacker;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Floats above the ground, so no footsteps
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
