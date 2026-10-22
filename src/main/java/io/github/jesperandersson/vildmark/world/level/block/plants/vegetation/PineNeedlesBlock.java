package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

/**
 * Backport of 1.21.5's leaf litter: a flat ground cover with 1-4 segments, placed like pink petals.
 */
public class PineNeedlesBlock extends BushBlock {
    public static final MapCodec<PineNeedlesBlock> CODEC = simpleCodec(PineNeedlesBlock::new);
    public static final int MAX_SEGMENTS = 4;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty AMOUNT = BlockStateProperties.FLOWER_AMOUNT;
    private static final BiFunction<Direction, Integer, VoxelShape> SHAPE_BY_PROPERTIES = Util.memoize(
            (facing, amount) -> {
                VoxelShape[] segments = new VoxelShape[]{
                        Block.box(8.0, 0.0, 8.0, 16.0, 1.0, 16.0),
                        Block.box(8.0, 0.0, 0.0, 16.0, 1.0, 8.0),
                        Block.box(0.0, 0.0, 0.0, 8.0, 1.0, 8.0),
                        Block.box(0.0, 0.0, 8.0, 8.0, 1.0, 16.0)
                };
                VoxelShape shape = Shapes.empty();

                for (int i = 0; i < amount; i++) {
                    shape = Shapes.or(shape, segments[Math.floorMod(i - facing.get2DDataValue(), 4)]);
                }

                return shape.singleEncompassing();
            }
    );

    public PineNeedlesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AMOUNT, 1));
    }

    @Override
    public MapCodec<PineNeedlesBlock> codec() {
        return CODEC;
    }

    // Like leaf litter, needles can lie on any solid top face, not just dirt
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(AMOUNT) < MAX_SEGMENTS
                || super.canBeReplaced(state, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_PROPERTIES.apply(state.getValue(FACING), state.getValue(AMOUNT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        return existing.is(this)
                ? existing.setValue(AMOUNT, Math.min(MAX_SEGMENTS, existing.getValue(AMOUNT) + 1))
                : this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AMOUNT);
    }
}
