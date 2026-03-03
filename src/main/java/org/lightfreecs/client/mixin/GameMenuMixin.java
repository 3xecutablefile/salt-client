/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.MatchContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public class GameMenuMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int x = 10, y = 10;
        MatchContext.KitType kit = MatchContext.currentKit != null ? MatchContext.currentKit : (MatchContext.lastMatchKit != null ? MatchContext.lastMatchKit : MatchContext.KitType.SWORD);
        int elo = MatchContext.kitElo.getOrDefault(kit, 0);
        String rank = MatchContext.getRankDisplay(elo, 101);
        Formatting color = MatchContext.getEloColor(elo, 101);
        context.drawText(client.textRenderer, Text.literal("Current/Last Kit: ").formatted(Formatting.ITALIC).append(Text.literal(kit.apiName).formatted(Formatting.WHITE)), x, y, 0xFFFFFF, true);
        context.drawText(client.textRenderer, Text.literal("ELO: ").formatted(Formatting.ITALIC).append(Text.literal(String.valueOf(elo)).formatted(color)), x, y + 10, 0xFFFFFF, true);
        context.drawText(client.textRenderer, Text.literal("Rank: ").formatted(Formatting.ITALIC).append(Text.literal(rank).formatted(color)), x, y + 20, 0xFFFFFF, true);
    }
}
