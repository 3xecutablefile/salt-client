/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.lightfreecs.client.MatchContext;
import org.lightfreecs.client.StatsManager;
import org.lightfreecs.client.WorldManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class WorldCaptureMixin {
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void onSetWorld(ClientWorld world, CallbackInfo ci) {
        if (world == null) {
            if (MatchContext.inMatch && MatchContext.isRanked) {
                MatchContext.reset(false);
                StatsManager.save();
            }
        } else {
            WorldManager.PERSISTENT_WORLD = world;
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        if (MatchContext.inMatch && MatchContext.isRanked) {
            MatchContext.reset(false);
            StatsManager.save();
        }
    }
}
