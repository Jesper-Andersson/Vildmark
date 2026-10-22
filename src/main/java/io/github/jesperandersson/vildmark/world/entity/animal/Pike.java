package io.github.jesperandersson.vildmark.world.entity.animal;

import io.github.jesperandersson.vildmark.world.item.VItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

// Solitary predator (not a schooling fish) that hunts roach
public class Pike extends AbstractFish implements GeoEntity {
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.pike.swim");
    private static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.pike.flop");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Pike(EntityType<? extends Pike> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractFish.createAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(
                this, /* speedModifier= */ 1.2, /* followingTargetEvenIfNotSeen= */ true));
        // On average only one in this many ticks looks for prey, so a pike doesn't wipe out a
        // whole school of roach at once
        int preySearchInterval = 150;
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Roach.class,
                preySearchInterval, /* mustSee= */ true, /* mustReach= */ false,
                /* selector= */ null));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(VItems.PIKE_BUCKET.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("swim", /* transitionTicks= */ 5,
                state -> state.setAndContinue(isInWater() ? SWIM : FLOP)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
