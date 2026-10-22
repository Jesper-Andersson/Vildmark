package io.github.jesperandersson.vildmark.world.level.block;

import io.github.jesperandersson.vildmark.world.level.block.plants.tree.grower.PineTreeGrower;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

import static io.github.jesperandersson.vildmark.Vildmark.BLOCKS;
import static io.github.jesperandersson.vildmark.Vildmark.CHANTARELLE_BONEMEAL_PLACED;
import static io.github.jesperandersson.vildmark.Vildmark.PATCH_CHANTARELLE_CONFIGURED;

/**
 * All of Vildmark's blocks. Each entry gives the block's ID, a factory that builds the block from
 * its properties, and a supplier for those properties. The supplier runs at registration, so each
 * block gets its own Properties instance.
 */
public class VBlocks {
    // Berry shrubs and mushrooms
    public static final DeferredBlock<Block> LINGONBERRY_SHRUB = BLOCKS.registerBlock(
            "lingonberry_shrub",
            LingonberryShrubBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> BLUEBERRY_SHRUB = BLOCKS.registerBlock(
            "blueberry_shrub",
            BlueberryShrubBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> CLOUDBERRY_SHRUB = BLOCKS.registerBlock(
            "cloudberry_shrub",
            CloudberryShrubBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> CHANTARELLE = BLOCKS.registerBlock(
            "chantarelle",
            properties -> new ChantarelleBlock(
                    properties, PATCH_CHANTARELLE_CONFIGURED, CHANTARELLE_BONEMEAL_PLACED),
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noCollision()
                    .randomTicks()
                    .sound(SoundType.GRASS));

    public static final DeferredBlock<Block> PINE_CONK = BLOCKS.registerBlock(
            "pine_conk",
            PineConkMushroomBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .randomTicks()
                    .sound(SoundType.WOOD));

    // Water plants
    public static final DeferredBlock<Block> REED = BLOCKS.registerBlock(
            "reed",
            ReedBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> CATTAIL = BLOCKS.registerBlock(
            "cattail",
            CattailBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Pine tree
    public static final DeferredBlock<Block> PINE_SAPLING = BLOCKS.registerBlock(
            "pine_sapling",
            properties -> new SaplingBlock(PineTreeGrower.PINE, properties),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> PINE_LOG = BLOCKS.registerBlock(
            "pine_log",
            RotatedPillarBlock::new,
            VBlocks::pineWoodProperties);

    // Worldgen-only bark stages up a pine trunk; no items, they drop a normal pine log
    public static final DeferredBlock<Block> PEELING_PINE_LOG = BLOCKS.registerBlock(
            "peeling_pine_log",
            RotatedPillarBlock::new,
            VBlocks::pineWoodProperties);

    public static final DeferredBlock<Block> PEELED_PINE_LOG = BLOCKS.registerBlock(
            "peeled_pine_log",
            RotatedPillarBlock::new,
            VBlocks::pineWoodProperties);

    public static final DeferredBlock<Block> PINE_PLANKS = BLOCKS.registerBlock(
            "pine_planks",
            Block::new,
            VBlocks::pineWoodProperties);

    public static final DeferredBlock<StairBlock> PINE_STAIRS = BLOCKS.registerBlock(
            "pine_stairs",
            properties -> new StairBlock(PINE_PLANKS.get().defaultBlockState(), properties),
            VBlocks::pineWoodProperties);

    public static final DeferredBlock<SlabBlock> PINE_SLAB = BLOCKS.registerBlock(
            "pine_slab",
            SlabBlock::new,
            VBlocks::pineWoodProperties);

    public static final DeferredBlock<FenceBlock> SLANTED_FENCE = BLOCKS.registerBlock(
            "slanted_fence",
            SlantedFenceBlock::new,
            () -> pineWoodProperties()
                    .forceSolidOn()
                    .strength(/* destroyTime= */ 2f, /* explosionResistance= */ 3f));

    public static final DeferredBlock<Block> PINE_LEAVES = BLOCKS.registerBlock(
            "pine_leaves",
            PineLeavesBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .randomTicks()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noOcclusion()
                    .sound(SoundType.GRASS)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false));

    public static final DeferredBlock<Block> BEARD_LICHEN = BLOCKS.registerBlock(
            "beard_lichen",
            BeardLichenBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.HANGING_ROOTS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> PINE_NEEDLES = BLOCKS.registerBlock(
            "pine_needles",
            PineNeedlesBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Bog
    public static final DeferredBlock<Block> PEAT = BLOCKS.registerBlock(
            "peat",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5f)
                    .sound(SoundType.MUD));

    public static final DeferredBlock<Block> PEAT_IRON_ORE = BLOCKS.registerBlock(
            "peat_iron_ore",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6f)
                    .sound(SoundType.MUD));

    public static final DeferredBlock<Block> COTTON_GRASS = BLOCKS.registerBlock(
            "cotton_grass",
            BogPlantBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> SUNDEW = BLOCKS.registerBlock(
            "sundew",
            SundewBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Ground vegetation
    public static final DeferredBlock<Block> STINGING_NETTLE = BLOCKS.registerBlock(
            "stinging_nettle",
            StingingNettleBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .randomTicks()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> TALL_STINGING_NETTLE = BLOCKS.registerBlock(
            "tall_stinging_nettle",
            TallStingingNettleBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> SNAKE_FERN = BLOCKS.registerBlock(
            "snakefern",
            SnakeFernBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final DeferredBlock<Block> HEATHER = BLOCKS.registerBlock(
            "heather",
            HeatherBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .instabreak()
                    .replaceable()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    // Food
    public static final DeferredBlock<CakeBlock> BLUEBERRY_PIE = BLOCKS.registerBlock(
            "blueberry_pie",
            BlueberryPieBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .forceSolidOn()
                    .strength(0.5f)
                    .sound(SoundType.MUD)
                    .pushReaction(PushReaction.DESTROY));

    // Shared by the pine logs, planks, stairs, slab and fence. A fresh instance each time, since
    // registerBlock sets the block's ID on the properties it's given
    private static BlockBehaviour.Properties pineWoodProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(/* destroyTime= */ 2f, /* explosionResistance= */ 2f);
    }

    // Called from the mod constructor only to load this class, which adds the blocks to BLOCKS
    public static void register() {}
}
