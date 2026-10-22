package com.github.Jesper_Andersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.neoforged.neoforge.common.util.TriState;

import javax.annotation.Nullable;

public class PineConkMushroomBlock extends HorizontalDirectionalBlock implements BonemealableBlock {
    public static final int MAX_AGE = 1;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;

    protected static final VoxelShape[] EAST_AABB;
    protected static final VoxelShape[] WEST_AABB;
    protected static final VoxelShape[] NORTH_AABB;
    protected static final VoxelShape[] SOUTH_AABB;


    public static final MapCodec<PineConkMushroomBlock> CODEC = simpleCodec(PineConkMushroomBlock::new);

    public PineConkMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos.relative(state.getValue(FACING)));
        TriState soilDecision = blockstate.canSustainPlant(level, pos.relative(state.getValue(FACING)), state.getValue(FACING).getOpposite(), state);
        return !soilDecision.isDefault() ? soilDecision.isTrue() : blockstate.is(BlockTags.LOGS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int i = state.getValue(AGE);
        switch (state.getValue(FACING)) {
            case SOUTH:
                return SOUTH_AABB[i];
            case NORTH:
            default:
                return NORTH_AABB[i];
            case WEST:
                return WEST_AABB[i];
            case EAST:
                return EAST_AABB[i];
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();

        for(Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockstate = blockstate.setValue(FACING, direction);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder){
        pBuilder.add(AGE);
        pBuilder.add(FACING);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(AGE) < 1;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        serverLevel.setBlock(blockPos, blockState.setValue(AGE, blockState.getValue(AGE) + 1), 2);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    static {
        // Matches pine_conk_small.json and pine_conk.json, rotated from the north model the same way the blockstate does
        NORTH_AABB = new VoxelShape[]{
                Block.box(4.0F, 8.0F, 0.0F, 12.0F, 10.0F, 5.0F),
                Shapes.or(Block.box(3.0F, 7.0F, 0.0F, 13.0F, 9.0F, 6.0F), Block.box(4.0F, 9.0F, 0.0F, 12.0F, 10.0F, 5.0F))};
        EAST_AABB = new VoxelShape[]{
                Block.box(11.0F, 8.0F, 4.0F, 16.0F, 10.0F, 12.0F),
                Shapes.or(Block.box(10.0F, 7.0F, 3.0F, 16.0F, 9.0F, 13.0F), Block.box(11.0F, 9.0F, 4.0F, 16.0F, 10.0F, 12.0F))};
        SOUTH_AABB = new VoxelShape[]{
                Block.box(4.0F, 8.0F, 11.0F, 12.0F, 10.0F, 16.0F),
                Shapes.or(Block.box(3.0F, 7.0F, 10.0F, 13.0F, 9.0F, 16.0F), Block.box(4.0F, 9.0F, 11.0F, 12.0F, 10.0F, 16.0F))};
        WEST_AABB = new VoxelShape[]{
                Block.box(0.0F, 8.0F, 4.0F, 5.0F, 10.0F, 12.0F),
                Shapes.or(Block.box(0.0F, 7.0F, 3.0F, 6.0F, 9.0F, 13.0F), Block.box(0.0F, 9.0F, 4.0F, 5.0F, 10.0F, 12.0F))};
    }
}