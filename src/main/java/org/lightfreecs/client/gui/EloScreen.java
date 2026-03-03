/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(value = EnvType.CLIENT)
public class EloScreen extends Screen {
    public EloScreen() { super(Text.literal("ELO Status")); }
    @Override public void close() { this.client.setScreen(null); }
}
