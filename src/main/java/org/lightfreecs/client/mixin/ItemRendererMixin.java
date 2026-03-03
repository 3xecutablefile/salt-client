/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.lightfreecs.client.CosmeticManager;
import org.lightfreecs.client.MatchContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void onRenderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (stack.isEmpty()) return;
        String kitName = null;
        if (stack.isOf(Items.NETHERITE_SWORD)) kitName = "Netherite OP";
        else if (stack.isOf(Items.DIAMOND_SWORD)) kitName = MatchContext.currentKit != null ? MatchContext.currentKit.apiName : "Sword";
        if (kitName == null) return;
        String playerName = entity != null ? entity.getName().getString() : (MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getName().getString() : null);
        if (playerName == null) return;
        String cosmetic = "default";
        if (playerName.equals(MinecraftClient.getInstance().player.getName().getString())) {
            cosmetic = CosmeticManager.getSelectedCosmetic(kitName);
        } else {
            var otherPlayerCosmetics = org.lightfreecs.client.StatsManager.playerKitCosmetics.get(playerName);
            if (otherPlayerCosmetics != null) cosmetic = otherPlayerCosmetics.getOrDefault(kitName, "default");
        }
        if (!cosmetic.equals("default")) {
            Identifier modelId = Identifier.of("mcpvp", "item/" + cosmetic + "_sword_cosmetic");
            BakedModel customModel = MinecraftClient.getInstance().getBakedModelManager().getModel(new ModelIdentifier(modelId, "inventory"));
            if (customModel != null) {
                // Custom cosmetic rendering disabled for 1.21.4 - renderBakedItemModel is private
                // matrices.push();
                // customModel.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
                // matrices.translate(-0.5f, -0.5f, -0.5f);
                // ItemRenderer.renderBakedItemModel(customModel, new int[]{light}, overlay, 0, matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()));
                // matrices.pop();
                // ci.cancel();
            }
        }
    }
}
