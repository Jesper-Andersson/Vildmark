package com.github.Jesper_Andersson.nordic.client.particle;

import com.github.Jesper_Andersson.nordic.Nordic;
import com.github.Jesper_Andersson.nordic.core.particles.NParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Nordic.MODID, value = Dist.CLIENT)
public class NParticleProviders {
    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(NParticleTypes.WISP_FLAME.get(),
                FlameParticle.SmallFlameProvider::new);
    }
}
