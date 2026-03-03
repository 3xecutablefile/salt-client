package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.GuiSettings;
import com.saltclient.util.HudPos;
import com.saltclient.util.UiDraw;
import com.saltclient.util.UiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SaltScreen extends Screen {
    private static final int BACKGROUND0 = 0xE80D0D0D;
    private static final int SURFACE = 0xFF1A1A1A;
    private static final int SURFACE_HOVER = 0xFF252525;
    private static final int SURFACE_ACTIVE = 0xFF2D2D2D;
    
    private static final int PRIMARY = 0xFFFF6B35;
    private static final int PRIMARY_LIGHT = 0xFFFF8A5C;
    private static final int ACCENT = 0xFF00D9FF;
    private static final int ACCENT_DARK = 0xFF00A8C6;
    
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B0B0;
    private static final int TEXT_MUTED = 0xFF707070;
    
    private static final int TOGGLE_ON = 0xFFFF6B35;
    private static final int TOGGLE_OFF = 0xFF404040;
    
    private static final int BORDER = 0xFF333333;
    private static final int BORDER_LIGHT = 0xFF444444;
    
    private static final int CATEGORY_COLORS[] = {
        0xFFFF6B35,
        0xFF00D9FF,
        0xFF7C3AED,
        0xFF10B981,
        0xFFF59E0B,
        0xFFEC4899,
        0xFF6366F1,
        0xFF14B8A6,
        0xFFEF4444,
        0xFF8B5CF6,
        0xFF64748B
    };

    private TextFieldWidget search;
    private TextFieldWidget configName;
    private TextFieldWidget configPath;

    private ModuleCategory selected = ModuleCategory.ALL;
    private boolean configTab;
    private boolean settingsOpen;
    private Module selectedModule;

    private double moduleScroll;
    private double configScroll;

    private final Map<String, Float> toggleAnim = new HashMap<>();
    private final Map<String, Float> cardHoverAnim = new HashMap<>();
    private List<String> namedConfigs = new ArrayList<>();

    private String statusText = "";
    private int statusColor = TEXT_SECONDARY;
    private long statusUntilMs;

    private long openedAtMs;
    private long lastConfigClickMs;
    private String lastConfigClicked = "";

    private static final class Layout {
        int panelX, panelY, panelW, panelH;
        int headerH = 60;
        int sidebarW = 220;
        int footerH = 44;
        
        int contentX, contentY, contentW, contentH;
        int sidebarX, sidebarY, sidebarH;
        int listX, listY, listW, listH;
        
        int searchX, searchY, searchW, searchH = 36;
        
        int cfgNameX, cfgNameY, cfgNameW, cfgNameH;
        int cfgPathX, cfgPathY, cfgPathW, cfgPathH;
        int cfgListX, cfgListY, cfgListW, cfgListH;
        
        int resetX, resetY, resetW, resetH;
        int editX, editY, editW, editH;
    }

    private Layout layout() {
        Layout l = new Layout();
        
        l.panelW = Math.min(this.width - 40, 1100);
        l.panelH = Math.min(this.height - 40, 640);
        l.panelX = (this.width - l.panelW) / 2;
        l.panelY = (this.height - l.panelH) / 2;
        
        l.sidebarX = l.panelX + 12;
        l.sidebarY = l.panelY + l.headerH + 12;
        l.sidebarW = Math.min(240, l.panelW / 5);
        l.sidebarH = l.panelY + l.panelH - l.footerH - 12 - l.sidebarY;
        
        l.contentX = l.sidebarX + l.sidebarW + 16;
        l.contentY = l.panelY + l.headerH + 12;
        l.contentW = l.panelX + l.panelW - 12 - l.contentX;
        l.contentH = l.panelY + l.panelH - l.footerH - 12 - l.contentY;
        
        l.listX = l.contentX;
        l.listY = l.contentY + l.searchH + 12;
        l.listW = l.contentW;
        l.listH = l.contentH - l.searchH - 12;
        
        l.searchX = l.listX;
        l.searchY = l.listY - l.searchH - 12;
        l.searchW = l.listW;
        
        l.cfgNameX = l.contentX + 100;
        l.cfgNameY = l.contentY + 12;
        l.cfgNameW = Math.max(150, l.contentW - 350);
        l.cfgNameH = 28;
        
        l.cfgPathX = l.cfgNameX;
        l.cfgPathY = l.cfgNameY + 38;
        l.cfgPathW = l.cfgNameW;
        l.cfgPathH = 28;
        
        l.cfgListX = l.contentX + 12;
        l.cfgListY = l.contentY + 80;
        l.cfgListW = l.contentW - 24;
        l.cfgListH = l.contentH - 92;
        
        l.resetX = l.panelX + 16;
        l.resetY = l.panelY + l.panelH - l.footerH + 8;
        l.resetW = 100;
        l.resetH = 28;
        
        l.editX = l.resetX + l.resetW + 12;
        l.editY = l.resetY;
        l.editW = 100;
        l.editH = 28;
        
        return l;
    }

    public SaltScreen() {
        super(UiFonts.text("Salt"));
    }

    @Override
    protected void init() {
        Layout l = layout();
        
        this.search = new TextFieldWidget(
            this.textRenderer,
            l.searchX + 16,
            l.searchY + 10,
            l.searchW - 40,
            18,
            Text.literal("Search modules...")
        );
        this.search.setMaxLength(64);
        this.search.setDrawsBackground(false);
        this.search.setEditableColor(TEXT_PRIMARY);
        this.search.setUneditableColor(TEXT_MUTED);
        this.addDrawableChild(this.search);

        this.configName = new TextFieldWidget(
            this.textRenderer,
            l.cfgNameX + 6,
            l.cfgNameY + 7,
            l.cfgNameW - 12,
            14,
            Text.literal("default")
        );
        this.configName.setMaxLength(64);
        this.configName.setDrawsBackground(false);
        this.configName.setEditableColor(TEXT_PRIMARY);
        this.configName.setUneditableColor(TEXT_MUTED);
        this.configName.setText("default");
        this.addDrawableChild(this.configName);

        this.configPath = new TextFieldWidget(
            this.textRenderer,
            l.cfgPathX + 6,
            l.cfgPathY + 7,
            l.cfgPathW - 12,
            14,
            Text.literal("/sdcard/Download/config.json")
        );
        this.configPath.setMaxLength(200);
        this.configPath.setDrawsBackground(false);
        this.configPath.setEditableColor(TEXT_PRIMARY);
        this.configPath.setUneditableColor(TEXT_MUTED);
        this.addDrawableChild(this.configPath);

        this.setInitialFocus(this.search);
        refreshConfigList();
        if (openedAtMs == 0L) openedAtMs = System.currentTimeMillis();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Layout l = layout();
        syncWidgets(l);
        
        float open = openProgress();
        
        ctx.fill(0, 0, this.width, this.height, scaleAlpha(BACKGROUND0, open));
        
        renderPanel(ctx, l, open);
        
        if (!settingsOpen) {
            renderHeader(ctx, l, open);
            renderSidebar(ctx, mouseX, mouseY, l, open);
            
            if (configTab) {
                renderConfigManager(ctx, mouseX, mouseY, l, open);
            } else {
                renderModules(ctx, mouseX, mouseY, l, open);
            }
            
            renderFooter(ctx, mouseX, mouseY, l, open);
        } else {
            renderSettingsPanel(ctx, mouseX, mouseY, l, open);
        }
        
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, Layout l, float alpha) {
        UiDraw.fillRounded(ctx, l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.panelH, 16, scaleAlpha(SURFACE, alpha));
        UiDraw.fillRounded(ctx, l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.headerH, 16, scaleAlpha(SURFACE_ACTIVE, alpha));
        ctx.fill(l.panelX, l.panelY + l.headerH - 8, l.panelX + l.panelW, l.panelY + l.headerH - 7, scaleAlpha(BORDER, alpha));
        ctx.fill(l.panelX, l.panelY + l.panelH - l.footerH, l.panelX + l.panelW, l.panelY + l.panelH - l.footerH + 1, scaleAlpha(BORDER, alpha));
    }

    private void renderHeader(DrawContext ctx, Layout l, float alpha) {
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("S"), l.panelX + 20, l.panelY + 22, scaleAlpha(PRIMARY, alpha));
        ctx.drawTextWithShadow(this.textRenderer, this.title, l.panelX + 42, l.panelY + 22, scaleAlpha(TEXT_PRIMARY, alpha));
        
        int versionY = l.panelY + 38;
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("v1.1.5"), l.panelX + 42, versionY, scaleAlpha(TEXT_MUTED, alpha));
        
        int btnX = l.panelX + l.panelW - 40;
        int btnY = l.panelY + 18;
        int btnS = 28;
        
        for (int i = 0; i < 3; i++) {
            String icon = i == 0 ? "<" : (i == 1 ? "+" : "C");
            UiDraw.fillRounded(ctx, btnX + i * 36, btnY, btnX + i * 36 + btnS, btnY + btnS, 8, scaleAlpha(SURFACE_ACTIVE, alpha));
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(icon), btnX + i * 36 + btnS/2, btnY + 8, scaleAlpha(TEXT_SECONDARY, alpha));
        }
    }

    private void renderSidebar(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        UiDraw.fillRounded(ctx, l.sidebarX, l.sidebarY, l.sidebarX + l.sidebarW, l.sidebarY + l.sidebarH, 12, scaleAlpha(SURFACE_ACTIVE, alpha));
        
        int rowH = 40;
        int gap = 4;
        int y = l.sidebarY + 12;
        
        int idx = 0;
        for (ModuleCategory c : ModuleCategory.values()) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
            boolean active = !configTab && c == selected;
            
            if (active || hover) {
                int bg = active ? PRIMARY : SURFACE_HOVER;
                UiDraw.fillRounded(ctx, x, y, x + w, y + rowH, 10, scaleAlpha(bg, alpha));
            }
            
            if (active) {
                ctx.fill(x, y, x + 3, y + rowH, scaleAlpha(PRIMARY, alpha));
            }
            
            int color = CATEGORYColors(c, idx);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(categoryIcon(c)), x + 10, y + 13, scaleAlpha(color, alpha));
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(categoryName(c)), x + 30, y + 13, scaleAlpha(active ? TEXT_PRIMARY : TEXT_SECONDARY, alpha));
            
            int count = moduleCount(c);
            String countStr = String.valueOf(count);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(countStr), x + w - 20, y + 13, scaleAlpha(TEXT_MUTED, alpha));
            
            y += rowH + gap;
            idx++;
        }
        
        int configY = y + 8;
        int x = l.sidebarX + 8;
        int w = l.sidebarW - 16;
        boolean hover = inside(mouseX, mouseY, x, configY, w, rowH);
        boolean active = configTab;
        
        if (active || hover) {
            int bg = active ? PRIMARY : SURFACE_HOVER;
            UiDraw.fillRounded(ctx, x, configY, x + w, configY + rowH, 10, scaleAlpha(bg, alpha));
        }
        
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("~"), x + 10, configY + 13, scaleAlpha(PRIMARY, alpha));
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Configs"), x + 30, configY + 13, scaleAlpha(active ? TEXT_PRIMARY : TEXT_SECONDARY, alpha));
    }

    private int CATEGORYColors(ModuleCategory c, int idx) {
        return CATEGORY_COLORS[idx % CATEGORY_COLORS.length];
    }

    private void renderModules(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        List<Module> list = filteredModules();
        
        UiDraw.fillRounded(ctx, l.searchX, l.searchY, l.searchX + l.searchW, l.searchY + l.searchH, 10, scaleAlpha(SURFACE_ACTIVE, alpha));
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("?"), l.searchX + 6, l.searchY + 11, scaleAlpha(TEXT_MUTED, alpha));
        
        int cols = Math.max(1, l.listW / 280);
        int gap = 12;
        int cardH = 72;
        int colW = (l.listW - (cols - 1) * gap) / cols;
        
        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = Math.max(0, rows * (cardH + gap) - gap);
        int maxScroll = Math.max(0, contentH - l.listH);
        moduleScroll = clamp(moduleScroll, 0, maxScroll);
        
        int startY = l.listY - (int) moduleScroll;
        
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            
            int row = i / cols;
            int col = i % cols;
            int x = l.listX + col * (colW + gap);
            int y = startY + row * (cardH + gap);
            
            if (y + cardH < l.listY || y > l.listY + l.listH) continue;
            
            boolean hover = inside(mouseX, mouseY, x, y, colW, cardH);
            float hoverTarget = hover ? 1.0f : 0.0f;
            float hoverCurrent = cardHoverAnim.getOrDefault(m.getId(), hoverTarget);
            hoverCurrent += (hoverTarget - hoverCurrent) * 0.15f;
            cardHoverAnim.put(m.getId(), hoverCurrent);
            
            int bg = blend(SURFACE_ACTIVE, SURFACE_HOVER, hoverCurrent);
            
            UiDraw.fillRounded(ctx, x, y, x + colW, y + cardH, 12, scaleAlpha(bg, alpha));
            
            if (m.isEnabled()) {
                UiDraw.fillRounded(ctx, x, y, x + 4, y + cardH, 4, scaleAlpha(PRIMARY, alpha));
            }
            
            int iconColor = CATEGORY_COLORS[m.getCategory().ordinal() % CATEGORY_COLORS.length];
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(categoryIcon(m.getCategory())), x + 14, y + 14, scaleAlpha(iconColor, alpha));
            
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(m.getName()), x + 40, y + 14, scaleAlpha(TEXT_PRIMARY, alpha));
            
            if (!m.isImplemented()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("WIP"), x + 40, y + 30, scaleAlpha(0xFFFFAA00, alpha));
            }
            
            String desc = m.getDescription() == null ? "" : m.getDescription();
            String shortDesc = trimToWidth(desc, colW - 100);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(shortDesc), x + 40, y + 36, scaleAlpha(TEXT_MUTED, alpha));
            
            int tw = 44;
            int th = 20;
            int tx = x + colW - tw - 12;
            int ty = y + cardH - th - 10;
            
            float toggle = animateToggle(m);
            int toggleColor = blend(TOGGLE_OFF, TOGGLE_ON, toggle);
            UiDraw.fillRounded(ctx, tx, ty, tx + tw, ty + th, th / 2, scaleAlpha(toggleColor, alpha));
            
            int knob = 16;
            int range = tw - knob - 4;
            int kx = tx + 2 + Math.round(range * toggle);
            int ky = ty + 2;
            UiDraw.fillRounded(ctx, kx, ky, kx + knob, ky + knob, knob / 2, scaleAlpha(0xFFFFFFFF, alpha));
            
            int sb = 22;
            int sx = x + colW - sb - tw - 18;
            int sy = y + cardH - sb - 10;
            int sFill = hover ? SURFACE_HOVER : SURFACE_ACTIVE;
            UiDraw.fillRounded(ctx, sx, sy, sx + sb, sy + sb, 6, scaleAlpha(sFill, alpha));
            ctx.drawTextWithShadow(this.textRenderer, Text.literal("S"), sx + 6, sy + 5, scaleAlpha(TEXT_MUTED, alpha));
        }
        
        if (maxScroll > 0) {
            renderScrollbar(ctx, l, alpha, maxScroll);
        }
    }

    private void renderScrollbar(DrawContext ctx, Layout l, float alpha, int maxScroll) {
        int barX = l.listX + l.listW - 8;
        int barY = l.listY;
        int barH = l.listH;
        
        ctx.fill(barX - 2, barY, barX, barY + barH, scaleAlpha(BORDER, alpha));
        
        float scrollRatio = (float) (moduleScroll / maxScroll);
        int thumbH = Math.max(30, (int) ((l.listH * l.listH) / (maxScroll + l.listH)));
        int thumbY = barY + (int) ((barH - thumbH) * scrollRatio);
        
        UiDraw.fillRounded(ctx, barX - 4, thumbY, barX + 4, thumbY + thumbH, 4, scaleAlpha(PRIMARY, alpha));
    }

    private void renderConfigManager(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Config Manager"), l.contentX + 12, l.contentY + 14, scaleAlpha(TEXT_PRIMARY, alpha));
        
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Name:"), l.cfgNameX - 70, l.cfgNameY + 8, scaleAlpha(TEXT_SECONDARY, alpha));
        UiDraw.fillRounded(ctx, l.cfgNameX, l.cfgNameY, l.cfgNameX + l.cfgNameW, l.cfgNameY + l.cfgNameH, 8, scaleAlpha(SURFACE_ACTIVE, alpha));
        
        int btnW = 80;
        int btnH = 28;
        int btnGap = 8;
        
        int saveX = l.cfgNameX + l.cfgNameW + 12;
        drawButton(ctx, mouseX, mouseY, saveX, l.cfgNameY, btnW, btnH, "Save", alpha, PRIMARY);
        
        int loadX = saveX + btnW + btnGap;
        drawButton(ctx, mouseX, mouseY, loadX, l.cfgNameY, btnW, btnH, "Load", alpha, ACCENT_DARK);
        
        int refreshX = loadX + btnW + btnGap;
        drawButton(ctx, mouseX, mouseY, refreshX, l.cfgNameY, btnW, btnH, "Refresh", alpha, SURFACE_ACTIVE);
        
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("File:"), l.cfgPathX - 50, l.cfgPathY + 8, scaleAlpha(TEXT_SECONDARY, alpha));
        UiDraw.fillRounded(ctx, l.cfgPathX, l.cfgPathY, l.cfgPathX + l.cfgPathW, l.cfgPathY + l.cfgPathH, 8, scaleAlpha(SURFACE_ACTIVE, alpha));
        
        int fileLoadX = l.cfgPathX + l.cfgPathW + 12;
        drawButton(ctx, mouseX, mouseY, fileLoadX, l.cfgPathY, btnW, btnH, "Load", alpha, ACCENT_DARK);
        
        UiDraw.fillRounded(ctx, l.cfgListX, l.cfgListY, l.cfgListX + l.cfgListW, l.cfgListY + l.cfgListH, 10, scaleAlpha(SURFACE_ACTIVE, alpha));
        
        int rowH = 32;
        int visibleRows = Math.max(1, l.cfgListH / rowH);
        int maxScroll = Math.max(0, namedConfigs.size() * rowH - l.cfgListH);
        configScroll = clamp(configScroll, 0, maxScroll);
        
        int startIndex = (int) (configScroll / rowH);
        
        for (int row = 0; row <= visibleRows; row++) {
            int idx = startIndex + row;
            if (idx >= namedConfigs.size()) break;
            
            int y = l.cfgListY + 6 + row * rowH - (int) (configScroll % rowH);
            if (y + rowH <= l.cfgListY || y >= l.cfgListY + l.cfgListH) continue;
            
            String name = namedConfigs.get(idx);
            boolean hover = inside(mouseX, mouseY, l.cfgListX + 4, y, l.cfgListW - 8, rowH - 4);
            
            int rowColor = hover ? SURFACE_HOVER : 0x00000000;
            if (configName != null && name.equalsIgnoreCase(configName.getText().trim())) {
                rowColor = PRIMARY;
            }
            UiDraw.fillRounded(ctx, l.cfgListX + 4, y, l.cfgListX + l.cfgListW - 4, y + rowH - 4, 8, scaleAlpha(rowColor, alpha));
            
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(name), l.cfgListX + 14, y + 10, scaleAlpha(TEXT_PRIMARY, alpha));
        }
        
        if (namedConfigs.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No saved configs"), l.cfgListX + l.cfgListW/2, l.cfgListY + l.cfgListH/2, scaleAlpha(TEXT_MUTED, alpha));
        }
        
        if (!statusText.isEmpty() && System.currentTimeMillis() < statusUntilMs) {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(statusText), l.cfgListX, l.cfgListY + l.cfgListH + 4, scaleAlpha(statusColor, alpha));
        }
    }

    private void renderFooter(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        drawButton(ctx, mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH, "RESET", alpha, SURFACE_ACTIVE);
        drawButton(ctx, mouseX, mouseY, l.editX, l.editY, l.editW, l.editH, "HUD", alpha, SURFACE_ACTIVE);
        
        String stats = statusText();
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(stats), l.panelX + l.panelW - 12 - this.textRenderer.getWidth(stats), l.panelY + l.panelH - 20, scaleAlpha(TEXT_MUTED, alpha));
    }

    private void renderSettingsPanel(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        int panelW = 450;
        int panelH = 500;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        
        UiDraw.fillRounded(ctx, panelX, panelY, panelX + panelW, panelY + panelH, 16, scaleAlpha(SURFACE, alpha));
        
        int closeBtnX = panelX + panelW - 36;
        int closeBtnY = panelY + 8;
        boolean closeHover = inside(mouseX, mouseY, closeBtnX, closeBtnY, 28, 28);
        UiDraw.fillRounded(ctx, closeBtnX, closeBtnY, closeBtnX + 28, closeBtnY + 28, 8, closeHover ? SURFACE_HOVER : SURFACE_ACTIVE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("X"), closeBtnX + 14, closeBtnY + 8, scaleAlpha(TEXT_PRIMARY, alpha));
        
        if (selectedModule != null) {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(selectedModule.getName()), panelX + 20, panelY + 20, scaleAlpha(TEXT_PRIMARY, alpha));
            
            String desc = selectedModule.getDescription() == null ? "" : selectedModule.getDescription();
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(desc), panelX + 20, panelY + 40, scaleAlpha(TEXT_SECONDARY, alpha));
            
            ctx.fill(panelX + 16, panelY + 60, panelX + panelW - 16, panelY + 61, scaleAlpha(BORDER, alpha));
            
            int y = panelY + 80;
            int rowH = 36;
            int gap = 8;
            
            for (var setting : selectedModule.getSettings()) {
                if (y > panelY + panelH - 80) break;
                
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(setting.getName()), panelX + 20, y + 10, scaleAlpha(TEXT_PRIMARY, alpha));
                
                int ctrlX = panelX + panelW - 120;
                int ctrlW = 100;
                
                if (setting instanceof com.saltclient.setting.BoolSetting) {
                    com.saltclient.setting.BoolSetting bs = (com.saltclient.setting.BoolSetting) setting;
                    boolean on = bs.getValue();
                    int toggleColor = on ? PRIMARY : TOGGLE_OFF;
                    UiDraw.fillRounded(ctx, ctrlX, y + 8, ctrlX + ctrlW, y + 8 + 20, 10, scaleAlpha(toggleColor, alpha));
                    ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(on ? "ON" : "OFF"), ctrlX + ctrlW/2, y + 12, scaleAlpha(TEXT_PRIMARY, alpha));
                } else if (setting instanceof com.saltclient.setting.IntSetting) {
                    com.saltclient.setting.IntSetting is = (com.saltclient.setting.IntSetting) setting;
                    ctx.drawTextWithShadow(this.textRenderer, Text.literal(String.valueOf(is.getValue())), ctrlX + 30, y + 12, scaleAlpha(TEXT_SECONDARY, alpha));
                    
                    boolean minusHover = inside(mouseX, mouseY, ctrlX, y + 8, 24, 20);
                    boolean plusHover = inside(mouseX, mouseY, ctrlX + ctrlW - 24, y + 8, 24, 20);
                    UiDraw.fillRounded(ctx, ctrlX, y + 8, ctrlX + 24, y + 28, 6, scaleAlpha(minusHover ? SURFACE_HOVER : SURFACE_ACTIVE, alpha));
                    UiDraw.fillRounded(ctx, ctrlX + ctrlW - 24, y + 8, ctrlX + ctrlW, y + 28, 6, scaleAlpha(plusHover ? SURFACE_HOVER : SURFACE_ACTIVE, alpha));
                    ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("-"), ctrlX + 12, y + 11, scaleAlpha(TEXT_PRIMARY, alpha));
                    ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("+"), ctrlX + ctrlW - 12, y + 11, scaleAlpha(TEXT_PRIMARY, alpha));
                } else if (setting instanceof com.saltclient.setting.EnumSetting) {
                    com.saltclient.setting.EnumSetting<?> es = (com.saltclient.setting.EnumSetting<?>) setting;
                    UiDraw.fillRounded(ctx, ctrlX, y + 8, ctrlX + ctrlW, y + 28, 6, scaleAlpha(SURFACE_ACTIVE, alpha));
                    ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(es.getValue().toString()), ctrlX + ctrlW/2, y + 11, scaleAlpha(TEXT_SECONDARY, alpha));
                } else if (setting instanceof com.saltclient.setting.KeySetting) {
                    com.saltclient.setting.KeySetting ks = (com.saltclient.setting.KeySetting) setting;
                    String keyName = ks.getValue() < 0 ? "None" : net.minecraft.client.util.InputUtil.fromKeyCode(ks.getValue(), 0).getLocalizedText().getString();
                    UiDraw.fillRounded(ctx, ctrlX, y + 8, ctrlX + ctrlW, y + 28, 6, scaleAlpha(SURFACE_ACTIVE, alpha));
                    ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(keyName), ctrlX + ctrlW/2 - this.textRenderer.getWidth(keyName)/2, y + 11, scaleAlpha(ACCENT, alpha));
                }
                
                y += rowH + gap;
            }
        }
    }

    private void drawButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h, String label, float alpha, int color) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        int bg = hover ? blend(color, 0xFFFFFFFF, 0.1f) : color;
        UiDraw.fillRounded(ctx, x, y, x + w, y + h, 8, scaleAlpha(bg, alpha));
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + w / 2, y + 8, scaleAlpha(TEXT_PRIMARY, alpha));
    }

    private int moduleCount(ModuleCategory c) {
        int count = 0;
        for (Module m : SaltClient.MODULES.all()) {
            if (m.getCategory() == c) count++;
        }
        return count;
    }

    private String categoryIcon(ModuleCategory c) {
        return switch (c) {
            case ALL -> "#";
            case HUD -> "H";
            case CHAT -> "C";
            case CAMERA -> "O";
            case CROSSHAIR -> "+";
            case VISUAL -> "V";
            case PERFORMANCE -> "P";
            case MOVEMENT -> "M";
            case COMBAT -> "K";
            case MISC -> "E";
        };
    }

    private String categoryName(ModuleCategory c) {
        return switch (c) {
            case ALL -> "All Modules";
            case HUD -> "HUD";
            case CHAT -> "Chat";
            case CAMERA -> "Camera";
            case CROSSHAIR -> "Crosshair";
            case VISUAL -> "Visual";
            case PERFORMANCE -> "Performance";
            case MOVEMENT -> "Movement";
            case COMBAT -> "Combat";
            case MISC -> "Misc";
        };
    }

    private List<Module> filteredModules() {
        String q = (search == null) ? "" : search.getText();
        q = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<Module> out = new ArrayList<>();
        for (Module m : SaltClient.MODULES.all()) {
            if (!q.isEmpty()) {
                if (!m.getName().toLowerCase(Locale.ROOT).contains(q)) continue;
            } else if (selected != ModuleCategory.ALL && m.getCategory() != selected) {
                continue;
            }
            out.add(m);
        }
        out.sort(Comparator.comparing(Module::getName));
        return out;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Layout l = layout();
        
        if (settingsOpen) {
            int panelW = 450;
            int panelH = 500;
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;
            
            int closeBtnX = panelX + panelW - 36;
            int closeBtnY = panelY + 8;
            if (inside(mouseX, mouseY, closeBtnX, closeBtnY, 28, 28)) {
                settingsOpen = false;
                selectedModule = null;
                return true;
            }
            
            if (selectedModule != null) {
                int y = panelY + 80;
                int rowH = 36;
                int gap = 8;
                
                for (var setting : selectedModule.getSettings()) {
                    if (y > panelY + panelH - 80) break;
                    
                    int ctrlX = panelX + panelW - 120;
                    int ctrlW = 100;
                    
                    if (setting instanceof com.saltclient.setting.BoolSetting bs) {
                        if (inside(mouseX, mouseY, ctrlX, y + 8, ctrlW, 20)) {
                            bs.toggle();
                            SaltClient.CONFIG.save(SaltClient.MODULES);
                            return true;
                        }
                    } else if (setting instanceof com.saltclient.setting.IntSetting is) {
                        if (inside(mouseX, mouseY, ctrlX, y + 8, 24, 20)) {
                            is.dec();
                            SaltClient.CONFIG.save(SaltClient.MODULES);
                            return true;
                        }
                        if (inside(mouseX, mouseY, ctrlX + ctrlW - 24, y + 8, 24, 20)) {
                            is.inc();
                            SaltClient.CONFIG.save(SaltClient.MODULES);
                            return true;
                        }
                    } else if (setting instanceof com.saltclient.setting.EnumSetting<?> es) {
                        if (inside(mouseX, mouseY, ctrlX, y + 8, ctrlW, 20)) {
                            es.next();
                            SaltClient.CONFIG.save(SaltClient.MODULES);
                            return true;
                        }
                    } else if (setting instanceof com.saltclient.setting.KeySetting) {
                        if (inside(mouseX, mouseY, ctrlX, y + 8, ctrlW, 20)) {
                            return true;
                        }
                    }
                    
                    y += rowH + gap;
                }
            }
            
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        int btnX = l.panelX + l.panelW - 40;
        int btnY = l.panelY + 18;
        int btnS = 28;
        
        if (inside(mouseX, mouseY, btnX, btnY, btnS, btnS)) {
            this.close();
            return true;
        }
        if (inside(mouseX, mouseY, btnX + 36, btnY, btnS, btnS)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new InstallerScreen(this));
            return true;
        }
        if (inside(mouseX, mouseY, btnX + 72, btnY, btnS, btnS)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new GlobalChatScreen());
            return true;
        }
        
        if (!configTab && search != null && search.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (configTab) {
            if (configName != null && configName.mouseClicked(mouseX, mouseY, button)) return true;
            if (configPath != null && configPath.mouseClicked(mouseX, mouseY, button)) return true;
        }
        
        int rowH = 40;
        int gap = 4;
        int y = l.sidebarY + 12;
        
        int idx = 0;
        for (ModuleCategory c : ModuleCategory.values()) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            if (inside(mouseX, mouseY, x, y, w, rowH)) {
                configTab = false;
                selected = c;
                moduleScroll = 0;
                setInitialFocus(search);
                return true;
            }
            y += rowH + gap;
            idx++;
        }
        
        int configY = y + 8;
        int x = l.sidebarX + 8;
        int w = l.sidebarW - 16;
        if (inside(mouseX, mouseY, x, configY, w, rowH)) {
            configTab = true;
            setInitialFocus(configName);
            return true;
        }
        
        if (inside(mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH)) {
            HudPos.resetAll();
            SaltClient.CONFIG.save(SaltClient.MODULES);
            setStatus("HUD positions reset", true);
            return true;
        }
        if (inside(mouseX, mouseY, l.editX, l.editY, l.editW, l.editH)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new HudEditorScreen(this));
            return true;
        }
        
        if (configTab) {
            return handleConfigClick(mouseX, mouseY, button, l);
        }
        
        if (!inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        List<Module> list = filteredModules();
        int cols = Math.max(1, l.listW / 280);
        int gapModules = 12;
        int cardH = 72;
        int colW = (l.listW - (cols - 1) * gapModules) / cols;
        
        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = Math.max(0, rows * (cardH + gapModules) - gapModules);
        int maxScroll = Math.max(0, contentH - l.listH);
        
        int step = cardH + gapModules;
        
        double localY = mouseY - l.listY + moduleScroll;
        
        int unitW = colW + gapModules;
        int unitH = cardH + gapModules;
        int col = (int) ((mouseX - l.listX) / unitW);
        int row = (int) (localY / unitH);
        
        if (col < 0 || col >= cols || row < 0) return true;
        
        double inCol = (mouseX - l.listX) - (col * unitW);
        double inRow = localY - (row * unitH);
        if (inCol < 0 || inCol >= colW || inRow < 0 || inRow >= cardH) return true;
        
        int moduleIdx = row * cols + col;
        if (moduleIdx < 0 || moduleIdx >= list.size()) return true;
        
        Module m = list.get(moduleIdx);
        int cardX = l.listX + col * unitW;
        int cardY = l.listY + row * unitH - (int) moduleScroll;
        
        int sb = 22;
        int settingsX = cardX + colW - sb - 44;
        int settingsY = cardY + cardH - sb - 10;
        if (inside(mouseX, mouseY, settingsX, settingsY, sb, sb)) {
            settingsOpen = true;
            selectedModule = m;
            return true;
        }
        
        if (!m.isImplemented()) {
            setStatus("WIP: " + m.getName(), false);
            return true;
        }
        
        m.toggle();
        return true;
    }

    private boolean handleConfigClick(double mouseX, double mouseY, int button, Layout l) {
        int btnW = 80;
        int btnH = 28;
        
        int saveX = l.cfgNameX + l.cfgNameW + 12;
        if (inside(mouseX, mouseY, saveX, l.cfgNameY, btnW, btnH)) {
            String name = configName == null ? "" : configName.getText();
            boolean ok = SaltClient.CONFIG.saveNamed(SaltClient.MODULES, name);
            if (ok) {
                refreshConfigList();
                setStatus("Saved: " + name.trim(), true);
            } else {
                setStatus("Save failed", false);
            }
            return true;
        }
        
        int loadX = saveX + btnW + 8;
        if (inside(mouseX, mouseY, loadX, l.cfgNameY, btnW, btnH)) {
            String name = configName == null ? "" : configName.getText();
            boolean ok = SaltClient.CONFIG.loadNamed(SaltClient.MODULES, name);
            setStatus(ok ? "Loaded: " + name.trim() : "Load failed", ok);
            return true;
        }
        
        int refreshX = loadX + btnW + 8;
        if (inside(mouseX, mouseY, refreshX, l.cfgNameY, btnW, btnH)) {
            refreshConfigList();
            setStatus("Refreshed", true);
            return true;
        }
        
        int fileLoadX = l.cfgPathX + l.cfgPathW + 12;
        if (inside(mouseX, mouseY, fileLoadX, l.cfgPathY, btnW, btnH)) {
            String path = configPath == null ? "" : configPath.getText();
            boolean ok = SaltClient.CONFIG.loadExternal(SaltClient.MODULES, path);
            setStatus(ok ? "File loaded" : "File load failed", ok);
            return true;
        }
        
        if (!inside(mouseX, mouseY, l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        int rowH = 32;
        int idx = (int) ((mouseY - l.cfgListY + configScroll) / rowH);
        if (idx < 0 || idx >= namedConfigs.size()) return true;
        
        String name = namedConfigs.get(idx);
        if (configName != null) configName.setText(name);
        
        if (button == 0) {
            long now = System.currentTimeMillis();
            if (name.equals(lastConfigClicked) && now - lastConfigClickMs <= 350L) {
                boolean ok = SaltClient.CONFIG.loadNamed(SaltClient.MODULES, name);
                setStatus(ok ? "Loaded: " + name : "Failed: " + name, ok);
            }
            lastConfigClicked = name;
            lastConfigClickMs = now;
            return true;
        }
        
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout l = layout();
        
        if (settingsOpen) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        
        if (configTab) {
            if (inside(mouseX, mouseY, l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH)) {
                configScroll -= verticalAmount * 20.0;
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        
        if (inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            moduleScroll -= verticalAmount * 24.0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private void refreshConfigList() {
        namedConfigs = SaltClient.CONFIG.listNamedConfigs();
        configScroll = 0.0;
    }

    private void setStatus(String text, boolean success) {
        statusText = text == null ? "" : text;
        statusColor = success ? 0xFF10B981 : 0xFFEF4444;
        statusUntilMs = System.currentTimeMillis() + 3000L;
    }

    private void syncWidgets(Layout l) {
        if (search != null) {
            search.setX(l.searchX + 16);
            search.setY(l.searchY + 10);
            search.setWidth(l.searchW - 40);
            search.setVisible(!configTab && !settingsOpen);
            search.setEditable(!configTab && !settingsOpen);
        }

        if (configName != null) {
            configName.setX(l.cfgNameX + 6);
            configName.setY(l.cfgNameY + 7);
            configName.setWidth(l.cfgNameW - 12);
            configName.setVisible(configTab && !settingsOpen);
            configName.setEditable(configTab && !settingsOpen);
        }

        if (configPath != null) {
            configPath.setX(l.cfgPathX + 6);
            configPath.setY(l.cfgPathY + 7);
            configPath.setWidth(l.cfgPathW - 12);
            configPath.setVisible(configTab && !settingsOpen);
            configPath.setEditable(configTab && !settingsOpen);
        }
    }

    private float animateToggle(Module module) {
        float target = module.isEnabled() ? 1.0f : 0.0f;
        float current = toggleAnim.getOrDefault(module.getId(), target);
        current += (target - current) * 0.25f;
        if (Math.abs(target - current) < 0.01f) current = target;
        toggleAnim.put(module.getId(), current);
        return current;
    }

    private float openProgress() {
        if (!GuiSettings.animationsEnabled()) return 1.0f;
        int speed = Math.max(1, GuiSettings.animationSpeedMs());
        float linear = (System.currentTimeMillis() - openedAtMs) / (float) speed;
        linear = (float) clamp(linear, 0.0, 1.0);
        float inv = 1.0f - linear;
        return 1.0f - inv * inv * inv;
    }

    private String trimToWidth(String text, int width) {
        if (text == null || text.isEmpty()) return "";
        if (this.textRenderer.getWidth(text) <= width) return text;

        String suffix = "...";
        int suffixW = this.textRenderer.getWidth(suffix);
        if (suffixW >= width) return suffix;

        int end = text.length();
        while (end > 0 && this.textRenderer.getWidth(text.substring(0, end)) + suffixW > width) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private String statusText() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int fps = mc == null ? 0 : mc.getCurrentFps();

        int ping = -1;
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        if (ping >= 0) return fps + " FPS  |  " + ping + " ms";
        return fps + " FPS";
    }

    private static int scaleAlpha(int color, float amount) {
        int a = (color >>> 24) & 0xFF;
        int scaled = Math.max(0, Math.min(255, Math.round(a * amount)));
        return (color & 0x00FFFFFF) | (scaled << 24);
    }

    private static int blend(int from, int to, float t) {
        t = (float) clamp(t, 0.0, 1.0);

        int a1 = (from >>> 24) & 0xFF;
        int r1 = (from >>> 16) & 0xFF;
        int g1 = (from >>> 8) & 0xFF;
        int b1 = from & 0xFF;

        int a2 = (to >>> 24) & 0xFF;
        int r2 = (to >>> 16) & 0xFF;
        int g2 = (to >>> 8) & 0xFF;
        int b2 = to & 0xFF;

        int a = Math.round(a1 + (a2 - a1) * t);
        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int b = Math.round(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}
