/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(value = EnvType.CLIENT)
public class HowToPlayScreen extends Screen {
    private final Screen parent;

    public HowToPlayScreen(Screen parent) {
        super(Text.literal("How to Play"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
