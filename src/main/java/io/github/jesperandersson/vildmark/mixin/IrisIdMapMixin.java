package io.github.jesperandersson.vildmark.mixin;

import io.github.jesperandersson.vildmark.client.ShaderEntityAliases;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Properties;

/**
 * Lets shader packs light Vildmark entities by giving them the entity IDs of matching vanilla entities.
 * Skipped when Iris isn't installed, and does nothing if Iris changes the method.
 */
/*
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shaderpack.IdMap", remap = false)
public abstract class IrisIdMapMixin {
    @Inject(method = "parseEntityIdMap", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vildmark$aliasEntities(Properties properties, CallbackInfoReturnable<Object2IntMap<NamespacedId>> cir) {
        cir.setReturnValue(ShaderEntityAliases.alias(cir.getReturnValue()));
    }
}

 */
