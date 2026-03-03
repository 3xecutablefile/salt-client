/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lightfreecs.client.MatchContext;
import org.lightfreecs.client.StatsManager;
import org.lightfreecs.client.VersionManager;
import org.lightfreecs.client.gui.HowToPlayScreen;
import org.lightfreecs.client.gui.LeaderboardScreen;
import org.lightfreecs.client.gui.PlayerStatsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static final Identifier TROPHY_ICON = Identifier.of("mcpvp", "textures/item/trophy.png");

    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitHead(CallbackInfo ci) {
        VersionManager.checkVersion();
        StatsManager.syncWithServer();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        int x = 10, y = 50;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Leaderboard"), button -> this.client.setScreen(new LeaderboardScreen(this))).dimensions(x + 20, y, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("My Stats"), button -> { if (this.client.player != null) this.client.setScreen(new PlayerStatsScreen(this, this.client.player.getName().getString())); }).dimensions(x + 20, y + 22, 80, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("How to Play"), button -> this.client.setScreen(new HowToPlayScreen(this))).dimensions(x + 20, y + 44, 80, 20).build());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int x = 10, y = 10;
        MatchContext.KitType kit = MatchContext.currentKit != null ? MatchContext.currentKit : (MatchContext.lastMatchKit != null ? MatchContext.lastMatchKit : MatchContext.KitType.SWORD);
        int elo = MatchContext.kitElo.getOrDefault(kit, 0);
        String rank = MatchContext.getRankDisplay(elo, 101);
        context.drawText(client.textRenderer, Text.literal("Ranked: " + kit.apiName), x, y, 0xFFFFFF, true);
        context.drawText(client.textRenderer, Text.literal("ELO: " + elo), x, y + 10, 0xFFFFFF, true);
        context.drawText(client.textRenderer, Text.literal("Rank: " + rank), x, y + 20, 0xFFFFFF, true);
    }
}
