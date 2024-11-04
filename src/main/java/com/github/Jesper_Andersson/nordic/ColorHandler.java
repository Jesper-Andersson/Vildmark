package com.github.Jesper_Andersson.nordic;

import com.github.Jesper_Andersson.nordic.world.item.NItems;
import com.github.Jesper_Andersson.nordic.world.level.block.NBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.renderer.BiomeColors;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandler {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.getBlockColors().register(
                ((blockState, blockAndTintGetter, blockPos, tintIndex) -> {
                    return blockAndTintGetter != null && blockPos != null ?
                            BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos) :
                            FoliageColor.getDefaultColor();

                }),
                NBlocks.PINE_LEAVES.get()
        );

    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.getItemColors().register(
                (itemStack, tintIndex) -> {
                    return FoliageColor.getDefaultColor();
                },
                NItems.PINE_LEAVES_ITEM.get()
        );
    }
}

