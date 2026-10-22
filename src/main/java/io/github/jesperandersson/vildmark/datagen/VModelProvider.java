package io.github.jesperandersson.vildmark.datagen;

import io.github.jesperandersson.vildmark.ColorHandler;
import io.github.jesperandersson.vildmark.Vildmark;
import io.github.jesperandersson.vildmark.world.item.VItems;
import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.BeardLichenBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineConkMushroomBlock;
import io.github.jesperandersson.vildmark.world.level.block.plants.vegetation.PineNeedlesBlock;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.Optional;

/**
 * Block states, block models and item models. Block items without an entry here get their block's model.
 */
public class VModelProvider extends ModelProvider {
    private static final ModelTemplate CROSS = ExtendedModelTemplateBuilder.of(ModelTemplates.CROSS).renderType("cutout").build();
    private static final ModelTemplate TINTED_CROSS = ExtendedModelTemplateBuilder.of(ModelTemplates.TINTED_CROSS).renderType("cutout").build();
    private static final TextureSlot LICHEN = TextureSlot.create("lichen");

    private BlockModelGenerators blockModels;
    private ItemModelGenerators itemModels;

    public VModelProvider(PackOutput output) {
        super(output, Vildmark.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        this.itemModels = itemModels;

        // Pine wood
        ResourceLocation pineLogTop = modLoc("block/pine_log_top");
        pineLog(VBlocks.PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELING_PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELED_PINE_LOG.get(), pineLogTop);

        blockModels.createTrivialCube(VBlocks.PINE_PLANKS.get());
        pineStairsAndSlab();
        slantedFence();
        simpleBlock(VBlocks.PINE_LEAVES.get(), ModelTemplates.LEAVES.create(VBlocks.PINE_LEAVES.get(), TextureMapping.cube(VBlocks.PINE_LEAVES.get()), blockModels.modelOutput));
        crossBlock(VBlocks.PINE_SAPLING.get(), CROSS);
        pineNeedles();
        beardLichen();
        pineConk();
        blueberryPie();

        // Bog
        blockModels.createTrivialCube(VBlocks.PEAT.get());
        blockModels.createTrivialCube(VBlocks.PEAT_IRON_ORE.get());
        crossBlock(VBlocks.COTTON_GRASS.get(), CROSS);
        crossBlock(VBlocks.SUNDEW.get(), CROSS);

        // Plants
        shrub(VBlocks.LINGONBERRY_SHRUB.get());
        shrub(VBlocks.BLUEBERRY_SHRUB.get());
        shrub(VBlocks.CLOUDBERRY_SHRUB.get());
        crossBlock(VBlocks.CHANTARELLE.get(), CROSS);
        doublePlant(VBlocks.REED.get(), CROSS);
        doublePlant(VBlocks.CATTAIL.get(), CROSS);
        doublePlant(VBlocks.TALL_STINGING_NETTLE.get(), TINTED_CROSS);
        crossBlock(VBlocks.STINGING_NETTLE.get(), TINTED_CROSS);
        crossBlock(VBlocks.HEATHER.get(), TINTED_CROSS);
        snakeFern();

        registerItemModels();
    }

    private void registerItemModels() {
        itemModel(VItems.SLANTED_FENCE_ITEM.get(), ItemModelUtils.plainModel(modLoc("block/slanted_fence_inventory")));

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
        flatItem(VItems.BLUEBERRY_PIE_ITEM.get(), modLoc("item/blueberry_pie"));

        // Tints used to come from item color handlers, now they're part of the item model
        itemModel(VItems.PINE_LEAVES_ITEM.get(), ItemModelUtils.tintedModel(ModelLocationUtils.getModelLocation(VBlocks.PINE_LEAVES.get()),
                ItemModelUtils.constantTint(FoliageColor.FOLIAGE_DEFAULT)));
        itemModel(VItems.SNAKE_FERN_ITEM.get(), ItemModelUtils.tintedModel(ModelLocationUtils.getModelLocation(VBlocks.SNAKE_FERN.get()),
                new GrassColorSource()));
        itemModel(VItems.STINGING_NETTLE_ITEM.get(), ItemModelUtils.tintedModel(flatItemModel(VItems.STINGING_NETTLE_ITEM.get(), modLoc("block/stinging_nettle")),
                new GrassColorSource()));
        itemModel(VItems.HEATHER_ITEM.get(), ItemModelUtils.tintedModel(flatItemModel(VItems.HEATHER_ITEM.get(), modLoc("block/heather")),
                ItemModelUtils.constantTint(ColorHandler.HEATHER_COLOR)));

        basicItem(VItems.LINGONBERRIES_ITEM.get());
        basicItem(VItems.BLUEBERRIES_ITEM.get());
        basicItem(VItems.CLOUDBERRIES_ITEM.get());
        basicItem(VItems.BARROW_WIGHT_SPAWN_EGG.get());

        // Fish reuse the vanilla salmon and cod textures for now
        flatItem(VItems.PIKE.get(), mcLoc("item/salmon"));
        flatItem(VItems.COOKED_PIKE.get(), mcLoc("item/cooked_salmon"));
        flatItem(VItems.PIKE_BUCKET.get(), mcLoc("item/salmon_bucket"));
        flatItem(VItems.ROACH.get(), mcLoc("item/cod"));
        flatItem(VItems.COOKED_ROACH.get(), mcLoc("item/cooked_cod"));
        flatItem(VItems.ROACH_BUCKET.get(), mcLoc("item/cod_bucket"));

        // Spawn egg colors moved here from the spawn egg items
        itemModels.generateSpawnEgg(VItems.PIKE_SPAWN_EGG.get(), 0x4F5F32, 0xC8C27A);
        itemModels.generateSpawnEgg(VItems.ROACH_SPAWN_EGG.get(), 0xB9C0C6, 0xC2402E);
        itemModels.generateSpawnEgg(VItems.MOOSE_SPAWN_EGG.get(), 0x4A3526, 0xA08B6E);
        itemModels.generateSpawnEgg(VItems.TOMTE_SPAWN_EGG.get(), 0xB02A2A, 0x7A7A7A);
        itemModels.generateSpawnEgg(VItems.WILL_O_THE_WISP_SPAWN_EGG.get(), 0xB02A2A, 0x7A7A7A);
    }

    // Uses the vanilla cake and cake_slice1-6 models with the pie textures, one model per bite
    private void blueberryPie() {
        Block block = VBlocks.BLUEBERRY_PIE.get();
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, modLoc("block/blueberry_pie_side"))
                .put(TextureSlot.BOTTOM, modLoc("block/blueberry_pie_bottom"))
                .put(TextureSlot.TOP, modLoc("block/blueberry_pie_top"))
                .put(TextureSlot.SIDE, modLoc("block/blueberry_pie_side"))
                .put(TextureSlot.INSIDE, modLoc("block/blueberry_pie_inner"));

        PropertyDispatch.C1<Integer> dispatch = PropertyDispatch.property(CakeBlock.BITES);
        for (int bites : CakeBlock.BITES.getPossibleValues()) {
            String suffix = bites == 0 ? "" : "_slice" + bites;
            ModelTemplate template = new ModelTemplate(Optional.of(mcLoc("block/cake" + suffix)), Optional.of(suffix),
                    TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.INSIDE);
            dispatch.select(bites, model(template.create(block, textures, blockModels.modelOutput)));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(dispatch));
    }

    private void pineLog(Block block, ResourceLocation end) {
        TextureMapping textures = TextureMapping.column(TextureMapping.getBlockTexture(block), end);
        ResourceLocation vertical = ModelTemplates.CUBE_COLUMN.create(block, textures, blockModels.modelOutput);
        ResourceLocation horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(block, textures, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block, vertical, horizontal));
    }

    private void pineStairsAndSlab() {
        TextureMapping planks = TextureMapping.cube(VBlocks.PINE_PLANKS.get());

        Block stairs = VBlocks.PINE_STAIRS.get();
        blockModels.blockStateOutput.accept(BlockModelGenerators.createStairs(stairs,
                ModelTemplates.STAIRS_INNER.create(stairs, planks, blockModels.modelOutput),
                ModelTemplates.STAIRS_STRAIGHT.create(stairs, planks, blockModels.modelOutput),
                ModelTemplates.STAIRS_OUTER.create(stairs, planks, blockModels.modelOutput)));

        Block slab = VBlocks.PINE_SLAB.get();
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSlab(slab,
                ModelTemplates.SLAB_BOTTOM.create(slab, planks, blockModels.modelOutput),
                ModelTemplates.SLAB_TOP.create(slab, planks, blockModels.modelOutput),
                ModelLocationUtils.getModelLocation(VBlocks.PINE_PLANKS.get())));
    }

    private void shrub(Block block) {
        PropertyDispatch.C1<Integer> dispatch = PropertyDispatch.property(BlockStateProperties.AGE_4);
        for (int age : BlockStateProperties.AGE_4.getPossibleValues()) {
            String suffix = "_stage" + age;
            ResourceLocation texture = TextureMapping.getBlockTexture(block, suffix);
            dispatch.select(age, model(CROSS.create(ModelLocationUtils.getModelLocation(block, suffix), TextureMapping.cross(texture), blockModels.modelOutput)));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(dispatch));
    }

    private void doublePlant(Block block, ModelTemplate template) {
        ResourceLocation bottom = template.create(ModelLocationUtils.getModelLocation(block, "_bottom"),
                TextureMapping.cross(TextureMapping.getBlockTexture(block, "_bottom")), blockModels.modelOutput);
        ResourceLocation top = template.create(ModelLocationUtils.getModelLocation(block, "_top"),
                TextureMapping.cross(TextureMapping.getBlockTexture(block, "_top")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        .select(DoubleBlockHalf.LOWER, model(bottom))
                        .select(DoubleBlockHalf.UPPER, model(top))));
    }

    // Side model reaches the next post to the south, so only south and east are drawn (see SlantedFenceBlock)
    private void slantedFence() {
        ResourceLocation post = modLoc("block/slanted_fence_post");
        ResourceLocation side = modLoc("block/slanted_fence_side");
        blockModels.blockStateOutput.accept(MultiPartGenerator.multiPart(VBlocks.SLANTED_FENCE.get())
                .with(model(post))
                .with(Condition.condition().term(FenceBlock.SOUTH, true), model(side))
                .with(Condition.condition().term(FenceBlock.EAST, true), model(side).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)));
    }

    // Attaches to log, models oriented to north
    private void pineConk() {
        ResourceLocation young = modLoc("block/pine_conk_small");
        ResourceLocation mature = modLoc("block/pine_conk");

        PropertyDispatch.C2<Direction, Integer> dispatch = PropertyDispatch.properties(PineConkMushroomBlock.FACING, PineConkMushroomBlock.AGE);
        for (Direction facing : PineConkMushroomBlock.FACING.getPossibleValues()) {
            for (int age : PineConkMushroomBlock.AGE.getPossibleValues()) {
                ResourceLocation model = age < PineConkMushroomBlock.MAX_AGE ? young : mature;
                dispatch.select(facing, age, model(model, facing));
            }
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(VBlocks.PINE_CONK.get()).with(dispatch));
    }

    // Hangs straight down from a ceiling or lies flat against a wall, with a separate texture for the bottom block
    private void beardLichen() {
        Block block = VBlocks.BEARD_LICHEN.get();
        ResourceLocation hanging = CROSS.create(block, TextureMapping.cross(block), blockModels.modelOutput);
        ResourceLocation hangingTip = CROSS.create(ModelLocationUtils.getModelLocation(block, "_tip"),
                TextureMapping.cross(TextureMapping.getBlockTexture(block, "_tip")), blockModels.modelOutput);
        ResourceLocation wall = lichenWall("beard_lichen_wall", modLoc("block/beard_lichen"));
        ResourceLocation wallTip = lichenWall("beard_lichen_wall_tip", modLoc("block/beard_lichen_tip"));

        PropertyDispatch.C2<Direction, Boolean> dispatch = PropertyDispatch.properties(BeardLichenBlock.FACING, BeardLichenBlock.TIP);
        for (Direction facing : BeardLichenBlock.FACING.getPossibleValues()) {
            for (boolean tip : BeardLichenBlock.TIP.getPossibleValues()) {
                Variant variant = facing == Direction.UP
                        ? model(tip ? hangingTip : hanging)
                        : model(tip ? wallTip : wall, facing);
                dispatch.select(facing, tip, variant);
            }
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(dispatch));
    }

    private ResourceLocation lichenWall(String name, ResourceLocation texture) {
        return ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(LICHEN)
                .ambientOcclusion(false)
                .renderType("cutout")
                .element(element -> element
                        .from(0, 0, 0.8f).to(16, 16, 0.8f)
                        .shade(false)
                        .face(Direction.NORTH, face -> face.uvs(16, 0, 0, 16).texture(LICHEN))
                        .face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(LICHEN)))
                .build()
                .create(modLoc("block/" + name), new TextureMapping().put(TextureSlot.PARTICLE, texture).put(LICHEN, texture), blockModels.modelOutput);
    }

    // Like pink petals: each extra needle layer fills the next quarter of the block
    private void pineNeedles() {
        Block block = VBlocks.PINE_NEEDLES.get();
        ResourceLocation texture = TextureMapping.getBlockTexture(block);
        TextureMapping textures = new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture);
        int[][] quarters = {{0, 0}, {0, 8}, {8, 8}, {8, 0}};
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);

        for (int i = 0; i < quarters.length; i++) {
            int x = quarters[i][0];
            int z = quarters[i][1];
            ResourceLocation model = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(TextureSlot.PARTICLE)
                    .requiredTextureSlot(TextureSlot.TEXTURE)
                    .ambientOcclusion(false)
                    .renderType("cutout")
                    .element(element -> element
                            .from(x, 0.25f, z).to(x + 8, 0.25f, z + 8)
                            .face(Direction.UP, face -> face.uvs(x, z, x + 8, z + 8).texture(TextureSlot.TEXTURE))
                            .face(Direction.DOWN, face -> face.uvs(x, z + 8, x + 8, z).texture(TextureSlot.TEXTURE)))
                    .build()
                    .create(modLoc("block/pine_needles_" + (i + 1)), textures, blockModels.modelOutput);

            // Quarter i shows once there are at least i + 1 needle layers
            int minAmount = i + 1;
            Integer[] amounts = PineNeedlesBlock.AMOUNT.getPossibleValues().stream()
                    .filter(amount -> amount > minAmount)
                    .toArray(Integer[]::new);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                generator.with(Condition.condition().term(PineNeedlesBlock.FACING, facing).term(PineNeedlesBlock.AMOUNT, minAmount, amounts),
                        model(model, facing));
            }
        }
        blockModels.blockStateOutput.accept(generator);
    }

    private void snakeFern() {
        Block block = VBlocks.SNAKE_FERN.get();
        ResourceLocation texture = TextureMapping.getBlockTexture(block);
        ResourceLocation model = ExtendedModelTemplateBuilder.builder()
                .parent(mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.TEXTURE)
                .customLoader(ObjModelBuilder::new, obj -> obj
                        .modelLocation(modLoc("models/block/snakefern.obj"))
                        .automaticCulling(false)
                        .emissiveAmbient(false))
                .renderType("cutout")
                .ambientOcclusion(false)
                .build()
                .create(block, new TextureMapping().put(TextureSlot.PARTICLE, texture).put(TextureSlot.TEXTURE, texture), blockModels.modelOutput);
        simpleBlock(block, model);
    }

    private void crossBlock(Block block, ModelTemplate template) {
        simpleBlock(block, template.create(block, TextureMapping.cross(block), blockModels.modelOutput));
    }

    private void simpleBlock(Block block, ResourceLocation model) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, model));
    }

    private void basicItem(Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void flatItem(Item item, ResourceLocation texture) {
        itemModel(item, ItemModelUtils.plainModel(flatItemModel(item, texture)));
    }

    private ResourceLocation flatItemModel(Item item, ResourceLocation texture) {
        return ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(texture), itemModels.modelOutput);
    }

    private void itemModel(Item item, ItemModel.Unbaked model) {
        itemModels.itemModelOutput.accept(item, model);
    }

    private static Variant model(ResourceLocation model) {
        return Variant.variant().with(VariantProperties.MODEL, model);
    }

    // Rotates a model authored facing north to face the given direction
    private static Variant model(ResourceLocation model, Direction facing) {
        VariantProperties.Rotation rotation = yRotation(facing);
        return rotation == VariantProperties.Rotation.R0 ? model(model) : model(model).with(VariantProperties.Y_ROT, rotation);
    }

    private static VariantProperties.Rotation yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> VariantProperties.Rotation.R90;
            case SOUTH -> VariantProperties.Rotation.R180;
            case WEST -> VariantProperties.Rotation.R270;
            default -> VariantProperties.Rotation.R0;
        };
    }

    private static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Vildmark.MODID, path);
    }

    private static ResourceLocation mcLoc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
