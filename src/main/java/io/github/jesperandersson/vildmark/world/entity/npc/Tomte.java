package io.github.jesperandersson.vildmark.world.entity.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

/**
 * Tomte: a small household spirit from Nordic folklore. Behaves like a wandering trader without
 * trading or the invisibility potion and milk goals. Trader llamas only come from
 * WanderingTraderSpawner, which never spawns this entity.
 */
public class Tomte extends WanderingTrader implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Tomte(EntityType<? extends WanderingTrader> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    protected void registerGoals() {
        // Same as WanderingTrader minus the UseItemGoals, trading goals and WanderToPositionGoal
        // (only used by the spawner). A lower number is a higher priority
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Flees from each mob once it comes within the given distance in blocks
        this.goalSelector.addGoal(1, fleeFrom(Zombie.class, 8.0F));
        this.goalSelector.addGoal(1, fleeFrom(Evoker.class, 12.0F));
        this.goalSelector.addGoal(1, fleeFrom(Vindicator.class, 8.0F));
        this.goalSelector.addGoal(1, fleeFrom(Vex.class, 8.0F));
        this.goalSelector.addGoal(1, fleeFrom(Pillager.class, 15.0F));
        this.goalSelector.addGoal(1, fleeFrom(Illusioner.class, 12.0F));
        this.goalSelector.addGoal(1, fleeFrom(Zoglin.class, 10.0F));
        this.goalSelector.addGoal(1, new PanicGoal(this, /* speedModifier= */ 0.5));
        double strollSpeed = 0.35;
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, strollSpeed));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, strollSpeed));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    private <T extends LivingEntity> AvoidEntityGoal<T> fleeFrom(
            Class<T> mobClass, float distance) {
        return new AvoidEntityGoal<>(this, mobClass, distance,
                /* walkSpeedModifier= */ 0.5, /* sprintSpeedModifier= */ 0.5);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // WanderingTrader opens the trade screen here. Name tags and leads are handled before this
        // is called
        return InteractionResult.PASS;
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        // No trading
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // All motion is procedural, see TomteRenderer
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
