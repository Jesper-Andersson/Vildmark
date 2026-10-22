package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import net.minecraft.world.level.ScheduledTickAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.TriState;

/**
 * Pine conk: a bracket fungus on the side of a log. {@link #FACING} points toward the log it grows
 * on, and bone meal grows it from small (age 0) to mature (age 1).
 */
public class PineConkMushroomBlock extends HorizontalDirectionalBlock implements BonemealableBlock {
    public static final MapCodec<PineConkMushroomBlock> CODEC =
            simpleCodec(PineConkMushroomBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;

    // Shapes for each facing, indexed by age. They match pine_conk_small.json and pine_conk.json,
    // rotated from the north model the same way the blockstate does
    protected static final VoxelShape[] NORTH_SHAPES = {
            Block.box(4.0F, 8.0F, 0.0F, 12.0F, 10.0F, 5.0F),
            Shapes.or(
                    Block.box(3.0F, 7.0F, 0.0F, 13.0F, 9.0F, 6.0F),
                    Block.box(4.0F, 9.0F, 0.0F, 12.0F, 10.0F, 5.0F))};
    protected static final VoxelShape[] EAST_SHAPES = {
            Block.box(11.0F, 8.0F, 4.0F, 16.0F, 10.0F, 12.0F),
            Shapes.or(
                    Block.box(10.0F, 7.0F, 3.0F, 16.0F, 9.0F, 13.0F),
                    Block.box(11.0F, 9.0F, 4.0F, 16.0F, 10.0F, 12.0F))};
    protected static final VoxelShape[] SOUTH_SHAPES = {
            Block.box(4.0F, 8.0F, 11.0F, 12.0F, 10.0F, 16.0F),
            Shapes.or(
                    Block.box(3.0F, 7.0F, 10.0F, 13.0F, 9.0F, 16.0F),
                    Block.box(4.0F, 9.0F, 11.0F, 12.0F, 10.0F, 16.0F))};
    protected static final VoxelShape[] WEST_SHAPES = {
            Block.box(0.0F, 8.0F, 4.0F, 5.0F, 10.0F, 12.0F),
            Shapes.or(
                    Block.box(0.0F, 7.0F, 3.0F, 6.0F, 9.0F, 13.0F),
                    Block.box(0.0F, 9.0F, 4.0F, 5.0F, 10.0F, 12.0F))};

    public PineConkMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    // Survives on any log, or on a block that a mod says can sustain it
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction towardLog = state.getValue(FACING);
        BlockPos supportPos = pos.relative(towardLog);
        BlockState support = level.getBlockState(supportPos);
        Direction towardConk = towardLog.getOpposite();
        TriState canSustain = support.canSustainPlant(level, supportPos, towardConk, state);
        return canSustain.toBoolean(support.is(BlockTags.LOGS));
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPES[age];
            case WEST -> WEST_SHAPES[age];
            case EAST -> EAST_SHAPES[age];
            default -> NORTH_SHAPES[age];
        };
    }

    // Faces the first log in the direction the player looks, or can't be placed if there's none
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (Direction direction : context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) {
                continue;
            }
            BlockState facingState = this.defaultBlockState().setValue(FACING, direction);
            if (facingState.canSurvive(level, pos)) {
                return facingState;
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        builder.add(FACING);
    }

    // Breaks when the log it grows on is removed
    @Override
    protected BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos,
            Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState,
            RandomSource random) {
        boolean isLogSide = directionToNeighbor == state.getValue(FACING);
        if (isLogSide && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, directionToNeighbor, neighborPos,
                neighborState, random);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
    }

    // Mobs don't try to walk through it
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
