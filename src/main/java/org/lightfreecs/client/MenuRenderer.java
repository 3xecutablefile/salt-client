/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value = EnvType.CLIENT)
public class MenuRenderer {
    public static void renderMatchInfo(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        int x = 10, y = 10;
        MatchContext.KitType kit = MatchContext.currentKit != null ? MatchContext.currentKit : (MatchContext.lastMatchKit != null ? MatchContext.lastMatchKit : MatchContext.KitType.SWORD);
        int elo = MatchContext.kitElo.getOrDefault(kit, 0);
        Formatting color = MatchContext.getEloColor(elo, 101);
        if (kit.customTexture != null) {
            context.drawTexture(id -> RenderLayer.getGuiTextured(id), kit.customTexture, x, y, 0, 0, 16, 16, 16, 16);
        } else {
            context.drawItem(new ItemStack(kit.icon), x, y);
        }
        context.drawText(client.textRenderer, Text.literal(kit.apiName).formatted(Formatting.WHITE), x + 20, y + 4, 0xFFFFFF, true);
        context.drawText(client.textRenderer, Text.literal("ELO: ").formatted(Formatting.RED).append(Text.literal(String.valueOf(elo)).formatted(color)), x, y += 20, 0xFFFFFF, true);
        String rank = MatchContext.getRankDisplay(elo, 101);
        context.drawText(client.textRenderer, Text.literal("Rank: ").formatted(Formatting.RED).append(Text.literal(rank).formatted(color)), x, y += 10, 0xFFFFFF, true);
    }
}
