package com.github.Jesper_Andersson.vildmark.client.particle;

import com.github.Jesper_Andersson.vildmark.Vildmark;
import com.github.Jesper_Andersson.vildmark.core.particles.VParticleTypes;
import net.minecraft.client.particle.FlameParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Vildmark.MODID, value = Dist.CLIENT)
public class VParticleProviders {
    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(VParticleTypes.WISP_FLAME.get(),
                FlameParticle.SmallFlameProvider::new);
    }
}
