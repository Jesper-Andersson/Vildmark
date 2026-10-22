package io.github.jesperandersson.vildmark.mixin;

import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomState.class)
public interface RandomStateAccessor {
    @Mutable
    @Accessor("router")
    void vildmark$setRouter(NoiseRouter router);
}
