/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(value = EnvType.CLIENT)
public class PlayerStatsScreen extends Screen {
    private final Screen parent;
    private final String playerName;
    public PlayerStatsScreen(Screen parent, String playerName) { super(Text.literal(playerName + "'s Stats")); this.parent = parent; this.playerName = playerName; }
    @Override public void close() { this.client.setScreen(this.parent); }
}
