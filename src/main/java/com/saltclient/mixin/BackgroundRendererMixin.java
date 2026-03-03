package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public final class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void salt_applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("fogremover")) {
            return;
        }
        if (SaltClient.MODULES.isEnabled("clearwater") && camera.getSubmersionType() == CameraSubmersionType.WATER) {
            return;
        }
    }
}
