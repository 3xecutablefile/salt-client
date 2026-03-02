package com.saltclient.module.impl.visual;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;

public final class BlockHighlightModule extends Module {
    private final EnumSetting<HighlightStyle> style;
    private final BoolSetting fill;

    public BlockHighlightModule() {
        super("blockhighlight", "BlockHighlight", "Highlight targeted blocks.", ModuleCategory.VISUAL, true);
        this.style = addSetting(new EnumSetting<>("style", "Style", "Outline style.", HighlightStyle.OUTLINE, HighlightStyle.values()));
        this.fill = addSetting(new BoolSetting("fill", "Fill", "Fill the block highlight.", false));
    }

    @Override
    public void onHudRender(DrawContext ctx) {
    }

    public void renderBlockHighlight() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.crosshairTarget == null) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult hitResult)) return;

        BlockPos pos = hitResult.getBlockPos();
        Direction face = hitResult.getSide();

        renderBlockBox(pos, style.getValue(), fill.getValue());
    }

    private void renderBlockBox(BlockPos pos, HighlightStyle style, boolean fill) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        float minX = (float) (pos.getX() - x + 0.001);
        float minY = (float) (pos.getY() - y + 0.001);
        float minZ = (float) (pos.getZ() - z + 0.001);
        float maxX = (float) (pos.getX() - x + 1 - 0.001);
        float maxY = (float) (pos.getY() - y + 1 - 0.001);
        float maxZ = (float) (pos.getZ() - z + 1 - 0.001);

        float r = 0.0f, g = 1.0f, b = 1.0f;
        float alpha = 0.5f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (fill) {
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
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    public enum HighlightStyle {
        OUTLINE,
        FILL,
        BOTH
    }
}
