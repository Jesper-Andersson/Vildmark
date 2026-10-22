package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BeardLichenBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineConkMushroomBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineNeedlesBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.FenceBlock;
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

public class VBlockStateProvider extends BlockStateProvider {
    public VBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper){
        super(output, Vildmark.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Pine wood
        ResourceLocation pineLogTop = modLoc("block/pine_log_top");
        pineLog(VBlocks.PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELING_PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELED_PINE_LOG.get(), pineLogTop);

        ResourceLocation planks = blockTexture(VBlocks.PINE_PLANKS.get());
        simpleBlock(VBlocks.PINE_PLANKS.get());
        stairsBlock(VBlocks.PINE_STAIRS.get(), planks);
        slabBlock(VBlocks.PINE_SLAB.get(), planks, planks);
        slantedFence();
        simpleBlock(VBlocks.PINE_LEAVES.get(), models().leaves(name(VBlocks.PINE_LEAVES.get()), blockTexture(VBlocks.PINE_LEAVES.get())));
        simpleBlock(VBlocks.PINE_SAPLING.get(), cross(name(VBlocks.PINE_SAPLING.get())));
        pineNeedles();
        beardLichen();
        pineConk();
        blueberryPie();

        // Bog
        simpleBlock(VBlocks.PEAT.get());
        simpleBlock(VBlocks.PEAT_IRON_ORE.get());
        simpleBlock(VBlocks.COTTON_GRASS.get(), cross(name(VBlocks.COTTON_GRASS.get())));
        simpleBlock(VBlocks.SUNDEW.get(), cross(name(VBlocks.SUNDEW.get())));

        // Plants
        shrub(VBlocks.LINGONBERRY_SHRUB.get());
        shrub(VBlocks.BLUEBERRY_SHRUB.get());
        shrub(VBlocks.CLOUDBERRY_SHRUB.get());
        simpleBlock(VBlocks.CHANTARELLE.get(), cross(name(VBlocks.CHANTARELLE.get())));
        doublePlant(VBlocks.REED.get(), false);
        doublePlant(VBlocks.CATTAIL.get(), false);
        doublePlant(VBlocks.TALL_STINGING_NETTLE.get(), true);
        simpleBlock(VBlocks.STINGING_NETTLE.get(), tintedCross(name(VBlocks.STINGING_NETTLE.get())));
        simpleBlock(VBlocks.HEATHER.get(), tintedCross(name(VBlocks.HEATHER.get())));
        simpleBlock(VBlocks.SNAKE_FERN.get(), models().getBuilder(name(VBlocks.SNAKE_FERN.get()))
                .parent(models().getExistingFile(mcLoc("block/block")))
                .customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/block/snakefern.obj"))
                .automaticCulling(false)
                .emissiveAmbient(false)
                .end()
                .renderType("cutout")
                .ao(false)
                .texture("texture", blockTexture(VBlocks.SNAKE_FERN.get()))
                .texture("particle", blockTexture(VBlocks.SNAKE_FERN.get())));

        registerItemModels();
    }

    private void registerItemModels() {
        blockItem(VItems.PINE_LOG_ITEM.get());
        blockItem(VItems.PINE_PLANKS_ITEM.get());
        blockItem(VItems.PINE_STAIRS_ITEM.get());
        blockItem(VItems.PINE_SLAB_ITEM.get());
        itemModels().withExistingParent(name(VItems.SLANTED_FENCE_ITEM.get()), modLoc("block/slanted_fence_inventory"));
        blockItem(VItems.PINE_LEAVES_ITEM.get());
        blockItem(VItems.PINE_SAPLING_ITEM.get());
        blockItem(VItems.PEAT_ITEM.get());
        blockItem(VItems.PEAT_IRON_ORE_ITEM.get());
        blockItem(VItems.PINE_CONK_ITEM.get());
        blockItem(VItems.SNAKE_FERN_ITEM.get());

        flatItem(VItems.PINE_NEEDLES_ITEM.get(), modLoc("block/pine_needles"));
        flatItem(VItems.BEARD_LICHEN_ITEM.get(), modLoc("block/beard_lichen_tip"));
        flatItem(VItems.COTTON_GRASS_ITEM.get(), modLoc("block/cotton_grass"));
        flatItem(VItems.SUNDEW_ITEM.get(), modLoc("block/sundew"));
        flatItem(VItems.LINGONBERRY_SHRUB_ITEM.get(), modLoc("block/lingonberry_shrub_stage2"));
        flatItem(VItems.BLUEBERRY_SHRUB_ITEM.get(), modLoc("block/blueberry_shrub_stage2"));
        flatItem(VItems.CLOUDBERRY_SHRUB_ITEM.get(), modLoc("block/cloudberry_shrub_stage2"));
        flatItem(VItems.CHANTARELLE_ITEM.get(), modLoc("block/chantarelle"));
        flatItem(VItems.REED_ITEM.get(), modLoc("block/reed_top"));
        flatItem(VItems.CATTAIL_ITEM.get(), modLoc("block/cattail_top"));
        flatItem(VItems.STINGING_NETTLE_ITEM.get(), modLoc("block/stinging_nettle"));
        flatItem(VItems.HEATHER_ITEM.get(), modLoc("block/heather"));
        flatItem(VItems.BLUEBERRY_PIE_ITEM.get(), modLoc("item/blueberry_pie"));

        itemModels().basicItem(VItems.LINGONBERRIES_ITEM.get());
        itemModels().basicItem(VItems.BLUEBERRIES_ITEM.get());
        itemModels().basicItem(VItems.CLOUDBERRIES_ITEM.get());
        itemModels().basicItem(VItems.BARROW_WIGHT_SPAWN_EGG.get());

        // Fish reuse the vanilla salmon and cod textures for now
        flatItem(VItems.PIKE.get(), mcLoc("item/salmon"));
        flatItem(VItems.COOKED_PIKE.get(), mcLoc("item/cooked_salmon"));
        flatItem(VItems.PIKE_BUCKET.get(), mcLoc("item/salmon_bucket"));
        flatItem(VItems.ROACH.get(), mcLoc("item/cod"));
        flatItem(VItems.COOKED_ROACH.get(), mcLoc("item/cooked_cod"));
        flatItem(VItems.ROACH_BUCKET.get(), mcLoc("item/cod_bucket"));

        spawnEgg(VItems.PIKE_SPAWN_EGG.get());
        spawnEgg(VItems.ROACH_SPAWN_EGG.get());
        spawnEgg(VItems.MOOSE_SPAWN_EGG.get());
        spawnEgg(VItems.TOMTE_SPAWN_EGG.get());
    }

    // Uses the vanilla cake and cake_slice1-6 models with the pie textures, one model per bite
    private void blueberryPie(){
        Block block = VBlocks.BLUEBERRY_PIE.get();
        getVariantBuilder(block).forAllStates(state -> {
            int bites = state.getValue(CakeBlock.BITES);
            String suffix = bites == 0 ? "" : "_slice" + bites;
            ModelFile model = models().withExistingParent(name(block) + suffix, mcLoc("block/cake" + suffix))
                    .texture("particle", modLoc("block/blueberry_pie_side"))
                    .texture("bottom", modLoc("block/blueberry_pie_bottom"))
                    .texture("top", modLoc("block/blueberry_pie_top"))
                    .texture("side", modLoc("block/blueberry_pie_side"))
                    .texture("inside", modLoc("block/blueberry_pie_inner"));
            return ConfiguredModel.builder().modelFile(model).build();
        });
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

    // Side model reaches the next post to the south, so only south and east are drawn (see SlantedFenceBlock)
    private void slantedFence() {
        ModelFile post = models().getExistingFile(modLoc("block/slanted_fence_post"));
        ModelFile side = models().getExistingFile(modLoc("block/slanted_fence_side"));
        getMultipartBuilder(VBlocks.SLANTED_FENCE.get())
                .part().modelFile(post).addModel().end()
                .part().modelFile(side).addModel().condition(FenceBlock.SOUTH, true).end()
                .part().modelFile(side).rotationY(270).addModel().condition(FenceBlock.EAST, true).end();
    }

    // Attaches to log, models oriented to north
    private void pineConk(){
        Block block = VBlocks.PINE_CONK.get();
        ModelFile young = models().getExistingFile(modLoc("block/pine_conk_small"));
        ModelFile mature = models().getExistingFile(modLoc("block/pine_conk"));

        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction facing : PineConkMushroomBlock.FACING.getPossibleValues()) {
            for (int age : PineConkMushroomBlock.AGE.getPossibleValues()) {
                ModelFile model;
                int rotation = yRotation(facing);

                if (age < PineConkMushroomBlock.MAX_AGE){
                    model = young;
                }
                else {
                    model = mature;
                }

                builder.partialState().with(PineConkMushroomBlock.FACING, facing).with(PineConkMushroomBlock.AGE , age)
                        .addModels(new ConfiguredModel(model, 0, rotation, false));
            }
        }
    }

    // Hangs straight down from a ceiling or lies flat against a wall, with a separate texture for the bottom block
    private void beardLichen() {
        Block block = VBlocks.BEARD_LICHEN.get();
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
        Block block = VBlocks.PINE_NEEDLES.get();
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
