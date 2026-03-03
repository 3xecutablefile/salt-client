/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

@Environment(value = EnvType.CLIENT)
public class ServerPreloader {
    public static void preload() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerList serverList = new ServerList(client);
        addServerIfMissing(serverList, "MCPVP CLUB", ServerConfig.MCPVP);
        addServerIfMissing(serverList, "MINEMEN CLUB", ServerConfig.MINEMEN);
        addServerIfMissing(serverList, "CATPVP XYZ", ServerConfig.CATPVP);
    }

    private static void addServerIfMissing(ServerList serverList, String name, String address) {
        for (int i = 0; i < serverList.size(); ++i) {
            if (serverList.get(i).address.equalsIgnoreCase(address)) return;
        }
        serverList.add(new ServerInfo(name, address, ServerInfo.ServerType.OTHER), false);
    }
}
