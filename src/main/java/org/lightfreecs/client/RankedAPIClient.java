/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class RankedAPIClient {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static final String API_KEY = "zDgCl0*a~hWoP0#0Np;X";

    public static void registerPlayer(String playerName) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ServerConfig.getApiUrl() + "/register?name=" + playerName)).header("X-API-Key", ServerConfig.getApiKey()).POST(HttpRequest.BodyPublishers.noBody()).build();
        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void fetchAllStats(String playerName, Consumer<Map<String, Integer>> callback) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ServerConfig.getApiUrl() + "/get_elo?name=" + playerName)).GET().build();
        CompletableFuture<String> future = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            try {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                HashMap<String, Integer> stats = new HashMap<>();
                for (String kit : new String[]{"Sword", "Axe", "UHC", "Mace", "Spear PVP", "Netherite OP", "Pot", "SMP", "Crystal"}) {
                    stats.put(kit, json.has(kit) ? json.get(kit).getAsInt() : 0);
                }
                callback.accept(stats);
            } catch (Exception e) { callback.accept(null); }
        });
    }

    @Deprecated public static void updateStats(String playerName, Map<String, Integer> kitEloMap, boolean isPublic) {}

    public static void fetchLeaderboard(String kitName, Consumer<JsonArray> callback) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ServerConfig.getApiUrl() + "/leaderboard?kit=" + kitName.replace(" ", "%20"))).GET().build();
        CompletableFuture<String> future = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> { try { callback.accept(GSON.fromJson(body, JsonArray.class)); } catch (Exception e) { callback.accept(new JsonArray()); } });
    }
}
