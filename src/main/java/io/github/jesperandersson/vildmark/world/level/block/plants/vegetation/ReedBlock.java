package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Reed (vass): a two block tall plant that grows in shallow water or on wet ground at the shore.
 * Both halves can be waterlogged.
 */
public class ReedBlock extends DoublePlantBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<ReedBlock> CODEC = simpleCodec(ReedBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ReedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos groundPos) {
        boolean isDirtGround = ground.is(Blocks.DIRT)
                || ground.is(Blocks.GRASS_BLOCK)
                || ground.is(Blocks.PODZOL)
                || ground.is(Blocks.COARSE_DIRT);
        boolean isShoreGround = ground.is(Blocks.MUD)
                || ground.is(Blocks.SAND)
                || ground.is(VBlocks.PEAT.get())
                || ground.is(Blocks.MOSS_BLOCK);
        return isDirtGround || isShoreGround;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.getValue(WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(state);
    }

    @Override
    @javax.annotation.Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Null when the upper half has no room
        BlockState placedState = super.getStateForPlacement(context);
        if (placedState == null) {
            return null;
        }
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return placedState.setValue(WATERLOGGED, fluid.is(FluidTags.WATER) && fluid.isSource());
    }

    @Override
    protected BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos,
            Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState,
            RandomSource random) {
        boolean isWaterlogged = state.getValue(WATERLOGGED);
        if (isWaterlogged) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        BlockState updatedState = super.updateShape(state, level, tickAccess, pos,
                directionToNeighbor, neighborPos, neighborState, random);
        // When the other half breaks, leave water behind instead of air
        if (updatedState.isAir() && isWaterlogged) {
            return Fluids.WATER.defaultFluidState().createLegacyBlock();
        }
        return updatedState;
    }
}
