/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.CosmeticManager;
import org.lightfreecs.client.MatchContext;

@Environment(value = EnvType.CLIENT)
public class SwordCosmeticScreen extends Screen {
    private final Screen parent;
    private final MatchContext.KitType kit;
    private static final Map<String, Integer> COSMETIC_RANKS = new LinkedHashMap<>();

    public SwordCosmeticScreen(Screen parent, MatchContext.KitType kit) {
        super(Text.literal(kit.apiName + " Cosmetics"));
        this.parent = parent;
        this.kit = kit;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        int kitPeakElo = CosmeticManager.peakElo.getOrDefault(this.kit.apiName, 0);
        int currentElo = MatchContext.kitElo.getOrDefault(this.kit, 0);
        int unlockElo = Math.max(kitPeakElo, currentElo);
        for (Map.Entry<String, Integer> entry : COSMETIC_RANKS.entrySet()) {
            String id = entry.getKey();
            int requiredElo = entry.getValue();
            if (unlockElo < requiredElo) continue;
            boolean isSelected = CosmeticManager.getSelectedCosmetic(this.kit.apiName).equals(id);
            String displayName = id.substring(0, 1).toUpperCase() + id.substring(1) + " Sword";
            if (id.equals("default")) displayName = "Default";
            Text buttonText = Text.literal(displayName);
            if (isSelected) buttonText = Text.literal(displayName + " ✔").formatted(Formatting.GREEN);
            this.addDrawableChild(ButtonWidget.builder(buttonText, button -> { CosmeticManager.updateKitCosmetic(this.kit.apiName, id); this.client.setScreen(new SwordCosmeticScreen(this.parent, this.kit)); }).dimensions(centerX - 100, y, 200, 20).build());
            y += 22;
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> this.client.setScreen(this.parent)).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, Text.literal(this.kit.apiName + " Cosmetics").formatted(Formatting.BOLD), this.width / 2 - 60, 20, 0xFFFFFF, true);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    static {
        COSMETIC_RANKS.put("default", 0);
        COSMETIC_RANKS.put("wood", 1);
        COSMETIC_RANKS.put("iron", 800);
        COSMETIC_RANKS.put("gold", 1600);
        COSMETIC_RANKS.put("netherite", 3400);
    }
}
