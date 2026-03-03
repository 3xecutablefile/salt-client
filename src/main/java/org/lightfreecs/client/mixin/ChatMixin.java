/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.MatchContext;
import org.lightfreecs.client.StatsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(ChatHud.class)
public class ChatMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"))
    private void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        String[] parts;
        if (!MatchContext.isAllowedServer()) return;
        String content = message.getString();
        String playerName = MinecraftClient.getInstance().player.getName().getString();
        
        if ((content.contains("Welcome to ") || content.contains("Welcome back to ")) && MatchContext.pendingAbandonmentPenalty && MatchContext.abandonedKit != null) {
            MatchContext.updateElo(MatchContext.abandonedKit, false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§c§l[RankedMod] §fYou left your last match early! -15 ELO penalty applied.").formatted(Formatting.RED), false);
            MatchContext.pendingAbandonmentPenalty = false;
            MatchContext.abandonedKit = null;
            StatsManager.save();
        }
        if (content.contains("Duel started") || content.contains("Match started")) {
            MatchContext.isMatchActive = true;
        }
        if ((content.contains("was slain by") || content.contains("was shot by") || content.contains("was blown up by")) && (parts = content.split(" ")).length >= 4) {
            String victim = parts[0];
            String killer = parts[parts.length - 1];
            if (killer.equals(playerName)) MatchContext.opponentName = victim;
            else if (victim.equals(playerName)) MatchContext.opponentName = killer;
        }
        if (content.contains("won the match!") || content.contains("Winner:") || content.contains("Match Completed")) {
            boolean won = content.contains(playerName);
            if (MatchContext.inMatch && MatchContext.isRanked) {
                MatchContext.updateElo(MatchContext.currentKit != null ? MatchContext.currentKit : MatchContext.currentQueuedKit, won);
                MatchContext.reset(true);
            }
            MatchContext.isMatchActive = false;
        }
        if (content.contains("resigned")) {
            if (MatchContext.inMatch && MatchContext.isRanked) {
                boolean iResigned = content.contains(playerName);
                MatchContext.updateElo(MatchContext.currentKit != null ? MatchContext.currentKit : MatchContext.currentQueuedKit, !iResigned);
                MatchContext.reset(true);
            }
            MatchContext.isMatchActive = false;
        }
        if (content.contains("HP") && content.contains("/")) {
            try {
                for (String part : parts = content.split(" ")) {
                    if (!part.contains("/")) continue;
                    MatchContext.lastRoundHealth = Float.parseFloat(part.split("/")[0]);
                    break;
                }
            } catch (Exception ignored) {}
        }
    }
}
