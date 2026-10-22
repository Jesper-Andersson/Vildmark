package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;

/**
 * Chantarelle mushroom. Slowly spreads to nearby ground like vanilla mushrooms, and bone meal grows
 * a patch of chantarelles around it.
 */
public class ChantarelleBlock extends MushroomBlock implements BonemealableBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
    // On average one in this many random ticks tries to spread
    private static final int SPREAD_CHANCE = 25;
    // No spreading once this many chantarelles grow within the spread area
    private static final int MAX_NEARBY = 5;
    // Steps of the random walk that picks where to spread to
    private static final int SPREAD_STEPS = 4;
    // Same chance as vanilla mushrooms
    private static final double BONEMEAL_SUCCESS_CHANCE = 0.4;

    private final ResourceKey<ConfiguredFeature<?, ?>> feature;
    // Spreads the feature into a patch. Patches are placement modifiers since 26.1, so they live
    // on a placed feature
    private final ResourceKey<PlacedFeature> bonemealPatch;

    public ChantarelleBlock(
            BlockBehaviour.Properties properties, ResourceKey<ConfiguredFeature<?, ?>> feature,
            ResourceKey<PlacedFeature> bonemealPatch) {
        super(feature, properties);
        this.feature = feature;
        this.bonemealPatch = bonemealPatch;
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SPREAD_CHANCE) != 0) {
            return;
        }

        // Too crowded: count chantarelles within 4 blocks sideways and 1 up or down
        int nearbyCount = 0;
        Iterable<BlockPos> spreadArea =
                BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4));
        for (BlockPos nearbyPos : spreadArea) {
            if (level.getBlockState(nearbyPos).is(this)) {
                nearbyCount++;
                if (nearbyCount >= MAX_NEARBY) {
                    return;
                }
            }
        }

        // Random walk: each step moves to a random neighbor if a chantarelle could grow there
        BlockPos walkPos = pos;
        BlockPos candidatePos = randomNeighbor(walkPos, random);
        for (int step = 0; step < SPREAD_STEPS; step++) {
            if (level.isEmptyBlock(candidatePos) && state.canSurvive(level, candidatePos)) {
                walkPos = candidatePos;
            }
            candidatePos = randomNeighbor(walkPos, random);
        }

        if (level.isEmptyBlock(candidatePos) && state.canSurvive(level, candidatePos)) {
            level.setBlock(candidatePos, state, Block.UPDATE_CLIENTS);
        }
    }

    // Up to 1 block away on each axis. Vertically, -1 and +1 are half as likely as staying level
    private static BlockPos randomNeighbor(BlockPos pos, RandomSource random) {
        int offsetX = random.nextInt(3) - 1;
        int offsetY = random.nextInt(2) - random.nextInt(2);
        int offsetZ = random.nextInt(3) - 1;
        return pos.offset(offsetX, offsetY, offsetZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos groundPos) {
        return ground.isSolidRender();
    }

    // Unlike vanilla mushrooms, grows in any light, but only on dirt, grass or podzol
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState ground = level.getBlockState(pos.below());
        return ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.PODZOL);
    }

    /**
     * Replaces this chantarelle with a patch of chantarelles. The chantarelle is put back if the
     * patch can't be placed.
     *
     * @return whether the patch was placed
     */
    @Override
    public boolean growMushroom(
            ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Holder<ConfiguredFeature<?, ?>> ownFeature = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .getOrThrow(this.feature);
        // Lets other mods cancel the growth or swap in a different feature
        BlockGrowFeatureEvent event =
                EventHooks.fireBlockGrowFeature(level, random, pos, ownFeature);
        if (event.isCanceled() || event.getFeature() == null) {
            return false;
        }

        level.removeBlock(pos, false);
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        boolean isPlaced;
        if (event.getFeature() == ownFeature) {
            PlacedFeature patch = level.registryAccess()
                    .lookupOrThrow(Registries.PLACED_FEATURE)
                    .getOrThrow(this.bonemealPatch)
                    .value();
            isPlaced = patch.place(level, generator, random, pos);
        } else {
            isPlaced = event.getFeature().value().place(level, generator, random, pos);
        }
        if (!isPlaced) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        return isPlaced;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < BONEMEAL_SUCCESS_CHANCE;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.growMushroom(level, pos, state, random);
    }
}
