package com.github.Jesper_Andersson.nordic.datagen;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.BeardLichenBlock;
import com.github.Jesper_Andersson.nordic.world.level.block.plants.vegetation.PineNeedlesBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NBlockStateProvider extends BlockStateProvider {
    public NBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper){
        super(output, Nordic.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Pine wood
        ResourceLocation pineLogTop = modLoc("block/pine_log_top");
        pineLog(NBlocks.PINE_LOG.get(), pineLogTop);
        pineLog(NBlocks.PEELING_PINE_LOG.get(), pineLogTop);
        pineLog(NBlocks.PEELED_PINE_LOG.get(), pineLogTop);

        ResourceLocation planks = blockTexture(NBlocks.PINE_PLANKS.get());
        simpleBlock(NBlocks.PINE_PLANKS.get());
        stairsBlock(NBlocks.PINE_STAIRS.get(), planks);
        slabBlock(NBlocks.PINE_SLAB.get(), planks, planks);
        simpleBlock(NBlocks.PINE_LEAVES.get(), models().leaves(name(NBlocks.PINE_LEAVES.get()), blockTexture(NBlocks.PINE_LEAVES.get())));
        simpleBlock(NBlocks.PINE_SAPLING.get(), cross(name(NBlocks.PINE_SAPLING.get())));
        pineNeedles();
        beardLichen();

        // Bog
        simpleBlock(NBlocks.PEAT.get());
        simpleBlock(NBlocks.PEAT_IRON_ORE.get());
        simpleBlock(NBlocks.COTTON_GRASS.get(), cross(name(NBlocks.COTTON_GRASS.get())));
        simpleBlock(NBlocks.SUNDEW.get(), cross(name(NBlocks.SUNDEW.get())));

        // Plants
        shrub(NBlocks.LINGONBERRY_SHRUB.get());
        shrub(NBlocks.BLUEBERRY_SHRUB.get());
        shrub(NBlocks.CLOUDBERRY_SHRUB.get());
        simpleBlock(NBlocks.CHANTARELLE.get(), cross(name(NBlocks.CHANTARELLE.get())));
        doublePlant(NBlocks.REED.get(), false);
        doublePlant(NBlocks.CATTAIL.get(), false);
        doublePlant(NBlocks.TALL_STINGING_NETTLE.get(), true);
        simpleBlock(NBlocks.STINGING_NETTLE.get(), tintedCross(name(NBlocks.STINGING_NETTLE.get())));
        simpleBlock(NBlocks.HEATHER.get(), tintedCross(name(NBlocks.HEATHER.get())));
        simpleBlock(NBlocks.SNAKE_FERN.get(), models().getBuilder(name(NBlocks.SNAKE_FERN.get()))
                .parent(models().getExistingFile(mcLoc("block/block")))
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/block/snakefern.obj"))
                .automaticCulling(false)
                .emissiveAmbient(false)
                .end()
                .renderType("cutout")
                .ao(false)
                .texture("texture", blockTexture(NBlocks.SNAKE_FERN.get()))
                .texture("particle", blockTexture(NBlocks.SNAKE_FERN.get())));

        registerItemModels();
    }

    private void registerItemModels() {
        blockItem(NItems.PINE_LOG_ITEM.get());
        blockItem(NItems.PINE_PLANKS_ITEM.get());
        blockItem(NItems.PINE_STAIRS_ITEM.get());
        blockItem(NItems.PINE_SLAB_ITEM.get());
        blockItem(NItems.PINE_LEAVES_ITEM.get());
        blockItem(NItems.PINE_SAPLING_ITEM.get());
        blockItem(NItems.PEAT_ITEM.get());
        blockItem(NItems.PEAT_IRON_ORE_ITEM.get());
        blockItem(NItems.SNAKE_FERN_ITEM.get());

        flatItem(NItems.PINE_NEEDLES_ITEM.get(), modLoc("block/pine_needles"));
        flatItem(NItems.BEARD_LICHEN_ITEM.get(), modLoc("block/beard_lichen_tip"));
        flatItem(NItems.COTTON_GRASS_ITEM.get(), modLoc("block/cotton_grass"));
        flatItem(NItems.SUNDEW_ITEM.get(), modLoc("block/sundew"));
        flatItem(NItems.LINGONBERRY_SHRUB_ITEM.get(), modLoc("block/lingonberry_shrub_stage2"));
        flatItem(NItems.BLUEBERRY_SHRUB_ITEM.get(), modLoc("block/blueberry_shrub_stage2"));
        flatItem(NItems.CLOUDBERRY_SHRUB_ITEM.get(), modLoc("block/cloudberry_shrub_stage2"));
        flatItem(NItems.CHANTARELLE_ITEM.get(), modLoc("block/chantarelle"));
        flatItem(NItems.REED_ITEM.get(), modLoc("block/reed_top"));
        flatItem(NItems.CATTAIL_ITEM.get(), modLoc("block/cattail_top"));
        flatItem(NItems.STINGING_NETTLE_ITEM.get(), modLoc("block/stinging_nettle"));
        flatItem(NItems.HEATHER_ITEM.get(), modLoc("block/heather"));

        itemModels().basicItem(NItems.LINGONBERRIES_ITEM.get());
        itemModels().basicItem(NItems.BLUEBERRIES_ITEM.get());
        itemModels().basicItem(NItems.CLOUDBERRIES_ITEM.get());
        itemModels().basicItem(NItems.BARROW_WIGHT_SPAWN_EGG.get());

        // Fish reuse the vanilla salmon and cod textures for now
        flatItem(NItems.PIKE.get(), mcLoc("item/salmon"));
        flatItem(NItems.COOKED_PIKE.get(), mcLoc("item/cooked_salmon"));
        flatItem(NItems.PIKE_BUCKET.get(), mcLoc("item/salmon_bucket"));
        flatItem(NItems.ROACH.get(), mcLoc("item/cod"));
        flatItem(NItems.COOKED_ROACH.get(), mcLoc("item/cooked_cod"));
        flatItem(NItems.ROACH_BUCKET.get(), mcLoc("item/cod_bucket"));

        spawnEgg(NItems.PIKE_SPAWN_EGG.get());
        spawnEgg(NItems.ROACH_SPAWN_EGG.get());
        spawnEgg(NItems.MOOSE_SPAWN_EGG.get());
        spawnEgg(NItems.TOMTE_SPAWN_EGG.get());
    }

    private void pineLog(Block block, ResourceLocation end) {
        axisBlock((RotatedPillarBlock) block, blockTexture(block), end);
    }

    private void shrub(Block block) {
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(cross(name(block) + "_stage" + state.getValue(BlockStateProperties.AGE_4)))
                .build());
    }

    private void doublePlant(Block block, boolean tinted) {
        ModelFile bottom = tinted ? tintedCross(name(block) + "_bottom") : cross(name(block) + "_bottom");
        ModelFile top = tinted ? tintedCross(name(block) + "_top") : cross(name(block) + "_top");
        getVariantBuilder(block)
                .partialState().with(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER).addModels(new ConfiguredModel(bottom))
                .partialState().with(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER).addModels(new ConfiguredModel(top));
    }

    // Hangs straight down from a ceiling or lies flat against a wall, with a separate texture for the bottom block
    private void beardLichen() {
        Block block = NBlocks.BEARD_LICHEN.get();
        ModelFile hanging = cross("beard_lichen");
        ModelFile hangingTip = cross("beard_lichen_tip");
        ModelFile wall = lichenWall("beard_lichen_wall", modLoc("block/beard_lichen"));
        ModelFile wallTip = lichenWall("beard_lichen_wall_tip", modLoc("block/beard_lichen_tip"));

        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction facing : BeardLichenBlock.FACING.getPossibleValues()) {
            for (boolean tip : BeardLichenBlock.TIP.getPossibleValues()) {
                ModelFile model = facing == Direction.UP ? (tip ? hangingTip : hanging) : (tip ? wallTip : wall);
                int rotation = facing == Direction.UP ? 0 : yRotation(facing);
                builder.partialState().with(BeardLichenBlock.FACING, facing).with(BeardLichenBlock.TIP, tip)
                        .addModels(new ConfiguredModel(model, 0, rotation, false));
            }
        }
    }

    private BlockModelBuilder lichenWall(String name, ResourceLocation texture) {
        return models().getBuilder(name)
                .ao(false)
                .renderType("cutout")
                .texture("particle", texture)
                .texture("lichen", texture)
                .element()
                .from(0, 0, 0.8f).to(16, 16, 0.8f)
                .shade(false)
                .face(Direction.NORTH).uvs(16, 0, 0, 16).texture("#lichen").end()
                .face(Direction.SOUTH).uvs(0, 0, 16, 16).texture("#lichen").end()
                .end();
    }

    // Like pink petals: each extra needle layer fills the next quarter of the block
    private void pineNeedles() {
        Block block = NBlocks.PINE_NEEDLES.get();
        int[][] quarters = {{0, 0}, {0, 8}, {8, 8}, {8, 0}};
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        for (int i = 0; i < quarters.length; i++) {
            int x = quarters[i][0];
            int z = quarters[i][1];
            ModelFile model = models().getBuilder("pine_needles_" + (i + 1))
                    .ao(false)
                    .renderType("cutout")
                    .texture("particle", modLoc("block/pine_needles"))
                    .texture("texture", modLoc("block/pine_needles"))
                    .element()
                    .from(x, 0.25f, z).to(x + 8, 0.25f, z + 8)
                    .face(Direction.UP).uvs(x, z, x + 8, z + 8).texture("#texture").end()
                    .face(Direction.DOWN).uvs(x, z + 8, x + 8, z).texture("#texture").end()
                    .end();

            // Quarter i shows once there are at least i + 1 needle layers
            int minAmount = i + 1;
            Integer[] amounts = PineNeedlesBlock.AMOUNT.getPossibleValues().stream()
                    .filter(amount -> amount >= minAmount)
                    .toArray(Integer[]::new);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                builder.part().modelFile(model).rotationY(yRotation(facing)).addModel()
                        .condition(PineNeedlesBlock.FACING, facing)
                        .condition(PineNeedlesBlock.AMOUNT, amounts)
                        .end();
            }
        }
    }

    // Blockstate y rotation for a model authored facing north
    private static int yRotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    private ModelFile cross(String name) {
        return models().cross(name, modLoc("block/" + name)).renderType("cutout");
    }

    private ModelFile tintedCross(String name) {
        return models().withExistingParent(name, mcLoc("block/tinted_cross"))
                .renderType("cutout")
                .texture("cross", modLoc("block/" + name));
    }

    private void blockItem(ItemLike item) {
        String name = name(item);
        itemModels().withExistingParent(name, modLoc("block/" + name));
    }

    private void flatItem(ItemLike item, ResourceLocation texture) {
        itemModels().withExistingParent(name(item), mcLoc("item/generated")).texture("layer0", texture);
    }

    private void spawnEgg(ItemLike item) {
        itemModels().withExistingParent(name(item), mcLoc("item/template_spawn_egg"));
    }

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private static String name(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
