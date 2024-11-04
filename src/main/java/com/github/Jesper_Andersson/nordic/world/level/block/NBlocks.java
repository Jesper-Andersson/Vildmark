package com.github.Jesper_Andersson.nordic.world.level.block;

import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.BlueberryShrubBlock;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.ChantarelleBlock;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.LingonberryShrubBlock;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.PineLeavesBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

import static com.github.Jesper_Andersson.nordic.Nordic.BLOCKS;
import static com.github.Jesper_Andersson.nordic.Nordic.PATCH_CHANTARELLE_CONFIGURED;

public class NBlocks {
    public static final RegistryObject<Block> LINGONBERRY_SHRUB = BLOCKS.register("lingonberry_shrub", () -> new LingonberryShrubBlock(BlockBehaviour.Properties.of().noOcclusion().noCollission().randomTicks().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> BLUEBERRY_SHRUB = BLOCKS.register("blueberry_shrub", () -> new BlueberryShrubBlock(BlockBehaviour.Properties.of().noOcclusion().noCollission().randomTicks().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> CHANTARELLE = BLOCKS.register("chantarelle", () -> new ChantarelleBlock(BlockBehaviour.Properties.of().noOcclusion().noCollission().randomTicks().sound(SoundType.GRASS), PATCH_CHANTARELLE_CONFIGURED));

    public static final RegistryObject<Block> PINE_SAPLING = BLOCKS.register("pine_sapling", () -> new SaplingBlock(new OakTreeGrower(), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> PINE_LOG = BLOCKS.register("pine_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2f,2f)));
    public static final RegistryObject<Block> PINE_PLANKS = BLOCKS.register("pine_planks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2f, 2f)));
    public static final RegistryObject<Block> PINE_LEAVES = BLOCKS.register("pine_leaves", () -> new PineLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).randomTicks().mapColor(MapColor.COLOR_GREEN).noOcclusion().sound(SoundType.GRASS).isViewBlocking((((state, level, pos) -> false))).isSuffocating((state, level, pos) -> false)));

    public static void register(){}
}
