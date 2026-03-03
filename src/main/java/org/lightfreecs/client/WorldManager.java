/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;

@Environment(value = EnvType.CLIENT)
public class WorldManager {
    public static ClientWorld PERSISTENT_WORLD = null;
}
