package com.github.Jesper_Andersson.nordic.world.level.block;

import com.github.Jesper_Andersson.nordic.world.level.block.plants.tree.grower.PineTreeGrower;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.github.Jesper_Andersson.nordic.Nordic.BLOCKS;
import static com.github.Jesper_Andersson.nordic.Nordic.PATCH_CHANTARELLE_CONFIGURED;

public class NBlocks {
    // Berry shrubs and mushrooms
    public static final DeferredBlock<Block> LINGONBERRY_SHRUB = BLOCKS.register("lingonberry_shrub",
            () -> new LingonberryShrubBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<Block> BLUEBERRY_SHRUB = BLOCKS.register("blueberry_shrub",
            () -> new BlueberryShrubBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<Block> CLOUDBERRY_SHRUB = BLOCKS.register("cloudberry_shrub",
            () -> new CloudberryShrubBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS)));

    public static final DeferredBlock<Block> CHANTARELLE = BLOCKS.register("chantarelle",
            () -> new ChantarelleBlock(
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()
                            .sound(SoundType.GRASS),
                    PATCH_CHANTARELLE_CONFIGURED));

    public static final DeferredBlock<Block> PINE_CONK = BLOCKS.register("pine_conk",
            () -> new PineConkMushroomBlock(
                    BlockBehaviour.Properties.of()
                            .randomTicks()
                            .sound(SoundType.WOOD)
            ));

    // Water plants
    public static final DeferredBlock<Block> REED = BLOCKS.register("reed",
            () -> new ReedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> CATTAIL = BLOCKS.register("cattail",
            () -> new CattailBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    // Pine tree
    public static final DeferredBlock<Block> PINE_SAPLING = BLOCKS.register("pine_sapling",
            () -> new SaplingBlock(
                    PineTreeGrower.PINE,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> PINE_LOG = BLOCKS.register("pine_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f)));

    // Worldgen-only bark stages up a pine trunk; no items, they drop a normal pine log
    public static final DeferredBlock<Block> PEELING_PINE_LOG = BLOCKS.register("peeling_pine_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f)));

    public static final DeferredBlock<Block> PEELED_PINE_LOG = BLOCKS.register("peeled_pine_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f)));

    public static final DeferredBlock<Block> PINE_PLANKS = BLOCKS.register("pine_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f)));

    public static final DeferredBlock<StairBlock> PINE_STAIRS = BLOCKS.register("pine_stairs",
            () -> new StairBlock(
                    PINE_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(PINE_PLANKS.get())));

    public static final DeferredBlock<SlabBlock> PINE_SLAB = BLOCKS.register("pine_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(PINE_PLANKS.get())));

    public static final DeferredBlock<FenceBlock> SLANTED_FENCE = BLOCKS.register("slantedfence",
            () -> new SlantedFenceBlock(BlockBehaviour.Properties.ofFullCopy(PINE_PLANKS.get())
                    .forceSolidOn()
                    .strength(2f, 3f)));

    public static final DeferredBlock<Block> PINE_LEAVES = BLOCKS.register("pine_leaves",
            () -> new PineLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .randomTicks()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

    public static final DeferredBlock<Block> BEARD_LICHEN = BLOCKS.register("beard_lichen",
            () -> new BeardLichenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.HANGING_ROOTS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> PINE_NEEDLES = BLOCKS.register("pine_needles",
            () -> new PineNeedlesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    // Bog
    public static final DeferredBlock<Block> PEAT = BLOCKS.register("peat",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(SoundType.MUD)));

    public static final DeferredBlock<Block> PEAT_IRON_ORE = BLOCKS.register("peat_iron_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6f)
                    .sound(SoundType.MUD)));

    public static final DeferredBlock<Block> COTTON_GRASS = BLOCKS.register("cotton_grass",
            () -> new BogPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> SUNDEW = BLOCKS.register("sundew",
            () -> new SundewBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    // Ground vegetation
    public static final DeferredBlock<Block> STINGING_NETTLE = BLOCKS.register("stinging_nettle",
            () -> new StingingNettleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .randomTicks()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> TALL_STINGING_NETTLE = BLOCKS.register("tall_stinging_nettle",
            () -> new TallStingingNettleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> SNAKE_FERN = BLOCKS.register("snakefern",
            () -> new SnakeFernBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> HEATHER = BLOCKS.register("heather",
            () -> new HeatherBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static void register(){}
}
