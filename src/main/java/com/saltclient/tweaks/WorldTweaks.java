package com.saltclient.tweaks;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class WorldTweaks {
    private WorldTweaks() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null) return;
        ClientWorld w = mc.world;
        if (w == null) return;

        if (SaltClient.MODULES.isEnabled("timechanger")) {
            // Time change disabled for 1.21.4 - ClientWorld.setTimeOfDay API changed
        }

        if (SaltClient.MODULES.isEnabled("weatherdisabler")) {
            w.getLevelProperties().setRaining(false);
        } else if (SaltClient.MODULES.isEnabled("weatherchanger")) {
            w.getLevelProperties().setRaining(true);
        }
    }
}
