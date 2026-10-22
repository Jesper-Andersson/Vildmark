package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.item.VItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Fully grown stinging nettle. Has no item of its own, so pick-block gives the young nettle. */
public class TallStingingNettleBlock extends DoublePlantBlock {
    public static final MapCodec<TallStingingNettleBlock> CODEC =
            simpleCodec(TallStingingNettleBlock::new);

    public TallStingingNettleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(
            BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        StingingNettleBlock.sting(level, entity);
        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }

    @Override
    public ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(VItems.STINGING_NETTLE_ITEM.get());
    }
}
