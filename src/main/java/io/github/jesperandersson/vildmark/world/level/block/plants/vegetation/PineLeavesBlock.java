package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IShearable;

import com.mojang.serialization.MapCodec;

import java.util.OptionalInt;

/**
 * Pine leaves, based on vanilla LeavesBlock. Each leaf stores its {@link #DISTANCE} to the nearest
 * log; leaves {@link #DECAY_DISTANCE} or more blocks away from any log decay unless a player placed
 * them.
 */
public class PineLeavesBlock extends TintedParticleLeavesBlock
        implements SimpleWaterloggedBlock, IShearable {
    public static final MapCodec<PineLeavesBlock> CODEC = simpleCodec(PineLeavesBlock::new);
    public static final int DECAY_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    // Placed by a player, so it never decays
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // Ticks until the distance is recalculated after a neighbor changes
    private static final int TICK_DELAY = 1;
    // Same falling leaf particle chance as vanilla oak leaves. Vanilla's LeavesBlock.animateTick
    // spawns the falling leaves, tinted by TintedParticleLeavesBlock, and drips rain water
    private static final float LEAF_PARTICLE_CHANCE = 0.01F;

    public PineLeavesBlock(BlockBehaviour.Properties properties) {
        super(LEAF_PARTICLE_CHANCE, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, DECAY_DISTANCE)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return CODEC;
    }

    // Nothing can be attached to leaves, such as torches or buttons
    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    // Only leaves that are about to decay need random ticks
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return decaying(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected boolean decaying(BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(DISTANCE) == DECAY_DISTANCE;
    }

    // Scheduled by updateShape when a neighbor changes
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, withUpdatedDistance(state, level, pos), Block.UPDATE_ALL);
    }

    // Called when a neighbor changes. Recalculating the distance is left to a scheduled tick, so a
    // log being broken doesn't update every leaf of the tree in the same tick
    @Override
    protected BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos,
            Direction directionToNeighbor, BlockPos neighborPos, BlockState neighborState,
            RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        int distanceThroughNeighbor = distanceToLog(neighborState) + 1;
        boolean isNextToLog = distanceThroughNeighbor == 1;
        if (!isNextToLog || state.getValue(DISTANCE) != distanceThroughNeighbor) {
            tickAccess.scheduleTick(pos, this, TICK_DELAY);
        }

        return state;
    }

    // The shortest distance to a log through any of the six neighbors
    private static BlockState withUpdatedDistance(
            BlockState state, LevelAccessor level, BlockPos pos) {
        int distance = DECAY_DISTANCE;
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            neighborPos.setWithOffset(pos, direction);
            distance = Math.min(distance, distanceToLog(level.getBlockState(neighborPos)) + 1);
            if (distance == 1) {
                // Next to a log, can't get any closer
                break;
            }
        }

        return state.setValue(DISTANCE, distance);
    }

    private static int distanceToLog(BlockState neighbor) {
        return getOptionalDistanceAt(neighbor).orElse(DECAY_DISTANCE);
    }

    /**
     * Returns how far the given block is from a log: 0 for a log, the stored distance for leaves,
     * and empty for anything else.
     */
    public static OptionalInt getOptionalDistanceAt(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return OptionalInt.of(0);
        }
        if (state.hasProperty(DISTANCE)) {
            return OptionalInt.of(state.getValue(DISTANCE));
        }
        return OptionalInt.empty();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.getValue(WATERLOGGED)) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
    }

    // Placed by a player: never decays, and fills with water when placed in it
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidAtPos = context.getLevel().getFluidState(context.getClickedPos());
        BlockState placedState = this.defaultBlockState()
                .setValue(PERSISTENT, true)
                .setValue(WATERLOGGED, fluidAtPos.getType() == Fluids.WATER);
        return withUpdatedDistance(placedState, context.getLevel(), context.getClickedPos());
    }
}
