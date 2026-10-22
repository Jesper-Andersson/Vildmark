package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

/**
 * Pine needle ground cover with 1-4 segments, placed like vanilla leaf litter and pink petals.
 */
public class PineNeedlesBlock extends VegetationBlock implements SegmentableBlock {
    public static final MapCodec<PineNeedlesBlock> CODEC = simpleCodec(PineNeedlesBlock::new);
    public static final int MAX_SEGMENTS = 4;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty AMOUNT = BlockStateProperties.FLOWER_AMOUNT;
    // The four quarters of the block, in the order segments are added when facing south
    private static final VoxelShape[] QUARTERS = {
            Block.box(8.0, 0.0, 8.0, 16.0, 1.0, 16.0),
            Block.box(8.0, 0.0, 0.0, 16.0, 1.0, 8.0),
            Block.box(0.0, 0.0, 0.0, 8.0, 1.0, 8.0),
            Block.box(0.0, 0.0, 8.0, 8.0, 1.0, 16.0)};
    // Cached, since there are only 4 facings times 4 amounts
    private static final BiFunction<Direction, Integer, VoxelShape> SHAPE_BY_PROPERTIES =
            Util.memoize(PineNeedlesBlock::computeShape);

    public PineNeedlesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AMOUNT, 1));
    }

    // One quarter per segment, turning from a starting quarter that depends on the facing. The
    // bounding box of the quarters is used, so 3 segments get a full square shape
    private static VoxelShape computeShape(Direction facing, int amount) {
        VoxelShape shape = Shapes.empty();
        for (int segment = 0; segment < amount; segment++) {
            int quarter = Math.floorMod(segment - facing.get2DDataValue(), QUARTERS.length);
            shape = Shapes.or(shape, QUARTERS[quarter]);
        }
        return shape.singleEncompassing();
    }

    @Override
    public MapCodec<PineNeedlesBlock> codec() {
        return CODEC;
    }

    // Keeps the pink petal property from before vanilla had leaf litter, so existing worlds and the
    // blockstate file still match. Vanilla's segmented drops read the amount from here
    @Override
    public IntegerProperty getSegmentAmountProperty() {
        return AMOUNT;
    }

    // Like leaf litter, needles can lie on any solid top face, not just dirt
    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos groundPos) {
        return ground.isFaceSturdy(level, groundPos, Direction.UP);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Placing more needles on needles adds a segment, unless the player sneaks
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        boolean isAddingSegment = !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < MAX_SEGMENTS;
        return isAddingSegment || super.canBeReplaced(state, context);
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_PROPERTIES.apply(state.getValue(FACING), state.getValue(AMOUNT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            int amount = Math.min(MAX_SEGMENTS, existing.getValue(AMOUNT) + 1);
            return existing.setValue(AMOUNT, amount);
        }
        // Faces the player
        Direction towardPlayer = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, towardPlayer);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AMOUNT);
    }
}
