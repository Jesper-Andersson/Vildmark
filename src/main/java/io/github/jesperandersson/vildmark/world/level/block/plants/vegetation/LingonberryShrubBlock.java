package io.github.jesperandersson.vildmark.world.level.block.plants.vegetation;

import io.github.jesperandersson.vildmark.world.item.VItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Lingonberry shrub: grows through ages 0-4 and can be picked from age 3, like a vanilla sweet
 * berry bush. Unlike {@link ShrubClass}, bone meal grows it.
 */
public class LingonberryShrubBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<LingonberryShrubBlock> CODEC =
            simpleCodec(LingonberryShrubBlock::new);
    public static final int MAX_AGE = 4;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    // Berries can be picked from this age on
    private static final int MIN_PICKABLE_AGE = 3;
    // Picking resets the shrub to this age, so it doesn't have to regrow from a sapling
    private static final int AGE_AFTER_PICKING = 1;
    // Same light level vanilla crops need to grow
    private static final int MIN_LIGHT_TO_GROW = 9;
    private static final VoxelShape SAPLING_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public LingonberryShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    public ItemStack getCloneItemStack(
            LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(VItems.LINGONBERRY_SHRUB_ITEM.get());
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        if (age == 0) {
            return SAPLING_SHAPE;
        }
        if (age < MAX_AGE) {
            return MID_GROWTH_SHAPE;
        }
        return super.getShape(state, level, pos, context);
    }

    // Only growing shrubs need random ticks
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    // Grows one age at a time in enough light, on average every fifth random tick
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        boolean hasEnoughLight = level.getRawBrightness(pos.above(), 0) >= MIN_LIGHT_TO_GROW;
        if (age >= MAX_AGE || !hasEnoughLight) {
            return;
        }
        // NeoForge hook that lets other mods speed up, slow down or block crop growth
        boolean defaultGrowChance = random.nextInt(5) == 0;
        if (!CommonHooks.canCropGrow(level, pos, state, defaultGrowChance)) {
            return;
        }

        BlockState grownState = state.setValue(AGE, age + 1);
        level.setBlock(pos, grownState, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(grownState));
        CommonHooks.fireCropGrowPost(level, pos, state);
    }

    @Override
    public InteractionResult useItemOn(
            ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        // Let bone meal fall through to performBonemeal while the shrub can still grow
        boolean isFullyGrown = state.getValue(AGE) == MAX_AGE;
        if (!isFullyGrown && heldItem.is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int age = state.getValue(AGE);
        if (age < MIN_PICKABLE_AGE) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }

        // 1-2 berries, plus one more from a fully grown shrub
        boolean isFullyGrown = age == MAX_AGE;
        int berryCount = 1 + level.getRandom().nextInt(2) + (isFullyGrown ? 1 : 0);
        popResource(level, pos, new ItemStack(VItems.LINGONBERRIES_ITEM.get(), berryCount));
        float pitch = 0.8F + level.getRandom().nextFloat() * 0.4F;
        // A null player means everyone nearby hears it, including the player who picked
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                /* volume= */ 1.0F, pitch);

        BlockState pickedState = state.setValue(AGE, AGE_AFTER_PICKING);
        level.setBlock(pos, pickedState, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, pickedState));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        }
    }
}
