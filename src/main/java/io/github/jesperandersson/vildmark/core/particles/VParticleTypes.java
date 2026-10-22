package io.github.jesperandersson.vildmark.core.particles;

import io.github.jesperandersson.vildmark.Vildmark;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VParticleTypes  {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Vildmark.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WISP_FLAME =
            PARTICLE_TYPES.register("wisp_flame", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
