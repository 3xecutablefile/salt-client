package com.saltclient.mixin;

import com.saltclient.SaltClient;
import com.saltclient.module.impl.visual.MotionBlurModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MotionBlurModule mb = (MotionBlurModule) SaltClient.MODULES.byId("motionblur").orElse(null);
        if (mb != null && mb.isEnabled()) {
            mb.applyBlur();
        }
    }
}
