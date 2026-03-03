/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value = EnvType.CLIENT)
public class VersionManager {
    private static final String CURRENT_VERSION = "1.4.0-Beta";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static void init() {}

    public static void checkVersion() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ServerConfig.getApiUrl() + "/version")).GET().build();
        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(body -> {
            try {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                String latest = json.get("latest").getAsString();
                if (!CURRENT_VERSION.equals(latest) && MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§c§l[RankedMod] §fA new version is available! Current: " + CURRENT_VERSION + ", Latest: " + latest).formatted(Formatting.RED), false);
                }
            } catch (Exception ignored) {}
        });
    }
}
