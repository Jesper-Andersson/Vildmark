package com.github.Jesper_Andersson.nordic.world.entity.animal;

import com.github.Jesper_Andersson.nordic.core.particles.NParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WillOTheWisp extends Bat implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation HOVER = RawAnimation.begin().thenLoop("hover");

    private BlockPos target;

    public WillOTheWisp(EntityType<? extends Bat> entityType, Level level) {
        super(entityType, level);
        this.setResting(false);
        this.setNoGravity(true);
    }

    public static boolean checkWillOTheWispSpawnRules(EntityType<WillOTheWisp> willothewisp, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!level.canSeeSky(pos)) {
            return false;
        } else {
            int i = level.getMaxLocalRawBrightness(pos);
            int j = 7; //Chance to spawn

            return i <= random.nextInt(j) && level.canSeeSky(pos) && level.isEmptyBlock(pos);
        }
    }

    @Override
    public void aiStep(){
        if (this.level().isClientSide){
            if (random.nextInt(7) == 0) {
                this.level().addParticle(NParticleTypes.WISP_FLAME.get(), this.getRandomX(0.25F), this.getRandomY(), this.getRandomZ(0.25F), 0.0F, 0.0F, 0.0F);
            }
            if (random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getRandomX(0.5F), this.getRandomY(), this.getRandomZ(0.5F), 0.0F, 0.1F, 0.0F);
            }
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        // Replaces Bat's flight
        if (target == null || !level().isEmptyBlock(target) || random.nextInt(200) == 0 || target.closerToCenterThan(position(), 1.0)) {
            target = BlockPos.containing(getX() + random.nextInt(9) - 4, getY() + random.nextInt(3) - 1, getZ() + random.nextInt(9) - 4);
        }
        // Air drag (x0.91 per tick) holds it at about half the wanted speed, so 0.1 cruises at ~1 block per second
        Vec3 wanted = Vec3.atCenterOf(target).subtract(position()).normalize().scale(0.1);
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.add(wanted.subtract(motion).scale(0.1)));

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Always hover
        controllers.add(new AnimationController<>(this, "movement", 0, state -> state.setAndContinue(HOVER)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
