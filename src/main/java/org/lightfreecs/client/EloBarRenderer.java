/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value = EnvType.CLIENT)
public class EloBarRenderer {
    private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = Identifier.of("hud/experience_bar_background");
    private static boolean visible = false;
    private static long startTime = 0L;
    private static final long DURATION = 5000L;
    private static int oldElo = 0, newElo = 0, baseChange = 0, bonus = 0, placements = 0;

    public static void show(int oldE, int newE, int base, int bon, int p) {
        oldElo = oldE; newElo = newE; baseChange = base; bonus = bon; placements = p;
        startTime = System.currentTimeMillis(); visible = true;
    }

    public static void render(DrawContext context) {
        if (!visible) return;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > DURATION) { visible = false; return; }
        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2, centerY = 20;
        float animationProgress = elapsed > 1000L ? MathHelper.clamp((float)(elapsed - 1000L) / 2000.0f, 0.0f, 1.0f) : 0.0f;
        String eloText; String changeText; float barProgress;
        if (placements < 10) {
            barProgress = MathHelper.lerp(animationProgress, (float)(placements - 1) / 10.0f, (float)placements / 10.0f);
            eloText = "Placement " + placements + "/10"; changeText = "§e+1 Match";
        } else if (placements == 10 && oldElo == 0) {
            int currentDisplayElo = MathHelper.lerp(animationProgress, 0, newElo);
            int nextRankElo = getNextRankElo(currentDisplayElo), prevRankElo = getPrevRankElo(currentDisplayElo);
            barProgress = nextRankElo > prevRankElo ? (float)(currentDisplayElo - prevRankElo) / (float)(nextRankElo - prevRankElo) : 1.0f;
            eloText = "Placed: " + currentDisplayElo; changeText = "§a+" + newElo;
        } else {
            int currentDisplayElo = MathHelper.lerp(animationProgress, oldElo, newElo);
            int nextRankElo = getNextRankElo(currentDisplayElo), prevRankElo = getPrevRankElo(currentDisplayElo);
            barProgress = nextRankElo > prevRankElo ? (float)(currentDisplayElo - prevRankElo) / (float)(nextRankElo - prevRankElo) : 1.0f;
            eloText = String.valueOf(currentDisplayElo);
            changeText = (baseChange >= 0 ? "§a+" : "§c") + baseChange + (bonus > 0 ? " §6(+" + bonus + " Bonus)" : "");
        }
        barProgress = MathHelper.clamp(barProgress, 0.0f, 1.0f);
        int barWidth = 182, barHeight = 5, barX = centerX - barWidth / 2, barY = centerY;
        context.drawTexture(RenderLayer::getGuiTextured, EXPERIENCE_BAR_BACKGROUND_TEXTURE, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);
        if (barProgress > 0.0f) {
            int filledWidth = (int)(barProgress * barWidth), color = placements < 10 ? 0xFFFDFF04 : 0xFF55FF55;
            context.fill(barX, barY, barX + filledWidth, barY + barHeight, color);
            context.fill(barX, barY, barX + filledWidth, barY + 1, 0x89000000);
        }
        context.drawText(client.textRenderer, "§b" + eloText, centerX, barY - 10, 0xFFFFFF, true);
        context.drawText(client.textRenderer, changeText, barX + barWidth + 5, barY - 2, 0xFFFFFF, true);
    }

    private static int getNextRankElo(int elo) {
        if (elo < 400) return 400; if (elo < 800) return 800; if (elo < 1200) return 1200;
        if (elo < 1600) return 1600; if (elo < 2000) return 2000; if (elo < 2400) return 2400;
        if (elo < 2800) return 2800; if (elo < 3400) return 3400; return Integer.MAX_VALUE;
    }
    private static int getPrevRankElo(int elo) {
        if (elo >= 3400) return 3400; if (elo >= 2800) return 2800; if (elo >= 2400) return 2400;
        if (elo >= 2000) return 2000; if (elo >= 1600) return 1600; if (elo >= 1200) return 1200;
        if (elo >= 800) return 800; if (elo >= 400) return 400; return 0;
    }
}
