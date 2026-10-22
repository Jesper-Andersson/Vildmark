package com.github.Jesper_Andersson.vildmark.world.level.block.plants.vegetation;

import com.github.Jesper_Andersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Young stinging nettle; grows into the two block tall nettle on random ticks or with bone meal. */
public class StingingNettleBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<StingingNettleBlock> CODEC = simpleCodec(StingingNettleBlock::new);

    public StingingNettleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        sting(pLevel, pEntity);
        super.entityInside(pState, pLevel, pPos, pEntity);
    }

    /** Poison I for 3 seconds. Only applied when the entity isn't already poisoned, otherwise re-applying every
     *  tick would keep resetting the duration and poison would never reach its damage tick. */
    static void sting(Level level, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !living.hasEffect(MobEffects.POISON)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        }
    }

    @Override
    protected void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pRandom.nextInt(8) == 0 && pLevel.isEmptyBlock(pPos.above())) {
            grow(pLevel, pPos);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState) {
        return pLevel.isEmptyBlock(pPos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        grow(pLevel, pPos);
    }

    private static void grow(ServerLevel level, BlockPos pos) {
        DoublePlantBlock.placeAt(level, VBlocks.TALL_STINGING_NETTLE.get().defaultBlockState(), pos, 2);
    }
}
