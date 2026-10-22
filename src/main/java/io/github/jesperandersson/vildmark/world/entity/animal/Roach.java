package io.github.jesperandersson.vildmark.world.entity.animal;

import io.github.jesperandersson.vildmark.world.item.VItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

// Based on the vanilla Salmon
public class Roach extends AbstractSchoolingFish implements GeoEntity {
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.roach.swim");
    private static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.roach.flop");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Roach(EntityType<? extends Roach> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public int getMaxSchoolSize() {
        // Roach shoals are big
        return 8;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(VItems.ROACH_BUCKET.get());
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
        controllers.add(new AnimationController<>(this, "swim", 5, state -> state.setAndContinue(isInWater() ? SWIM : FLOP)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
