package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.setting.KeySetting;
import com.saltclient.setting.Setting;
import com.saltclient.module.Module;
import com.saltclient.util.UiDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.List;

public final class ModuleSettingsScreen extends Screen {
    private static final int SURFACE = 0xFF1A1A1A;
    private static final int SURFACE_HOVER = 0xFF252525;
    private static final int SURFACE_ACTIVE = 0xFF2D2D2D;
    
    private static final int PRIMARY = 0xFFFF6B35;
    private static final int ACCENT = 0xFF00D9FF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B0B0;
    private static final int TEXT_MUTED = 0xFF707070;
    private static final int TOGGLE_ON = 0xFFFF6B35;
    private static final int TOGGLE_OFF = 0xFF404040;
    private static final int BORDER = 0xFF333333;

    private final Screen parent;
    private final Module module;
    private KeySetting listeningKey;

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName() + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 60, 480);
        int panelH = Math.min(this.height - 60, 400);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(this.width / 2 - 50, panelY + panelH - 32, 100, 24)
            .build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xE0000000);
        
        int panelW = Math.min(this.width - 60, 480);
        int panelH = Math.min(this.height - 60, 400);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        
        UiDraw.fillRounded(ctx, panelX, panelY, panelX + panelW, panelY + panelH, 16, SURFACE);
        
        int closeBtnX = panelX + panelW - 40;
        int closeBtnY = panelY + 8;
        boolean closeHover = mouseX >= closeBtnX && mouseX < closeBtnX + 32 && mouseY >= closeBtnY && mouseY < closeBtnY + 32;
        UiDraw.fillRounded(ctx, closeBtnX, closeBtnY, closeBtnX + 32, closeBtnY + 32, 8, closeHover ? SURFACE_HOVER : SURFACE_ACTIVE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("X"), closeBtnX + 16, closeBtnY + 10, TEXT_PRIMARY);
        
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 24, TEXT_PRIMARY);
        
        String desc = module.getDescription();
        if (desc != null && !desc.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(desc), this.width / 2, panelY + 44, TEXT_SECONDARY);
        }
        
        ctx.fill(panelX + 16, panelY + 60, panelX + panelW - 16, panelY + 61, BORDER);
        
        int y = panelY + 80;
        int rowH = 40;
        int gap = 8;
        
        List<Setting<?>> settings = module.getSettings();
        for (Setting<?> s : settings) {
            if (y > panelY + panelH - 70) break;
            
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(s.getName()), panelX + 20, y + 12, TEXT_PRIMARY);
            
            int ctrlX = panelX + panelW - 140;
            int ctrlW = 120;
            
            if (s instanceof BoolSetting bs) {
                boolean on = bs.getValue();
                int toggleColor = on ? TOGGLE_ON : TOGGLE_OFF;
                UiDraw.fillRounded(ctx, ctrlX, y + 10, ctrlX + ctrlW, y + 30, 10, toggleColor);
                
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(on ? "ON" : "OFF"), ctrlX + ctrlW / 2, y + 14, TEXT_PRIMARY);
                
            } else if (s instanceof IntSetting is) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(String.valueOf(is.getValue())), ctrlX + 40, y + 14, TEXT_SECONDARY);
                
                boolean minusHover = mouseX >= ctrlX && mouseX < ctrlX + 28 && mouseY >= y + 10 && mouseY < y + 30;
                boolean plusHover = mouseX >= ctrlX + ctrlW - 28 && mouseX < ctrlX + ctrlW && mouseY >= y + 10 && mouseY < y + 30;
                
                UiDraw.fillRounded(ctx, ctrlX, y + 10, ctrlX + 28, y + 30, 6, minusHover ? SURFACE_HOVER : SURFACE_ACTIVE);
                UiDraw.fillRounded(ctx, ctrlX + ctrlW - 28, y + 10, ctrlX + ctrlW, y + 30, 6, plusHover ? SURFACE_HOVER : SURFACE_ACTIVE);
                
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("-"), ctrlX + 14, y + 13, TEXT_PRIMARY);
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("+"), ctrlX + ctrlW - 14, y + 13, TEXT_PRIMARY);
                
            } else if (s instanceof EnumSetting<?> es) {
                UiDraw.fillRounded(ctx, ctrlX, y + 10, ctrlX + ctrlW, y + 30, 6, SURFACE_ACTIVE);
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(es.getValue().toString()), ctrlX + ctrlW / 2, y + 14, TEXT_SECONDARY);
                
            } else if (s instanceof KeySetting ks) {
                String keyName;
                if (listeningKey == ks) {
                    keyName = "Press key...";
                } else if (ks.getValue() < 0) {
                    keyName = "None";
                } else {
                    keyName = InputUtil.fromKeyCode(ks.getValue(), 0).getLocalizedText().getString();
                }
                
                UiDraw.fillRounded(ctx, ctrlX, y + 10, ctrlX + ctrlW, y + 30, 6, SURFACE_ACTIVE);
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(keyName), ctrlX + ctrlW / 2 - this.textRenderer.getWidth(keyName) / 2, y + 14, listeningKey == ks ? ACCENT : TEXT_SECONDARY);
            }
            
            y += rowH + gap;
        }
        
        if (listeningKey != null) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press any key... (ESC to cancel)"), this.width / 2, panelY + panelH - 50, TEXT_MUTED);
        }
        
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = Math.min(this.width - 60, 480);
        int panelH = Math.min(this.height - 60, 400);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        
        int closeBtnX = panelX + panelW - 40;
        int closeBtnY = panelY + 8;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + 32 && mouseY >= closeBtnY && mouseY < closeBtnY + 32) {
            close();
            return true;
        }
        
        int y = panelY + 80;
        int rowH = 40;
        int gap = 8;
        
        for (Setting<?> s : module.getSettings()) {
            if (y > panelY + panelH - 70) break;
            
            int ctrlX = panelX + panelW - 140;
            int ctrlW = 120;
            
            if (s instanceof BoolSetting bs) {
                if (mouseX >= ctrlX && mouseX < ctrlX + ctrlW && mouseY >= y + 10 && mouseY < y + 30) {
                    bs.toggle();
                    SaltClient.CONFIG.save(SaltClient.MODULES);
                    return true;
                }
            } else if (s instanceof IntSetting is) {
                if (mouseX >= ctrlX && mouseX < ctrlX + 28 && mouseY >= y + 10 && mouseY < y + 30) {
                    is.dec();
                    SaltClient.CONFIG.save(SaltClient.MODULES);
                    return true;
                }
                if (mouseX >= ctrlX + ctrlW - 28 && mouseX < ctrlX + ctrlW && mouseY >= y + 10 && mouseY < y + 30) {
                    is.inc();
                    SaltClient.CONFIG.save(SaltClient.MODULES);
                    return true;
                }
            } else if (s instanceof EnumSetting<?> es) {
                if (mouseX >= ctrlX && mouseX < ctrlX + ctrlW && mouseY >= y + 10 && mouseY < y + 30) {
                    es.next();
                    SaltClient.CONFIG.save(SaltClient.MODULES);
                    return true;
                }
            } else if (s instanceof KeySetting ks) {
                if (mouseX >= ctrlX && mouseX < ctrlX + ctrlW && mouseY >= y + 10 && mouseY < y + 30) {
                    if (listeningKey == ks) {
                        listeningKey = null;
                    } else {
                        listeningKey = ks;
                    }
                    return true;
                }
            }
            
            y += rowH + gap;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningKey != null) {
            if (keyCode == 256) {
                listeningKey = null;
                return true;
            }
            
            listeningKey.setValue(keyCode);
            listeningKey = null;
            SaltClient.CONFIG.save(SaltClient.MODULES);
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && parent != null) {
            mc.setScreen(parent);
        } else {
            super.close();
        }
    }
}
