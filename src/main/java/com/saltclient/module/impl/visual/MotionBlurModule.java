package com.saltclient.module.impl.visual;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.mixin.FramebufferAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class MotionBlurModule extends Module {
    private final IntSetting strength;
    private final BoolSetting velocityBased;

    private SimpleFramebuffer prevBuf;
    private int bufW = -1;
    private int bufH = -1;

    private float prevYaw;
    private float prevPitch;
    private float smoothedAlpha;

    public MotionBlurModule() {
        super("motionblur", "MotionBlur", "Smooth Lunar-style camera motion blur.", ModuleCategory.VISUAL, true);
        this.strength = addSetting(new IntSetting(
            "strength", "Strength",
            "Blur intensity.",
            9, 1, 10, 1
        ));
        this.velocityBased = addSetting(new BoolSetting(
            "velocityBased", "Velocity Based",
            "Scale blur with camera speed.",
            false
        ));
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        releaseBuffers();
        smoothedAlpha = 0f;
        syncAngles(mc);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        releaseBuffers();
    }

    public void applyBlur() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.currentScreen != null) return;

        Framebuffer main = mc.getFramebuffer();
        if (main == null || main.textureWidth <= 0 || main.textureHeight <= 0) return;

        int w = main.textureWidth;
        int h = main.textureHeight;

        if (!ensureBuffer(w, h)) return;

        float alpha = computeAlpha(mc);

        if (alpha >= 0.005f) {
            main.beginWrite(false);
            blendTextureOver(((FramebufferAccessor)(Object)prevBuf).getColorAttachment(), w, h, alpha);
        }

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.fbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevBuf.fbo);
        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        main.beginWrite(false);
    }

    @Override
    public void onHudRender(DrawContext ctx) {}

    private static void blendTextureOver(int glTextureId, int w, int h, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, glTextureId);
        RenderSystem.depthMask(false);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(
            new Matrix4f().setOrtho(0f, w, h, 0f, 1000f, 3000f),
            VertexSorter.BY_Z
        );
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.pushMatrix();
        mv.identity();
        mv.translate(0f, 0f, -2000f);
        RenderSystem.applyModelViewMatrix();

        Tessellator tess = Tessellator.getInstance();
        var buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(0, 0, 0).texture(0f, 1f);
        buf.vertex(w, 0, 0).texture(1f, 1f);
        buf.vertex(w, h, 0).texture(1f, 0f);
        buf.vertex(0, h, 0).texture(0f, 0f);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        mv.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private float computeAlpha(MinecraftClient mc) {
        float maxAlpha = MathHelper.clamp((strength.getValue() / 10.0f) * 0.92f, 0f, 0.92f);

        if (!velocityBased.getValue()) {
            smoothedAlpha = maxAlpha;
            return smoothedAlpha;
        }

        float yaw   = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        float dy = Math.abs(MathHelper.wrapDegrees(yaw - prevYaw));
        float dp = Math.abs(pitch - prevPitch);
        prevYaw   = yaw;
        prevPitch = pitch;

        float targetAlpha = MathHelper.clamp(((dy + dp) / 10.0f) * maxAlpha, 0f, maxAlpha);

        float blend = (targetAlpha > smoothedAlpha) ? 0.55f : 0.12f;
        smoothedAlpha += (targetAlpha - smoothedAlpha) * blend;

        return smoothedAlpha;
    }

    private void syncAngles(MinecraftClient mc) {
        if (mc != null && mc.player != null) {
            prevYaw   = mc.player.getYaw();
            prevPitch = mc.player.getPitch();
        }
    }

    private boolean ensureBuffer(int w, int h) {
        if (prevBuf != null && w == bufW && h == bufH) return true;
        try {
            releaseBuffers();
            prevBuf = new SimpleFramebuffer(w, h, true, MinecraftClient.IS_SYSTEM_MAC);
            prevBuf.setClearColor(0f, 0f, 0f, 0f);
            prevBuf.clear(MinecraftClient.IS_SYSTEM_MAC);
            bufW = w;
            bufH = h;
            return true;
        } catch (Throwable t) {
            releaseBuffers();
            return false;
        }
    }

    private void releaseBuffers() {
        if (prevBuf != null) { prevBuf.delete(); prevBuf = null; }
        bufW = -1;
        bufH = -1;
    }
}
