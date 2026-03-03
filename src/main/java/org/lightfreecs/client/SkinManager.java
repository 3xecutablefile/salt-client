/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;

@Environment(value = EnvType.CLIENT)
public class SkinManager {
    private static final Map<String, SkinTextures> cache = new HashMap<>();
    private static final Set<String> fetching = new HashSet<>();
    private static final Gson GSON = new Gson();

    public static SkinTextures getSkin(String name) {
        PlayerListEntry entry;
        if (cache.containsKey(name)) return cache.get(name);
        MinecraftClient client = MinecraftClient.getInstance();
        if (name.equals(client.player.getName().getString()) && client.player != null) {
            SkinTextures textures = client.getSkinProvider().getSkinTextures(client.player.getGameProfile());
            cache.put(name, textures); return textures;
        }
        if (client.getNetworkHandler() != null && (entry = client.getNetworkHandler().getPlayerListEntry(name)) != null) {
            SkinTextures textures = entry.getSkinTextures();
            cache.put(name, textures); return textures;
        }
        fetchSkinAsync(name);
        return null;
    }

    private static void fetchSkinAsync(String name) {
        if (fetching.contains(name)) return;
        fetching.add(name);
        CompletableFuture.runAsync(() -> {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                HttpURLConnection connection = (HttpURLConnection) URI.create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openConnection();
                if (connection.getResponseCode() == 200) {
                    JsonObject json = GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
                    UUID uuid = UUID.fromString(json.get("id").getAsString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
                    ProfileResult result = client.getSessionService().fetchProfile(uuid, true);
                    if (result != null) {
                        SkinTextures textures = client.getSkinProvider().getSkinTextures(result.profile());
                        cache.put(name, textures);
                    }
                }
            } catch (Exception ignored) {}
            finally { fetching.remove(name); }
        });
    }

    public static void clearCache() { cache.clear(); }
}
