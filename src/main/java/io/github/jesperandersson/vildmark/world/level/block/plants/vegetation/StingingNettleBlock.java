package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Young stinging nettle. Grows into the two block tall nettle on random ticks or with bone meal.
 */
public class StingingNettleBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<StingingNettleBlock> CODEC =
            simpleCodec(StingingNettleBlock::new);
    // 3 seconds at 20 ticks per second
    private static final int POISON_DURATION_TICKS = 60;
    // On average one in this many random ticks grows the nettle
    private static final int GROW_CHANCE = 8;

    public StingingNettleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(
            BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        sting(level, entity);
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }

    /**
     * Poisons a living entity for 3 seconds. Only applied when the entity isn't already poisoned,
     * otherwise re-applying every tick would keep resetting the duration and poison would never
     * reach its damage tick.
     */
    static void sting(Level level, Entity entity) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!living.hasEffect(MobEffects.POISON)) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.POISON, POISON_DURATION_TICKS, /* amplifier= */ 0));
        }
    }

    @Override
    protected void randomTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROW_CHANCE) == 0 && level.isEmptyBlock(pos.above())) {
            grow(level, pos);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.isEmptyBlock(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos);
    }

    private static void grow(ServerLevel level, BlockPos pos) {
        BlockState tallNettle = VBlocks.TALL_STINGING_NETTLE.get().defaultBlockState();
        DoublePlantBlock.placeAt(level, tallNettle, pos, Block.UPDATE_CLIENTS);
    }
}
