/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.MatchContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    private boolean hasSentOpenMessage = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Text title = screen.getTitle();
        if (title != null && title.getString().contains("Queue Duels") && !this.hasSentOpenMessage) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§6§l[RankedMod] §fQueue menu opened!").formatted(Formatting.GOLD), false);
            }
            this.hasSentOpenMessage = true;
        }
    }

    @Inject(method = "onMouseClick", at = @At("HEAD"))
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot == null) return;
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Text title = screen.getTitle();
        ItemStack stack = slot.getStack();
        if (title != null && title.getString().contains("Queue Duels") && stack != null && !stack.isEmpty()) {
            MatchContext.KitType kit = MatchContext.KitType.fromItem(stack.getItem());
            if (kit != null) {
                MatchContext.currentQueuedKit = kit;
                MatchContext.kitSelectedInMenu = true;
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§6§l[RankedMod] §fQueued for: §a" + kit.apiName).formatted(Formatting.GOLD), false);
                }
            }
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        Text title = screen.getTitle();
        if (title != null && title.getString().contains("Queue Duels")) {
            if (!MatchContext.kitSelectedInMenu) {
                MatchContext.currentQueuedKit = null;
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c§l[RankedMod] §fQueue closed without selecting a kit. Ranked tracking disabled.").formatted(Formatting.RED), false);
                }
            }
            this.hasSentOpenMessage = false;
        }
    }
}
