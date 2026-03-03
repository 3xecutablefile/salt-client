/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class ServerConfig {
    public static final String MCPVP = "mcpvp.club";
    public static final String MINEMEN = "minemen.club";
    public static final String CATPVP = "catpvp.xyz";
    private static final String API_BASE = "https://api.mcpvp.club";
    private static final String API_KEY = "zDgCl0*a~hWoP0#0Np;X";

    public static String getApiUrl() { return API_BASE; }
    public static String getApiKey() { return API_KEY; }
    public static String getDisplayName() { return "MCPVP"; }

    public static boolean isSupportedServer() {
        String server = getCurrentServer();
        return MCPVP.equals(server) || MINEMEN.equals(server) || CATPVP.equals(server);
    }

    public static String getCurrentServer() {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.getCurrentServerEntry() == null) return "";
        return client.getCurrentServerEntry().address.toLowerCase();
    }
}
