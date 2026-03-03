/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.CosmeticManager;
import org.lightfreecs.client.MatchContext;
import org.lightfreecs.client.StatsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Environment(value = EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @ModifyArgs(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabel(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void onRenderLabel(Args args) {
        EntityRenderState state = args.get(0);
        Text text = args.get(1);
        if (state instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState playerState = (PlayerEntityRenderState) state;
            String name = playerState.name;
            if (name == null) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            String localName = mc.player.getName().getString();
            String title = "";
            int elo;
            if (name.equals(localName)) {
                elo = MatchContext.currentKit != null ? MatchContext.kitElo.getOrDefault(MatchContext.currentKit, 0) : MatchContext.kitElo.values().stream().max(Integer::compare).orElse(0);
                title = CosmeticManager.selectedTitle;
            } else {
                StatsManager.fetchPlayerElo(name);
                elo = MatchContext.playerEloByName.getOrDefault(name, 0);
            }
            if (title != null && !title.isEmpty()) {
                args.set(1, Text.literal("[" + title + "] ").formatted(Formatting.GOLD).append(text));
            }
        }
    }
}
