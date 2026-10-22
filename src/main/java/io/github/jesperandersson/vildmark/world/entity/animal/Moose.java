package io.github.jesperandersson.vildmark.world.entity.animal;

import io.github.jesperandersson.vildmark.world.entity.VEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

// Placeholder: uses vanilla cow behaviour (goals, breeding, milking, sounds) until custom AI is written
public class Moose extends Cow implements GeoEntity {
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Moose(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Nullable
    @Override
    public Moose getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return VEntities.MOOSE.get().create(level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // The model only has a "walk" animation, so GeckoLib's default walk/idle controller can't be used
        controllers.add(new AnimationController<>(this, "movement", 0,
                state -> state.isMoving() ? state.setAndContinue(WALK) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
