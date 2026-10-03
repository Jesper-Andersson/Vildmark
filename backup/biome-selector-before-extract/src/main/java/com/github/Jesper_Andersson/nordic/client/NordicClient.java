package com.github.Jesper_Andersson.nordic.client;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.client.gui.BiomeToggleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Nordic.MODID, dist = Dist.CLIENT)
public class NordicClient {
    public NordicClient(ModContainer container) {
        // Config button in the Mods list
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> new BiomeToggleScreen(parent));
    }
}
