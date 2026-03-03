/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.MatchContext;

@Environment(value = EnvType.CLIENT)
public class CosmeticScreen extends Screen {
    private final Screen parent;
    private boolean showingKits = false;

    public CosmeticScreen(Screen parent) {
        super(Text.literal("Cosmetics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        if (!this.showingKits) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Select Title").formatted(Formatting.LIGHT_PURPLE), button -> this.client.setScreen(new TitleSelectionScreen(this))).dimensions(centerX - 100, centerY - 40, 200, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Item Cosmetics").formatted(Formatting.BLUE), button -> { this.showingKits = true; this.init(); }).dimensions(centerX - 100, centerY - 15, 200, 20).build());
        } else {
            int y = centerY - 40;
            for (MatchContext.KitType kit : new MatchContext.KitType[]{MatchContext.KitType.SWORD, MatchContext.KitType.AXE, MatchContext.KitType.NETHERITE_OP}) {
                this.addDrawableChild(ButtonWidget.builder(Text.literal(kit.apiName + " Kit"), button -> this.client.setScreen(new SwordCosmeticScreen(this, kit))).dimensions(centerX - 80, y, 160, 20).build());
                y += 25;
            }
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> { if (this.showingKits) { this.showingKits = false; this.init(); } else { this.client.setScreen(this.parent); } }).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        context.drawText(textRenderer, Text.literal("Cosmetics").formatted(Formatting.BOLD), centerX, 20, 0xFFFFFF, true);
        if (this.showingKits) {
            context.drawText(textRenderer, Text.literal("§6§lKits"), centerX, centerY - 55, 0xFFFFFF, true);
            int y = centerY - 38;
            for (MatchContext.KitType kit : new MatchContext.KitType[]{MatchContext.KitType.SWORD, MatchContext.KitType.AXE, MatchContext.KitType.NETHERITE_OP}) {
                context.drawTexture(id -> RenderLayer.getGuiTextured(id), kit.customTexture, centerX - 105, y, 0, 0, 16, 16, 16, 16);
                y += 25;
            }
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
