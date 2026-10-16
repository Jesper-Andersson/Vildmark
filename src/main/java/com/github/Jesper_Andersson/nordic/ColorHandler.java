package com.github.Jesper_Andersson.nordic;

import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.BiomeColors;


@EventBusSubscriber(modid = Nordic.MODID, value = Dist.CLIENT)
public class ColorHandler {
    // Placeholder heather is the grey short grass texture with a fixed purple tint, so it stands out from grass
    private static final int HEATHER_COLOR = 0xA0669E;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                ((blockState, blockAndTintGetter, blockPos, tintIndex) -> {
                    return blockAndTintGetter != null && blockPos != null ?
                            BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos) :
                            FoliageColor.getDefaultColor();

                }),
                NBlocks.PINE_LEAVES.get()
        );
        // Placeholder plants reuse the grey vanilla grass textures, so they need a grass tint
        event.register(
                (blockState, blockAndTintGetter, blockPos, tintIndex) ->
                        blockAndTintGetter != null && blockPos != null ?
                                BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos) :
                                GrassColor.getDefaultColor(),
                NBlocks.STINGING_NETTLE.get(),
                NBlocks.TALL_STINGING_NETTLE.get(),
                NBlocks.SNAKE_FERN.get()
        );
        event.register((blockState, blockAndTintGetter, blockPos, tintIndex) -> HEATHER_COLOR,
                NBlocks.HEATHER.get());

    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (itemStack, tintIndex) -> {
                    return FoliageColor.getDefaultColor();
                },
                NItems.PINE_LEAVES_ITEM.get()
        );
        event.register(
                (itemStack, tintIndex) -> GrassColor.getDefaultColor(),
                NItems.STINGING_NETTLE_ITEM.get(),
                NItems.SNAKE_FERN_ITEM.get()
        );
        event.register((itemStack, tintIndex) -> HEATHER_COLOR, NItems.HEATHER_ITEM.get());
    }
}
