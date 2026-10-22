package io.github.jesperandersson.vildmark.client;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Gives Vildmark plants the shader block IDs of matching vanilla plants, so shader packs that only
 * list vanilla blocks in their block.properties still animate them (leaves sway, foliage waves).
 */
public final class ShaderBlockAliases {
    // Vildmark block -> vanilla block whose shader ID it borrows
    private static final Map<Supplier<? extends Block>, Block> ALIASES = Map.ofEntries(
            Map.entry(VBlocks.PINE_LEAVES, Blocks.OAK_LEAVES),
            Map.entry(VBlocks.PINE_SAPLING, Blocks.OAK_SAPLING),
            Map.entry(VBlocks.REED, Blocks.TALL_GRASS),
            Map.entry(VBlocks.CATTAIL, Blocks.TALL_GRASS),
            Map.entry(VBlocks.LINGONBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH),
            Map.entry(VBlocks.BLUEBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH),
            Map.entry(VBlocks.CLOUDBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH),
            Map.entry(VBlocks.COTTON_GRASS, Blocks.SHORT_GRASS),
            Map.entry(VBlocks.SUNDEW, Blocks.SHORT_GRASS),
            Map.entry(VBlocks.STINGING_NETTLE, Blocks.SHORT_GRASS),
            Map.entry(VBlocks.TALL_STINGING_NETTLE, Blocks.TALL_GRASS),
            Map.entry(VBlocks.SNAKE_FERN, Blocks.FERN),
            Map.entry(VBlocks.HEATHER, Blocks.SHORT_GRASS),
            Map.entry(VBlocks.BEARD_LICHEN, Blocks.HANGING_ROOTS)
    );

    private ShaderBlockAliases() {}

    /**
     * Adds IDs for Vildmark states the pack didn't assign itself, copied from the matching vanilla
     * template states.
     */
    public static void alias(Object2IntMap<BlockState> shaderIds) {
        ALIASES.forEach((block, template) -> {
            for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
                if (shaderIds.containsKey(state)) {
                    continue;
                }
                // The map returns -1 for states the pack doesn't list
                int templateId = shaderIds.getInt(templateState(state, template));
                if (templateId != -1) {
                    shaderIds.put(state, templateId);
                }
            }
        });
    }

    /**
     * Returns the template's default state, with every property the two blocks share (same name
     * and value) copied from the given state. A waterlogged pine leaf maps to a waterlogged oak
     * leaf, for example.
     */
    private static BlockState templateState(BlockState state, Block template) {
        BlockState templateState = template.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            Property<?> templateProperty =
                    template.getStateDefinition().getProperty(property.getName());
            if (templateProperty != null) {
                String valueName = valueName(state, property);
                templateState = withValueNamed(templateState, templateProperty, valueName);
            }
        }
        return templateState;
    }

    private static <T extends Comparable<T>> String valueName(
            BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    // Unchanged if the property has no value with that name
    private static <T extends Comparable<T>> BlockState withValueNamed(
            BlockState state, Property<T> property, String valueName) {
        return property.getValue(valueName)
                .map(value -> state.setValue(property, value))
                .orElse(state);
    }
}
