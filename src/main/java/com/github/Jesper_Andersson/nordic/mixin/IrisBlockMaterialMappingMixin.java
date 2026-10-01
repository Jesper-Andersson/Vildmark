package com.github.Jesper_Andersson.nordic.mixin;

import com.github.Jesper_Andersson.nordic.client.ShaderBlockAliases;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets shader packs animate Nordic plants by giving them the block IDs of matching vanilla plants.
 * Skipped when Iris isn't installed, and does nothing if Iris changes the method.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping", remap = false)
public abstract class IrisBlockMaterialMappingMixin {
    @Inject(method = "createBlockStateIdMap", at = @At("RETURN"), require = 0)
    private static void nordic$aliasPlants(CallbackInfoReturnable<Object2IntMap<BlockState>> cir) {
        ShaderBlockAliases.alias(cir.getReturnValue());
    }
}
