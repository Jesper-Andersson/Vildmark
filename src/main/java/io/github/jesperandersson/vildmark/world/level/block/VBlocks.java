package io.github.jesperandersson.vildmark.world.level.block;

import io.github.jesperandersson.vildmark.world.level.block.plants.tree.grower.PineTreeGrower;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.*;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BeardLichenBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BlueberryShrubBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BogPlantBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.CattailBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.ChantarelleBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.CloudberryShrubBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.HeatherBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.LingonberryShrubBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineConkMushroomBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineLeavesBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineNeedlesBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.ReedBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.SnakeFernBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.StingingNettleBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.SundewBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.TallStingingNettleBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

import static io.github.jesperandersson.vildmark.Vildmark.BLOCKS;
import static io.github.jesperandersson.vildmark.Vildmark.PATCH_CHANTARELLE_CONFIGURED;

public class VBlocks {
    // Berry shrubs and mushrooms
    public static final DeferredBlock<Block> LINGONBERRY_SHRUB = BLOCKS.registerBlock("lingonberry_shrub",
            properties -> new LingonberryShrubBlock(properties),
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> BLUEBERRY_SHRUB = BLOCKS.registerBlock("blueberry_shrub",
            properties -> new BlueberryShrubBlock(properties),
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> CLOUDBERRY_SHRUB = BLOCKS.registerBlock("cloudberry_shrub",
            properties -> new CloudberryShrubBlock(properties),
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollission()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> CHANTARELLE = BLOCKS.registerBlock("chantarelle",
            properties -> new ChantarelleBlock(properties, PATCH_CHANTARELLE_CONFIGURED),
            BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .noCollission()
                            .randomTicks()
                            .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> PINE_CONK = BLOCKS.registerBlock("pine_conk",
            properties -> new PineConkMushroomBlock(properties),
            BlockBehaviour.Properties.of()
                            .randomTicks()
                            .sound(SoundType.WOOD));

    // Water plants
    public static final DeferredBlock<Block> REED = BLOCKS.registerBlock("reed",
            properties -> new ReedBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> CATTAIL = BLOCKS.registerBlock("cattail",
            properties -> new CattailBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Pine tree
    public static final DeferredBlock<Block> PINE_SAPLING = BLOCKS.registerBlock("pine_sapling",
            properties -> new SaplingBlock(PineTreeGrower.PINE, properties),
            BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> PINE_LOG = BLOCKS.registerBlock("pine_log",
            properties -> new RotatedPillarBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f));

    // Worldgen-only bark stages up a pine trunk; no items, they drop a normal pine log
    public static final DeferredBlock<Block> PEELING_PINE_LOG = BLOCKS.registerBlock("peeling_pine_log",
            properties -> new RotatedPillarBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f));

    public static final DeferredBlock<Block> PEELED_PINE_LOG = BLOCKS.registerBlock("peeled_pine_log",
            properties -> new RotatedPillarBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(2f, 2f));

    public static final DeferredBlock<Block> PINE_PLANKS = BLOCKS.registerBlock("pine_planks",
            properties -> new Block(properties),
            pinePlanksProperties());

    public static final DeferredBlock<StairBlock> PINE_STAIRS = BLOCKS.registerBlock("pine_stairs",
            properties -> new StairBlock(PINE_PLANKS.get().defaultBlockState(), properties),
            pinePlanksProperties());

    public static final DeferredBlock<SlabBlock> PINE_SLAB = BLOCKS.registerBlock("pine_slab",
            properties -> new SlabBlock(properties),
            pinePlanksProperties());

    public static final DeferredBlock<FenceBlock> SLANTED_FENCE = BLOCKS.registerBlock("slanted_fence",
            properties -> new io.github.jesperandersson.vildmark.world.level.block.SlantedFenceBlock(properties),
            pinePlanksProperties()
                    .forceSolidOn()
                    .strength(2f, 3f));

    public static final DeferredBlock<Block> PINE_LEAVES = BLOCKS.registerBlock("pine_leaves",
            properties -> new PineLeavesBlock(properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .randomTicks()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false));

    public static final DeferredBlock<Block> BEARD_LICHEN = BLOCKS.registerBlock("beard_lichen",
            properties -> new BeardLichenBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.HANGING_ROOTS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> PINE_NEEDLES = BLOCKS.registerBlock("pine_needles",
            properties -> new PineNeedlesBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Bog
    public static final DeferredBlock<Block> PEAT = BLOCKS.registerBlock("peat",
            properties -> new Block(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(SoundType.MUD));

    public static final DeferredBlock<Block> PEAT_IRON_ORE = BLOCKS.registerBlock("peat_iron_ore",
            properties -> new Block(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6f)
                    .sound(SoundType.MUD));

    public static final DeferredBlock<Block> COTTON_GRASS = BLOCKS.registerBlock("cotton_grass",
            properties -> new BogPlantBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> SUNDEW = BLOCKS.registerBlock("sundew",
            properties -> new SundewBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Ground vegetation
    public static final DeferredBlock<Block> STINGING_NETTLE = BLOCKS.registerBlock("stinging_nettle",
            properties -> new StingingNettleBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .randomTicks()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> TALL_STINGING_NETTLE = BLOCKS.registerBlock("tall_stinging_nettle",
            properties -> new TallStingingNettleBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> SNAKE_FERN = BLOCKS.registerBlock("snakefern",
            properties -> new SnakeFernBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> HEATHER = BLOCKS.registerBlock("heather",
            properties -> new HeatherBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<CakeBlock> BLUEBERRY_PIE = BLOCKS.registerBlock("blueberry_pie",
            properties -> new io.github.jesperandersson.vildmark.world.level.block.BlueberryPieBlock(properties),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .forceSolidOn()
                    .strength(0.5f)
                    .sound(SoundType.MUD)
                    .pushReaction(PushReaction.DESTROY));

    // Built fresh for each block: the planks block doesn't exist yet when these properties are evaluated,
    // and each block needs its own Properties since registerBlock sets the ID on them
    private static BlockBehaviour.Properties pinePlanksProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(2f, 2f);
    }

    public static void register(){}
}
