/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.EloBarRenderer;
import org.lightfreecs.client.MatchContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        EloBarRenderer.render(context);
        MatchContext.KitType displayKit = MatchContext.currentKit != null ? MatchContext.currentKit : MatchContext.currentQueuedKit;
        if (displayKit != null) {
            int x = 5, y = 5;
            int elo = MatchContext.kitElo.getOrDefault(displayKit, 0);
            int placements = MatchContext.placementMatches.getOrDefault(displayKit, 0);
            Formatting color = MatchContext.getEloColor(elo, 101);
            if (displayKit.customTexture != null) {
                context.drawTexture(id -> RenderLayer.getGuiTextured(id), displayKit.customTexture, x, y, 0, 0, 16, 16, 16, 16);
            }
            context.drawTextWithShadow(client.textRenderer, Text.literal(displayKit.apiName).formatted(Formatting.WHITE, Formatting.BOLD), x + 20, y + 4, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, Text.literal("ELO: " + elo).formatted(color), x + 20, y + 14, 0xFFFFFF);
            if (placements > 0 && placements < 10) {
                context.drawTextWithShadow(client.textRenderer, Text.literal("Placement: " + placements + "/10").formatted(Formatting.GOLD), x + 20, y + 24, 0xFFFFFF);
            }
        }
    }
}
