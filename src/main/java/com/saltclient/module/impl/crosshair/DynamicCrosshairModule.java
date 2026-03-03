package com.saltclient.module.impl.crosshair;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public final class DynamicCrosshairModule extends Module {
    private final BoolSetting playerIndicator;
    private final BoolSetting mobIndicator;
    private final BoolSetting hitMarker;
    private final IntSetting hitMarkerTime;
    private final EnumSetting<HitMarkerType> hitMarkerType;
    private final IntSetting hitMarkerSpeed;
    private final BoolSetting elytraIndicator;
    private final IntSetting elytraSize;
    private final BoolSetting showInThirdPerson;

    private final Identifier MOD_ICONS = Identifier.of("saltclient", "textures/crosshair/modicons.png");

    private int hitMarkerDisplayTicks = 0;

    public DynamicCrosshairModule() {
        super("dynamiccrosshair", "DynamicCrosshair", "Dynamic crosshair with indicators and hit markers.", ModuleCategory.CROSSHAIR, true);

        this.playerIndicator = addSetting(new BoolSetting("playerIndicator", "Player Indicator", "Show indicator when targeting a player.", true));
        this.mobIndicator = addSetting(new BoolSetting("mobIndicator", "Mob Indicator", "Show indicator when targeting a mob.", true));
        this.hitMarker = addSetting(new BoolSetting("hitMarker", "Hit Marker", "Show hit marker on attack.", true));
        this.hitMarkerTime = addSetting(new IntSetting("hitMarkerTime", "Hit Marker Time", "How long to show hit marker (ticks).", 15, 5, 40, 1));
        this.hitMarkerType = addSetting(new EnumSetting<>("hitMarkerType", "Hit Marker Type", "Visual style of hit marker.", HitMarkerType.DEFAULT, HitMarkerType.values()));
        this.hitMarkerSpeed = addSetting(new IntSetting("hitMarkerSpeed", "Hit Marker Speed", "Animation speed of hit marker.", 10, 5, 30, 1));
        this.elytraIndicator = addSetting(new BoolSetting("elytraIndicator", "Elytra Indicator", "Show indicator when wearing elytra.", true));
        this.elytraSize = addSetting(new IntSetting("elytraSize", "Elytra Size", "Size of elytra indicator (x10).", 10, 5, 20, 1));
        this.showInThirdPerson = addSetting(new BoolSetting("showInThirdPerson", "Show Third Person", "Show indicators in third person view.", true));
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (hitMarkerDisplayTicks > 0) {
            hitMarkerDisplayTicks--;
        }

        long now = System.currentTimeMillis();
        if (hitMarker.getValue() && SaltState.lastHitMs != 0 && (now - SaltState.lastHitMs) <= 50) {
            triggerHitMarker();
        }
    }

    public void triggerHitMarker() {
        if (hitMarker.getValue()) {
            this.hitMarkerDisplayTicks = hitMarkerTime.getValue();
        }
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (!showInThirdPerson.getValue() && !mc.options.getPerspective().isFirstPerson()) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        boolean targetingPlayer = mc.targetedEntity instanceof PlayerEntity;
        boolean targetingMob = mc.targetedEntity instanceof MobEntity;

        if ((playerIndicator.getValue() && targetingPlayer) || (mobIndicator.getValue() && targetingMob)) {
            ctx.drawTexture(id -> RenderLayer.getGuiTextured(id), MOD_ICONS, cx - 4, cy - 4, 0, 0, 9, 9, 64, 64);
        }

        if (elytraIndicator.getValue() && isWearingElytra(mc)) {
            float scale = elytraSize.getValue() / 10.0f;
            int iconW = (int) (14 * scale);
            int iconH = (int) (11 * scale);
            int x = cx - iconW / 2;
            int y = cy - 16;
            ctx.drawTexture(id -> RenderLayer.getGuiTextured(id), MOD_ICONS, x, y, 10, 0, 14, 11, 64, 64);
        }

        if (hitMarker.getValue() && hitMarkerDisplayTicks > 0) {
            switch (hitMarkerType.getValue()) {
                case FADE -> drawFadeHitMarker(ctx, cx, cy);
                case ANIMATION -> drawAnimatedHitMarker(ctx, cx, cy);
                case DEFAULT -> drawDefaultHitMarker(ctx, cx, cy);
            }
        }
    }

    private void drawDefaultHitMarker(DrawContext ctx, int cx, int cy) {
        ctx.drawTexture(id -> RenderLayer.getGuiTextured(id), MOD_ICONS, cx - 5, cy - 5, 24, 0, 10, 10, 64, 64);
    }

    private void drawFadeHitMarker(DrawContext ctx, int cx, int cy) {
        int fadeDuration = Math.max(2, hitMarkerSpeed.getValue());
        float fadeProgress = Math.min(1.0f, (float) this.hitMarkerDisplayTicks / fadeDuration);
        float alpha = fadeProgress;

        ctx.drawTexture(id -> RenderLayer.getGuiTextured(id), MOD_ICONS, cx - 5, cy - 5, 24, 0, 10, 10, 64, 64);
    }

    private void drawAnimatedHitMarker(DrawContext ctx, int cx, int cy) {
        int[] frameV = {24, 13, 0};
        int numFrames = frameV.length;

        int frameDuration = Math.max((hitMarkerSpeed.getValue() / numFrames) * 2, 1);
        int totalDuration = frameDuration * numFrames;

        boolean completed = this.hitMarkerDisplayTicks >= totalDuration;
        int frameIndex = completed ? (numFrames - 1) : Math.min((this.hitMarkerDisplayTicks / frameDuration), numFrames - 1);

        ctx.drawTexture(id -> RenderLayer.getGuiTextured(id), MOD_ICONS, cx - 5, cy - 5, 24, frameV[frameIndex], 10, 10, 64, 64);
    }

    private boolean isWearingElytra(MinecraftClient mc) {
        return mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
    }

    public enum HitMarkerType {
        FADE("Fade"),
        ANIMATION("Animation"),
        DEFAULT("Default");

        private final String name;

        HitMarkerType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
