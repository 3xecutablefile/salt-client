package com.saltclient.module.impl.visual;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.joml.Matrix4f;

public final class EntityHighlightModule extends Module {
    private final BoolSetting players;
    private final BoolSetting mobs;
    private final BoolSetting items;
    private final EnumSetting<HighlightStyle> style;

    public EntityHighlightModule() {
        super("entityhighlight", "EntityHighlight", "Highlight targeted entities.", ModuleCategory.VISUAL, true);
        this.players = addSetting(new BoolSetting("players", "Players", "Highlight players.", true));
        this.mobs = addSetting(new BoolSetting("mobs", "Mobs", "Highlight mobs.", true));
        this.items = addSetting(new BoolSetting("items", "Items", "Highlight items.", false));
        this.style = addSetting(new EnumSetting<>("style", "Style", "Highlight style.", HighlightStyle.OUTLINE, HighlightStyle.values()));
    }

    @Override
    public void onHudRender(DrawContext ctx) {
    }

    public void renderEntityHighlight() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hitResult)) return;

        Entity entity = hitResult.getEntity();

        boolean shouldHighlight = false;
        if (entity instanceof PlayerEntity && players.getValue()) shouldHighlight = true;
        else if (entity instanceof MobEntity && mobs.getValue()) shouldHighlight = true;
        else if (!entity.isLiving() && !players.getValue() && !mobs.getValue()) shouldHighlight = true;

        if (!shouldHighlight) return;

        renderEntityBox(entity, style.getValue());
    }

    private void renderEntityHighlight(MinecraftClient mc, Entity entity, HighlightStyle style) {
        if (mc.player == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        float minX = (float) (entity.getBoundingBox().minX - x);
        float minY = (float) (entity.getBoundingBox().minY - y);
        float minZ = (float) (entity.getBoundingBox().minZ - z);
        float maxX = (float) (entity.getBoundingBox().maxX - x);
        float maxY = (float) (entity.getBoundingBox().maxY - y);
        float maxZ = (float) (entity.getBoundingBox().maxZ - z);

        float r = 1.0f, g = 0.0f, b = 0.0f;
        float alpha = 0.3f;

        Tessellator tess = Tessellator.getInstance();
        var buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buf.vertex(minX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, minZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, minZ).color(r, g, b, alpha);

        buf.vertex(minX, minY, maxZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, maxZ).color(r, g, b, alpha);

        buf.vertex(minX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(minX, minY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, minZ).color(r, g, b, alpha);

        buf.vertex(minX, maxY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, maxZ).color(r, g, b, alpha);

        buf.vertex(minX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, minZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(minX, minY, maxZ).color(r, g, b, alpha);

        buf.vertex(maxX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, minZ).color(r, g, b, alpha);

        buf.vertex(minX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, minZ).color(r, g, b, alpha);
        buf.vertex(maxX, minY, maxZ).color(r, g, b, alpha);
        buf.vertex(minX, minY, maxZ).color(r, g, b, alpha);

        buf.vertex(minX, maxY, minZ).color(r, g, b, alpha);
        buf.vertex(minX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, maxZ).color(r, g, b, alpha);
        buf.vertex(maxX, maxY, minZ).color(r, g, b, alpha);

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private void renderEntityBox(Entity entity, HighlightStyle style) {
        MinecraftClient mc = MinecraftClient.getInstance();
        renderEntityHighlight(mc, entity, style);
    }

    public enum HighlightStyle {
        OUTLINE,
        FILL,
        BOTH
    }
}
