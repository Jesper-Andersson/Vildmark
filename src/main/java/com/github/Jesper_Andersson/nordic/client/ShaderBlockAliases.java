package com.github.Jesper_Andersson.nordic.client;

import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Gives Nordic plants the shader block IDs of matching vanilla plants, so shader packs that only list vanilla
 * blocks in their block.properties still animate them (leaves sway, foliage waves).
 */
public final class ShaderBlockAliases {
    // Nordic block -> vanilla block whose shader ID it borrows
    private static final Map<Supplier<? extends Block>, Block> ALIASES = Map.of(
            NBlocks.PINE_LEAVES, Blocks.OAK_LEAVES,
            NBlocks.PINE_SAPLING, Blocks.OAK_SAPLING,
            NBlocks.REED, Blocks.TALL_GRASS,
            NBlocks.LINGONBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH,
            NBlocks.BLUEBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH,
            NBlocks.CLOUDBERRY_SHRUB, Blocks.SWEET_BERRY_BUSH,
            NBlocks.COTTON_GRASS, Blocks.SHORT_GRASS,
            NBlocks.SUNDEW, Blocks.SHORT_GRASS
    );

    private ShaderBlockAliases() {}

    /** Adds IDs for Nordic states the pack didn't assign itself, copied from the vanilla template states. */
    public static void alias(Object2IntMap<BlockState> map) {
        ALIASES.forEach((block, template) -> {
            for (BlockState state : block.get().getStateDefinition().getPossibleStates()) {
                if (map.containsKey(state)) continue;
                int id = map.getInt(templateState(state, template));
                if (id != -1) map.put(state, id);
            }
        });
    }

    /** Template's default state with every property both blocks share (by name and value) copied over. */
    private static BlockState templateState(BlockState state, Block template) {
        BlockState result = template.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            Property<?> target = template.getStateDefinition().getProperty(property.getName());
            if (target != null) result = copy(result, target, valueName(state, property));
        }
        return result;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState result, Property<T> target, String value) {
        return target.getValue(value).map(v -> result.setValue(target, v)).orElse(result);
    }
}
