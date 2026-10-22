package io.github.jesperandersson.vildmark.mixin;

import io.github.jesperandersson.vildmark.client.ShaderBlockAliases;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets shader packs animate Vildmark plants by giving them the block IDs of matching vanilla
 * plants. Skipped when Iris isn't installed, and does nothing if Iris changes the method.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping", remap = false)
public abstract class IrisBlockMaterialMappingMixin {
    @Inject(method = "createBlockStateIdMap", at = @At("RETURN"), require = 0)
    private static void vildmark$aliasPlants(
            CallbackInfoReturnable<Object2IntMap<BlockState>> callbackInfo) {
        ShaderBlockAliases.alias(callbackInfo.getReturnValue());
    }
}
