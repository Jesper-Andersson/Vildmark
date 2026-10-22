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
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
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
 * Block states, block models and item models. Block items without an entry here get their block's
 * model.
 *
 * <p>A block state file maps each state of a block to a model (a "variant"), optionally rotated. A
 * model template is a parent model with texture slots, and creating it with a texture mapping
 * writes a model file that fills those slots.
 *
 * <p>Models don't declare a render type: since 26.1 the chunk layer comes from each texture's
 * transparency, so plant textures with transparent pixels render as cutout on their own.
 */
public class VModelProvider extends ModelProvider {
    private static final TextureSlot LICHEN = TextureSlot.create("lichen");

    private BlockModelGenerators blockModels;
    private ItemModelGenerators itemModels;

    public VModelProvider(PackOutput output) {
        super(output, Vildmark.MODID);
    }

    @Override
    protected void registerModels(
            BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        this.itemModels = itemModels;

        // Pine wood
        Material pineLogTop = modTexture("block/pine_log_top");
        pineLog(VBlocks.PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELING_PINE_LOG.get(), pineLogTop);
        pineLog(VBlocks.PEELED_PINE_LOG.get(), pineLogTop);

        blockModels.createTrivialCube(VBlocks.PINE_PLANKS.get());
        pineStairsAndSlab();
        slantedFence();
        Block pineLeaves = VBlocks.PINE_LEAVES.get();
        TextureMapping pineLeavesTextures = TextureMapping.cube(pineLeaves);
        simpleBlock(pineLeaves, createModel(ModelTemplates.LEAVES, pineLeaves, pineLeavesTextures));
        crossBlock(VBlocks.PINE_SAPLING.get(), ModelTemplates.CROSS);
        pineNeedles();
        beardLichen();
        pineConk();
        blueberryPie();

        // Bog
        blockModels.createTrivialCube(VBlocks.PEAT.get());
        blockModels.createTrivialCube(VBlocks.PEAT_IRON_ORE.get());
        crossBlock(VBlocks.COTTON_GRASS.get(), ModelTemplates.CROSS);
        crossBlock(VBlocks.SUNDEW.get(), ModelTemplates.CROSS);

        // Plants. Tinted crosses use the grass color from ColorHandler
        shrub(VBlocks.LINGONBERRY_SHRUB.get());
        shrub(VBlocks.BLUEBERRY_SHRUB.get());
        shrub(VBlocks.CLOUDBERRY_SHRUB.get());
        crossBlock(VBlocks.CHANTARELLE.get(), ModelTemplates.CROSS);
        doublePlant(VBlocks.REED.get(), ModelTemplates.CROSS);
        doublePlant(VBlocks.CATTAIL.get(), ModelTemplates.CROSS);
        doublePlant(VBlocks.TALL_STINGING_NETTLE.get(), ModelTemplates.TINTED_CROSS);
        crossBlock(VBlocks.STINGING_NETTLE.get(), ModelTemplates.TINTED_CROSS);
        crossBlock(VBlocks.HEATHER.get(), ModelTemplates.TINTED_CROSS);
        snakeFern();

        registerItemModels();
    }

    private void registerItemModels() {
        itemModel(VItems.SLANTED_FENCE_ITEM.get(),
                ItemModelUtils.plainModel(modLoc("block/slanted_fence_inventory")));

        // Plants show their texture as a flat item instead of the 3D block model
        flatItem(VItems.PINE_NEEDLES_ITEM.get(), modTexture("block/pine_needles"));
        flatItem(VItems.BEARD_LICHEN_ITEM.get(), modTexture("block/beard_lichen_tip"));
        flatItem(VItems.COTTON_GRASS_ITEM.get(), modTexture("block/cotton_grass"));
        flatItem(VItems.SUNDEW_ITEM.get(), modTexture("block/sundew"));
        flatItem(VItems.LINGONBERRY_SHRUB_ITEM.get(), modTexture("block/lingonberry_shrub_stage2"));
        flatItem(VItems.BLUEBERRY_SHRUB_ITEM.get(), modTexture("block/blueberry_shrub_stage2"));
        flatItem(VItems.CLOUDBERRY_SHRUB_ITEM.get(), modTexture("block/cloudberry_shrub_stage2"));
        flatItem(VItems.CHANTARELLE_ITEM.get(), modTexture("block/chantarelle"));
        flatItem(VItems.REED_ITEM.get(), modTexture("block/reed_top"));
        flatItem(VItems.CATTAIL_ITEM.get(), modTexture("block/cattail_top"));
        flatItem(VItems.BLUEBERRY_PIE_ITEM.get(), modTexture("item/blueberry_pie"));

        // Tints used to come from item color handlers, now they're part of the item model
        Identifier pineLeavesModel = ModelLocationUtils.getModelLocation(VBlocks.PINE_LEAVES.get());
        itemModel(VItems.PINE_LEAVES_ITEM.get(), ItemModelUtils.tintedModel(
                pineLeavesModel, ItemModelUtils.constantTint(FoliageColor.FOLIAGE_DEFAULT)));
        Identifier snakeFernModel = ModelLocationUtils.getModelLocation(VBlocks.SNAKE_FERN.get());
        itemModel(VItems.SNAKE_FERN_ITEM.get(),
                ItemModelUtils.tintedModel(snakeFernModel, new GrassColorSource()));
        Identifier stingingNettleModel = flatItemModel(
                VItems.STINGING_NETTLE_ITEM.get(), modTexture("block/stinging_nettle"));
        itemModel(VItems.STINGING_NETTLE_ITEM.get(),
                ItemModelUtils.tintedModel(stingingNettleModel, new GrassColorSource()));
        Identifier heatherModel =
                flatItemModel(VItems.HEATHER_ITEM.get(), modTexture("block/heather"));
        itemModel(VItems.HEATHER_ITEM.get(), ItemModelUtils.tintedModel(
                heatherModel, ItemModelUtils.constantTint(ColorHandler.HEATHER_COLOR)));

        basicItem(VItems.LINGONBERRIES_ITEM.get());
        basicItem(VItems.BLUEBERRIES_ITEM.get());
        basicItem(VItems.CLOUDBERRIES_ITEM.get());

        // Fish reuse the vanilla salmon and cod textures for now
        flatItem(VItems.PIKE.get(), mcTexture("item/salmon"));
        flatItem(VItems.COOKED_PIKE.get(), mcTexture("item/cooked_salmon"));
        flatItem(VItems.PIKE_BUCKET.get(), mcTexture("item/salmon_bucket"));
        flatItem(VItems.ROACH.get(), mcTexture("item/cod"));
        flatItem(VItems.COOKED_ROACH.get(), mcTexture("item/cooked_cod"));
        flatItem(VItems.ROACH_BUCKET.get(), mcTexture("item/cod_bucket"));

        // Since 1.21.5 spawn eggs are plain items with their own texture
        // (textures/item/<name>.png), not a tinted template
        basicItem(VItems.BARROW_WIGHT_SPAWN_EGG.get());
        basicItem(VItems.PIKE_SPAWN_EGG.get());
        basicItem(VItems.ROACH_SPAWN_EGG.get());
        basicItem(VItems.MOOSE_SPAWN_EGG.get());
        basicItem(VItems.TOMTE_SPAWN_EGG.get());
        basicItem(VItems.WILL_O_THE_WISP_SPAWN_EGG.get());
    }

    // Uses the vanilla cake and cake_slice1-6 models with the pie textures, one model per bite
    private void blueberryPie() {
        Block block = VBlocks.BLUEBERRY_PIE.get();
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, modTexture("block/blueberry_pie_side"))
                .put(TextureSlot.BOTTOM, modTexture("block/blueberry_pie_bottom"))
                .put(TextureSlot.TOP, modTexture("block/blueberry_pie_top"))
                .put(TextureSlot.SIDE, modTexture("block/blueberry_pie_side"))
                .put(TextureSlot.INSIDE, modTexture("block/blueberry_pie_inner"));

        PropertyDispatch.C1<MultiVariant, Integer> dispatch =
                PropertyDispatch.initial(CakeBlock.BITES);
        for (int bites : CakeBlock.BITES.getPossibleValues()) {
            String suffix = bites == 0 ? "" : "_slice" + bites;
            ModelTemplate template = new ModelTemplate(
                    Optional.of(mcLoc("block/cake" + suffix)), Optional.of(suffix),
                    TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE,
                    TextureSlot.INSIDE);
            dispatch.select(bites, variant(createModel(template, block, textures)));
        }
        addBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    // Upright, or lying along the X or Z axis
    private void pineLog(Block block, Material endTexture) {
        Material sideTexture = TextureMapping.getBlockTexture(block);
        TextureMapping textures = TextureMapping.column(sideTexture, endTexture);
        Identifier upright = createModel(ModelTemplates.CUBE_COLUMN, block, textures);
        Identifier lying = createModel(ModelTemplates.CUBE_COLUMN_HORIZONTAL, block, textures);
        addBlockState(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(
                block, variant(upright), variant(lying)));
    }

    private void pineStairsAndSlab() {
        TextureMapping planks = TextureMapping.cube(VBlocks.PINE_PLANKS.get());

        Block stairs = VBlocks.PINE_STAIRS.get();
        Identifier innerCorner = createModel(ModelTemplates.STAIRS_INNER, stairs, planks);
        Identifier straight = createModel(ModelTemplates.STAIRS_STRAIGHT, stairs, planks);
        Identifier outerCorner = createModel(ModelTemplates.STAIRS_OUTER, stairs, planks);
        addBlockState(BlockModelGenerators.createStairs(
                stairs, variant(innerCorner), variant(straight), variant(outerCorner)));

        // A double slab looks like a full block of planks
        Block slab = VBlocks.PINE_SLAB.get();
        Identifier bottomHalf = createModel(ModelTemplates.SLAB_BOTTOM, slab, planks);
        Identifier topHalf = createModel(ModelTemplates.SLAB_TOP, slab, planks);
        Identifier doubleSlab = ModelLocationUtils.getModelLocation(VBlocks.PINE_PLANKS.get());
        addBlockState(BlockModelGenerators.createSlab(
                slab, variant(bottomHalf), variant(topHalf), variant(doubleSlab)));
    }

    // One cross model per growth stage, each with its own texture (lingonberry_shrub_stage0-4)
    private void shrub(Block block) {
        PropertyDispatch.C1<MultiVariant, Integer> dispatch =
                PropertyDispatch.initial(BlockStateProperties.AGE_4);
        for (int age : BlockStateProperties.AGE_4.getPossibleValues()) {
            String suffix = "_stage" + age;
            Identifier model = suffixedModel(block, ModelTemplates.CROSS, suffix);
            dispatch.select(age, variant(model));
        }
        addBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    // Two blocks tall, with separate _bottom and _top textures
    private void doublePlant(Block block, ModelTemplate template) {
        Identifier bottom = suffixedModel(block, template, "_bottom");
        Identifier top = suffixedModel(block, template, "_top");
        addBlockState(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        .select(DoubleBlockHalf.LOWER, variant(bottom))
                        .select(DoubleBlockHalf.UPPER, variant(top))));
    }

    // A model named and textured after the block plus a suffix, such as reed_top
    private Identifier suffixedModel(Block block, ModelTemplate template, String suffix) {
        Material texture = TextureMapping.getBlockTexture(block, suffix);
        Identifier modelLocation = ModelLocationUtils.getModelLocation(block, suffix);
        TextureMapping textures = TextureMapping.cross(texture);
        return template.create(modelLocation, textures, blockModels.modelOutput);
    }

    // Side model reaches the next post to the south, so only south and east are drawn (see
    // SlantedFenceBlock)
    private void slantedFence() {
        MultiVariant post = variant(modLoc("block/slanted_fence_post"));
        MultiVariant side = variant(modLoc("block/slanted_fence_side"));
        addBlockState(MultiPartGenerator.multiPart(VBlocks.SLANTED_FENCE.get())
                .with(post)
                .with(BlockModelGenerators.condition().term(FenceBlock.SOUTH, true), side)
                .with(BlockModelGenerators.condition().term(FenceBlock.EAST, true),
                        side.with(BlockModelGenerators.Y_ROT_270)));
    }

    // Attaches to a log. The models face north
    private void pineConk() {
        Identifier young = modLoc("block/pine_conk_small");
        Identifier mature = modLoc("block/pine_conk");

        PropertyDispatch.C2<MultiVariant, Direction, Integer> dispatch =
                PropertyDispatch.initial(PineConkMushroomBlock.FACING, PineConkMushroomBlock.AGE);
        for (Direction facing : PineConkMushroomBlock.FACING.getPossibleValues()) {
            for (int age : PineConkMushroomBlock.AGE.getPossibleValues()) {
                Identifier model = age < PineConkMushroomBlock.MAX_AGE ? young : mature;
                dispatch.select(facing, age, rotatedFromNorth(model, facing));
            }
        }
        addBlockState(
                MultiVariantGenerator.dispatch(VBlocks.PINE_CONK.get()).with(dispatch));
    }

    // Hangs straight down from a ceiling or lies flat against a wall, with a separate texture for
    // the bottom block
    private void beardLichen() {
        Block block = VBlocks.BEARD_LICHEN.get();
        Identifier hanging = createModel(ModelTemplates.CROSS, block, TextureMapping.cross(block));
        Identifier hangingTip = suffixedModel(block, ModelTemplates.CROSS, "_tip");
        Identifier wall = lichenWall("beard_lichen_wall", modTexture("block/beard_lichen"));
        Identifier wallTip =
                lichenWall("beard_lichen_wall_tip", modTexture("block/beard_lichen_tip"));

        PropertyDispatch.C2<MultiVariant, Direction, Boolean> dispatch =
                PropertyDispatch.initial(BeardLichenBlock.FACING, BeardLichenBlock.TIP);
        for (Direction facing : BeardLichenBlock.FACING.getPossibleValues()) {
            for (boolean isTip : BeardLichenBlock.TIP.getPossibleValues()) {
                MultiVariant facingVariant = facing == Direction.UP
                        ? variant(isTip ? hangingTip : hanging)
                        : rotatedFromNorth(isTip ? wallTip : wall, facing);
                dispatch.select(facing, isTip, facingVariant);
            }
        }
        addBlockState(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    // A flat plane just in front of the north face, textured on both sides. The north side's UVs
    // are mirrored so the texture reads the same from both sides
    private Identifier lichenWall(String name, Material texture) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, texture)
                .put(LICHEN, texture);
        return ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(LICHEN)
                .ambientOcclusion(false)
                .element(element -> element
                        .from(0, 0, 0.8f).to(16, 16, 0.8f)
                        .shade(false)
                        .face(Direction.NORTH, face -> face.uvs(16, 0, 0, 16).texture(LICHEN))
                        .face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(LICHEN)))
                .build()
                .create(modLoc("block/" + name), textures, blockModels.modelOutput);
    }

    // Like pink petals: each extra needle layer fills the next quarter of the block
    private void pineNeedles() {
        Block block = VBlocks.PINE_NEEDLES.get();
        Material texture = TextureMapping.getBlockTexture(block);
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, texture)
                .put(TextureSlot.TEXTURE, texture);
        // Corner of each quarter as {x, z} in pixels, in the order the layers fill them
        int[][] quarterCorners = {{0, 0}, {0, 8}, {8, 8}, {8, 0}};
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);

        for (int quarterIdx = 0; quarterIdx < quarterCorners.length; quarterIdx++) {
            int x = quarterCorners[quarterIdx][0];
            int z = quarterCorners[quarterIdx][1];
            // A flat 8x8 square a quarter pixel above the ground, visible from above and below
            Identifier model = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(TextureSlot.PARTICLE)
                    .requiredTextureSlot(TextureSlot.TEXTURE)
                    .ambientOcclusion(false)
                    .element(element -> element
                            .from(x, 0.25f, z).to(x + 8, 0.25f, z + 8)
                            .face(Direction.UP, face -> face
                                    .uvs(x, z, x + 8, z + 8)
                                    .texture(TextureSlot.TEXTURE))
                            .face(Direction.DOWN, face -> face
                                    .uvs(x, z + 8, x + 8, z)
                                    .texture(TextureSlot.TEXTURE)))
                    .build()
                    .create(modLoc("block/pine_needles_" + (quarterIdx + 1)), textures,
                            blockModels.modelOutput);

            // Quarter n shows once there are at least n + 1 needle layers. The first quarter
            // always shows, and the game warns about a condition that lists every amount, so it
            // gets no amount condition
            int minAmount = quarterIdx + 1;
            Integer[] largerAmounts = PineNeedlesBlock.AMOUNT.getPossibleValues().stream()
                    .filter(amount -> amount > minAmount)
                    .toArray(Integer[]::new);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                ConditionBuilder condition =
                        BlockModelGenerators.condition().term(PineNeedlesBlock.FACING, facing);
                if (minAmount > 1) {
                    condition.term(PineNeedlesBlock.AMOUNT, minAmount, largerAmounts);
                }
                generator.with(condition, rotatedFromNorth(model, facing));
            }
        }
        addBlockState(generator);
    }

    // Uses an OBJ model exported from a 3D modelling program, loaded by NeoForge's OBJ loader
    private void snakeFern() {
        Block block = VBlocks.SNAKE_FERN.get();
        Material texture = TextureMapping.getBlockTexture(block);
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, texture)
                .put(TextureSlot.TEXTURE, texture);
        Identifier model = ExtendedModelTemplateBuilder.builder()
                .parent(mcLoc("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.TEXTURE)
                .customLoader(ObjModelBuilder::new, obj -> obj
                        .modelLocation(modLoc("models/block/snakefern.obj"))
                        .automaticCulling(false)
                        .emissiveAmbient(false))
                .ambientOcclusion(false)
                .build()
                .create(block, textures, blockModels.modelOutput);
        simpleBlock(block, model);
    }

    private void crossBlock(Block block, ModelTemplate template) {
        simpleBlock(block, createModel(template, block, TextureMapping.cross(block)));
    }

    // A block whose every state uses the same model
    private void simpleBlock(Block block, Identifier model) {
        addBlockState(BlockModelGenerators.createSimpleBlock(block, variant(model)));
    }

    private void addBlockState(BlockModelDefinitionGenerator blockState) {
        blockModels.blockStateOutput.accept(blockState);
    }

    // Writes the block's model file from the template and returns the model's location
    private Identifier createModel(ModelTemplate template, Block block, TextureMapping textures) {
        return template.create(block, textures, blockModels.modelOutput);
    }

    // An item whose texture is textures/item/<item name>.png
    private void basicItem(Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void flatItem(Item item, Material texture) {
        itemModel(item, ItemModelUtils.plainModel(flatItemModel(item, texture)));
    }

    private Identifier flatItemModel(Item item, Material texture) {
        TextureMapping textures = TextureMapping.layer0(texture);
        return ModelTemplates.FLAT_ITEM.create(item, textures, itemModels.modelOutput);
    }

    private void itemModel(Item item, ItemModel.Unbaked model) {
        itemModels.itemModelOutput.accept(item, model);
    }

    private static MultiVariant variant(Identifier model) {
        return BlockModelGenerators.plainVariant(model);
    }

    // Rotates a model authored facing north to face the given direction
    private static MultiVariant rotatedFromNorth(Identifier model, Direction facing) {
        return variant(model).with(yRotation(facing));
    }

    private static VariantMutator yRotation(Direction facing) {
        return switch (facing) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
    }

    private static Material modTexture(String path) {
        return new Material(modLoc(path));
    }

    private static Material mcTexture(String path) {
        return new Material(mcLoc(path));
    }

    private static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(Vildmark.MODID, path);
    }

    private static Identifier mcLoc(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
