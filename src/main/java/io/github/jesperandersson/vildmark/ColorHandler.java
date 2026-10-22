package io.github.jesperandersson.vildmark;

import io.github.jesperandersson.vildmark.world.level.block.VBlocks;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.BiomeColors;


@EventBusSubscriber(modid = io.github.jesperandersson.vildmark.Vildmark.MODID, value = Dist.CLIENT)
public class ColorHandler {
    // Placeholder heather is the grey short grass texture with a fixed purple tint, so it stands out from grass
    public static final int HEATHER_COLOR = 0xA0669E;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                ((blockState, blockAndTintGetter, blockPos, tintIndex) -> {
                    return blockAndTintGetter != null && blockPos != null ?
                            BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos) :
                            FoliageColor.FOLIAGE_DEFAULT;

                }),
                VBlocks.PINE_LEAVES.get()
        );
        // Placeholder plants reuse the grey vanilla grass textures, so they need a grass tint
        event.register(
                (blockState, blockAndTintGetter, blockPos, tintIndex) ->
                        blockAndTintGetter != null && blockPos != null ?
                                BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos) :
                                GrassColor.getDefaultColor(),
                VBlocks.STINGING_NETTLE.get(),
                VBlocks.TALL_STINGING_NETTLE.get(),
                VBlocks.SNAKE_FERN.get()
        );
        event.register((blockState, blockAndTintGetter, blockPos, tintIndex) -> HEATHER_COLOR,
                VBlocks.HEATHER.get());

    }
    // Item tints are part of the item models since 1.21.4, see VModelProvider
}
