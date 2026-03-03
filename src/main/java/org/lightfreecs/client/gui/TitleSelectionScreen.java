/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(value = EnvType.CLIENT)
public class TitleSelectionScreen extends Screen {
    private final Screen parent;
    public TitleSelectionScreen(Screen parent) { super(Text.literal("Select Title")); this.parent = parent; }
    @Override public void close() { this.client.setScreen(this.parent); }
}
