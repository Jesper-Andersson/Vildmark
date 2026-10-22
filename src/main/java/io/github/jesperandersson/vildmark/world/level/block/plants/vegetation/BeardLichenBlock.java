package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * Beard lichen (Usnea) hanging from pine branches: hangs below pine leaves or logs, or beside a
 * pine trunk, in strands of at most {@link #MAX_LENGTH} blocks. The lowest block of a strand is the
 * tip. {@link #FACING} points toward the support: {@code up} hangs as a cross under leaves or a
 * log, a horizontal facing lies flat against a pine trunk.
 */
public class BeardLichenBlock extends Block {
    public static final MapCodec<BeardLichenBlock> CODEC = simpleCodec(BeardLichenBlock::new);
    public static final EnumProperty<Direction> FACING = EnumProperty.create(
            "facing", Direction.class, direction -> direction != Direction.DOWN);
    public static final BooleanProperty TIP = BooleanProperty.create("tip");
    public static final int MAX_LENGTH = 2;
    private static final TagKey<Block> PINE_LOGS = TagKey.create(
            Registries.BLOCK, Identifier.fromNamespaceAndPath("vildmark", "pine_logs"));
    private static final VoxelShape HANGING_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = Map.of(
            Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0),
            Direction.SOUTH, Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0),
            Direction.WEST, Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0),
            Direction.EAST, Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

    public BeardLichenBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(TIP, true));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TIP);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? HANGING_SHAPE : WALL_SHAPES.get(facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean isLowest = !level.getBlockState(pos.below()).is(this);
        BlockState base = defaultBlockState().setValue(TIP, isLowest);

        // Continue an existing strand with its facing
        BlockState above = level.getBlockState(pos.above());
        if (above.is(this)) {
            BlockState continuedState = base.setValue(FACING, above.getValue(FACING));
            return continuedState.canSurvive(level, pos) ? continuedState : null;
        }

        // Otherwise attach toward the first support in the direction the player looks
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            BlockState facingState = base.setValue(FACING, direction);
            if (facingState.canSurvive(level, pos)) {
                return facingState;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Walk up the strand to its anchor; a strand longer than MAX_LENGTH has none
        Direction facing = state.getValue(FACING);
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int blocksChecked = 0; blocksChecked < MAX_LENGTH; blocksChecked++) {
            if (isAnchored(level, cursor, facing)) {
                return true;
            }
            cursor.move(Direction.UP);
            BlockState above = level.getBlockState(cursor);
            if (!above.is(this) || above.getValue(FACING) != facing) {
                return false;
            }
        }
        return false;
    }

    /**
     * Whether a lichen block at {@code pos} facing {@code facing} hangs from pine leaves or a pine
     * log above, or clings to a pine trunk on that side.
     */
    public static boolean isAnchored(LevelReader level, BlockPos pos, Direction facing) {
        BlockState support = level.getBlockState(pos.relative(facing));
        return facing == Direction.UP ? isSupport(support) : support.is(PINE_LOGS);
    }

    /** Whether lichen can hang below this block: pine leaves or a pine log. */
    public static boolean isSupport(BlockState state) {
        return state.is(VBlocks.PINE_LEAVES.get()) || state.is(PINE_LOGS);
    }

    @Override
    protected BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos,
            Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState,
            RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        // The block below changed, so this block may have become or stopped being the tip
        if (directionToNeighbor == Direction.DOWN) {
            return state.setValue(TIP, !neighborState.is(this));
        }
        return state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
