package com.github.Jesper_Andersson.vildmark.world.level.block.plants.vegetation;

import com.github.Jesper_Andersson.vildmark.world.level.block.VBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * Beard lichen (Usnea) hanging from pine branches: hangs below pine leaves or logs, or beside a pine trunk, in strands
 * of at most {@link #MAX_LENGTH} blocks. The lowest block of a strand is the tip. {@link #FACING} points toward the
 * support: {@code up} hangs as a cross under leaves or a log, a horizontal facing lies flat against a pine trunk.
 */
public class BeardLichenBlock extends Block {
    public static final MapCodec<BeardLichenBlock> CODEC = simpleCodec(BeardLichenBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);
    public static final BooleanProperty TIP = BooleanProperty.create("tip");
    public static final int MAX_LENGTH = 2;
    private static final TagKey<Block> PINE_LOGS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("vildmark", "pine_logs"));
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = Map.of(
            Direction.NORTH, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D),
            Direction.SOUTH, Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D),
            Direction.WEST, Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D),
            Direction.EAST, Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D));

    public BeardLichenBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(TIP, true));
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? SHAPE : WALL_SHAPES.get(facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState base = defaultBlockState().setValue(TIP, !level.getBlockState(pos.below()).is(this));
        // Continue an existing strand with its facing
        BlockState above = level.getBlockState(pos.above());
        if (above.is(this)) {
            BlockState state = base.setValue(FACING, above.getValue(FACING));
            return state.canSurvive(level, pos) ? state : null;
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN) continue;
            BlockState state = base.setValue(FACING, direction);
            if (state.canSurvive(level, pos)) return state;
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Walk up the strand to its anchor; a strand longer than MAX_LENGTH has none
        Direction facing = state.getValue(FACING);
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 0; i < MAX_LENGTH; i++) {
            if (isAnchored(level, cursor, facing)) return true;
            cursor.move(Direction.UP);
            BlockState above = level.getBlockState(cursor);
            if (!above.is(this) || above.getValue(FACING) != facing) return false;
        }
        return false;
    }

    /** Whether a lichen block at {@code pos} facing {@code facing} hangs from pine leaves or a pine log above, or clings to a pine trunk on that side. */
    public static boolean isAnchored(LevelReader level, BlockPos pos, Direction facing) {
        BlockState support = level.getBlockState(pos.relative(facing));
        return facing == Direction.UP ? isSupport(support) : support.is(PINE_LOGS);
    }

    public static boolean isSupport(BlockState state) {
        return state.is(VBlocks.PINE_LEAVES.get()) || state.is(PINE_LOGS);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        if (direction == Direction.DOWN) return state.setValue(TIP, !neighborState.is(this));
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
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
