package io.github.jesperandersson.vildmark;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;


/**
 * Block tints: which blocks are colored by the biome's grass or foliage color, or by a fixed color.
 */
@EventBusSubscriber(modid = Vildmark.MODID, value = Dist.CLIENT)
public class ColorHandler {
    // Placeholder heather is the grey short grass texture with a fixed purple tint, so it stands
    // out from grass
    public static final int HEATHER_COLOR = 0xA0669E;

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.foliage()), VBlocks.PINE_LEAVES.get());
        // Placeholder plants reuse the grey vanilla grass textures, so they need a grass tint
        event.register(List.of(BlockTintSources.grass()),
                VBlocks.STINGING_NETTLE.get(),
                VBlocks.TALL_STINGING_NETTLE.get(),
                VBlocks.SNAKE_FERN.get());
        // opaque() adds the alpha channel, which the 0xRRGGBB constant leaves at 0
        BlockTintSource heatherTint = BlockTintSources.constant(ARGB.opaque(HEATHER_COLOR));
        event.register(List.of(heatherTint), VBlocks.HEATHER.get());
    }
    // Item tints are part of the item models since 1.21.4, see VModelProvider
}
