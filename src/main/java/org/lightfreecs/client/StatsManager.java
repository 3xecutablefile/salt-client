/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(value = EnvType.CLIENT)
public class StatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    private static final HttpClient HTTP_CLIENT;
    private static final Set<String> fetchingPlayers;
    private static boolean hasSuccessfullyPulled;
    private static boolean isSyncing;
    public static final Map<String, Map<String, String>> playerKitCosmetics;

    public static void init() {
        configFile = new File(MinecraftClient.getInstance().runDirectory, "config/ranked_stats.json");
        StatsManager.load();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject root = new JsonObject();
            root.add("elo", GSON.toJsonTree(MatchContext.kitElo));
            root.add("mastery_xp", GSON.toJsonTree(MatchContext.kitMasteryXP));
            root.add("matches", GSON.toJsonTree(MatchContext.placementMatches));
            root.add("wins", GSON.toJsonTree(MatchContext.placementWins));
            root.add("total_matches", GSON.toJsonTree(MatchContext.totalMatches));
            root.add("total_wins", GSON.toJsonTree(MatchContext.totalWins));
            root.add("win_streaks", GSON.toJsonTree(MatchContext.winStreak));
            root.addProperty("public", MatchContext.statsPublic);
            root.addProperty("ranked_enabled", MatchContext.rankedEnabled);
            root.addProperty("pending_abandonment_penalty", MatchContext.pendingAbandonmentPenalty);
            if (MatchContext.abandonedKit != null) {
                root.addProperty("abandoned_kit", MatchContext.abandonedKit.name());
            }
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null) {
                Type type = new TypeToken<Map<MatchContext.KitType, Integer>>() {}.getType();
                if (root.has("elo")) {
                    Map<MatchContext.KitType, Integer> elo = GSON.fromJson(root.get("elo"), type);
                    if (elo != null) MatchContext.kitElo.putAll(elo);
                }
                if (root.has("mastery_xp")) {
                    Map<MatchContext.KitType, Integer> mastery = GSON.fromJson(root.get("mastery_xp"), type);
                    if (mastery != null) MatchContext.kitMasteryXP.putAll(mastery);
                }
                if (root.has("matches")) {
                    Map<MatchContext.KitType, Integer> matches = GSON.fromJson(root.get("matches"), type);
                    if (matches != null) MatchContext.placementMatches.putAll(matches);
                }
                if (root.has("wins")) {
                    Map<MatchContext.KitType, Integer> wins = GSON.fromJson(root.get("wins"), type);
                    if (wins != null) MatchContext.placementWins.putAll(wins);
                }
                if (root.has("total_matches")) {
                    Map<MatchContext.KitType, Integer> totalMatches = GSON.fromJson(root.get("total_matches"), type);
                    if (totalMatches != null) MatchContext.totalMatches.putAll(totalMatches);
                }
                if (root.has("total_wins")) {
                    Map<MatchContext.KitType, Integer> totalWins = GSON.fromJson(root.get("total_wins"), type);
                    if (totalWins != null) MatchContext.totalWins.putAll(totalWins);
                }
                if (root.has("win_streaks")) {
                    Map<MatchContext.KitType, Integer> streaks = GSON.fromJson(root.get("win_streaks"), type);
                    if (streaks != null) MatchContext.winStreak.putAll(streaks);
                }
                if (root.has("public")) MatchContext.statsPublic = root.get("public").getAsBoolean();
                if (root.has("ranked_enabled")) MatchContext.rankedEnabled = root.get("ranked_enabled").getAsBoolean();
                if (root.has("pending_abandonment_penalty")) MatchContext.pendingAbandonmentPenalty = root.get("pending_abandonment_penalty").getAsBoolean();
                if (root.has("abandoned_kit")) MatchContext.abandonedKit = MatchContext.KitType.valueOf(root.get("abandoned_kit").getAsString());
            }
        } catch (Exception ignored) {}
    }

    public static void syncWithServer() {
        String playerName = MinecraftClient.getInstance().player.getName().getString();
        if (playerName == null || playerName.equals("Player") || isSyncing) return;
        isSyncing = true;
        StatsManager.fetchFullPlayerStats(playerName, stats -> {
            if (stats != null) {
                MatchContext.isBanned = stats.isBanned();
                MatchContext.banReason = stats.banReason();
                if (stats.selectedCosmetics() != null) {
                    synchronized (CosmeticManager.selectedCosmetics) {
                        CosmeticManager.selectedCosmetics.clear();
                        CosmeticManager.selectedCosmetics.putAll(stats.selectedCosmetics());
                    }
                }
                if (stats.kitElo() != null) {
                    for (Map.Entry<MatchContext.KitType, Integer> entry : stats.kitElo().entrySet()) {
                        MatchContext.kitElo.put(entry.getKey(), entry.getValue());
                    }
                }
                if (stats.kitPlacements() != null) {
                    for (Map.Entry<MatchContext.KitType, Integer> entry : stats.kitPlacements().entrySet()) {
                        MatchContext.placementMatches.put(entry.getKey(), entry.getValue());
                    }
                }
                if (stats.totalMatches() != null) MatchContext.totalMatches.putAll(stats.totalMatches());
                if (stats.totalWins() != null) MatchContext.totalWins.putAll(stats.totalWins());
                if (stats.kitMasteryXP() != null) MatchContext.kitMasteryXP.putAll(stats.kitMasteryXP());
                MatchContext.statsPublic = stats.isPublic();
                hasSuccessfullyPulled = true;
                StatsManager.save();
            }
            isSyncing = false;
        });
    }

    public static void pushToGlobal() {
        if (!hasSuccessfullyPulled) {
            StatsManager.syncWithServer();
            return;
        }
        String playerName = MinecraftClient.getInstance().player.getName().getString();
        if (playerName == null || playerName.equals("Player")) return;
        HashMap<String, Object> apiPayload = new HashMap<>();
        apiPayload.put("public", MatchContext.statsPublic);
        apiPayload.put("version", "1.4.0-Beta");
        HashMap<String, Integer> masteryMap = new HashMap<>();
        for (Map.Entry<MatchContext.KitType, Integer> entry : MatchContext.kitMasteryXP.entrySet()) {
            masteryMap.put(entry.getKey().apiName, entry.getValue());
        }
        apiPayload.put("mastery_xp", masteryMap);
        String json = GSON.toJson(apiPayload);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/update_settings?name=" + playerName))
            .header("Content-Type", "application/json")
            .header("X-API-Key", ServerConfig.getApiKey())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void fetchFullPlayerStats(String playerName, Consumer<PlayerStats> callback) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/get_elo?name=" + playerName))
            .header("X-API-Key", ServerConfig.getApiKey())
            .GET()
            .build();
        CompletableFuture<String> future = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            HashMap<MatchContext.KitType, Integer> elos = new HashMap<>();
            HashMap<MatchContext.KitType, Integer> mastery = new HashMap<>();
            HashMap<MatchContext.KitType, Integer> placements = new HashMap<>();
            HashMap<MatchContext.KitType, Integer> totalMatches = new HashMap<>();
            HashMap<MatchContext.KitType, Integer> totalWins = new HashMap<>();
            HashMap<String, String> cosmetics = new HashMap<>();
            ArrayList<MatchHistoryEntry> history = new ArrayList<>();
            boolean isBanned = false;
            String banReason = "";
            String selectedTitle = "";
            try {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                if (json.has("is_banned")) isBanned = json.get("is_banned").getAsBoolean();
                if (json.has("ban_reason")) banReason = json.get("ban_reason").getAsString();
                if (json.has("selected_title")) selectedTitle = json.get("selected_title").getAsString();
                if (json.has("selected_cosmetics")) {
                    JsonObject cosJson = json.getAsJsonObject("selected_cosmetics");
                    for (Map.Entry<String, JsonElement> entry : cosJson.entrySet()) {
                        cosmetics.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                if (json.has("history")) {
                    JsonArray historyArray = json.getAsJsonArray("history");
                    for (JsonElement e : historyArray) {
                        JsonObject obj = e.getAsJsonObject();
                        history.add(new MatchHistoryEntry(obj.get("opponent").getAsString(), obj.get("kit").getAsString(), obj.get("won").getAsBoolean(), "", String.valueOf(obj.get("health").getAsFloat()), obj.get("date").getAsString()));
                    }
                }
                JsonObject masteryJson = json.has("mastery_xp") ? json.getAsJsonObject("mastery_xp") : null;
                JsonObject totalMatchesJson = json.has("total_matches") ? json.getAsJsonObject("total_matches") : null;
                JsonObject totalWinsJson = json.has("total_wins") ? json.getAsJsonObject("total_wins") : null;
                JsonObject placementStats = json.has("placement_stats") ? json.getAsJsonObject("placement_stats") : null;
                JsonObject placementMatches = placementStats != null && placementStats.has("matches") ? placementStats.getAsJsonObject("matches") : null;
                for (MatchContext.KitType kit : MatchContext.KitType.values()) {
                    elos.put(kit, json.has(kit.apiName) ? json.get(kit.apiName).getAsInt() : 0);
                    mastery.put(kit, masteryJson != null && masteryJson.has(kit.apiName) ? masteryJson.get(kit.apiName).getAsInt() : 0);
                    placements.put(kit, placementMatches != null && placementMatches.has(kit.apiName) ? placementMatches.get(kit.apiName).getAsInt() : 0);
                    totalMatches.put(kit, totalMatchesJson != null && totalMatchesJson.has(kit.apiName) ? totalMatchesJson.get(kit.apiName).getAsInt() : 0);
                    totalWins.put(kit, totalWinsJson != null && totalWinsJson.has(kit.apiName) ? totalWinsJson.get(kit.apiName).getAsInt() : 0);
                }
                callback.accept(new PlayerStats(playerName, elos, mastery, placements, totalMatches, totalWins, true, history, selectedTitle, cosmetics, isBanned, banReason));
            } catch (Exception e) {
                callback.accept(null);
            }
        });
    }

    public static void fetchLeaderboard(MatchContext.KitType kit, Consumer<List<LeaderboardEntry>> callback) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/leaderboard?kit=" + kit.apiName.replace(" ", "%20")))
            .GET()
            .build();
        CompletableFuture<String> future = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            ArrayList<LeaderboardEntry> entries = new ArrayList<>();
            try {
                JsonArray array = GSON.fromJson(body, JsonArray.class);
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    int elo = obj.has(kit.apiName) ? obj.get(kit.apiName).getAsInt() : 0;
                    String title = obj.has("selected_title") ? obj.get("selected_title").getAsString() : "";
                    HashMap<String, String> cosmetics = new HashMap<>();
                    if (obj.has("selected_cosmetics")) {
                        JsonObject cosJson = obj.getAsJsonObject("selected_cosmetics");
                        for (Map.Entry<String, JsonElement> cosEntry : cosJson.entrySet()) {
                            cosmetics.put(cosEntry.getKey(), cosEntry.getValue().getAsString());
                        }
                    }
                    entries.add(new LeaderboardEntry(name, elo, title, cosmetics));
                    playerKitCosmetics.put(name, cosmetics);
                }
            } catch (Exception ignored) {}
            entries.sort((a, b) -> Integer.compare(b.elo(), a.elo()));
            callback.accept(entries);
        });
    }

    public static void pushMatchResult(MatchContext.KitType kit, boolean won, float health, String opponent) {
        String playerName = MinecraftClient.getInstance().player.getName().getString();
        JsonObject matchData = new JsonObject();
        matchData.addProperty("opponent", opponent);
        matchData.addProperty("kit", kit.apiName);
        matchData.addProperty("won", won);
        matchData.addProperty("health", health);
        matchData.addProperty("version", "1.4.0-Beta");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/add_history?name=" + playerName))
            .header("Content-Type", "application/json")
            .header("X-API-Key", ServerConfig.getApiKey())
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(matchData)))
            .build();
        CompletableFuture<String> future = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            try {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                if (json.has("status") && json.get("status").getAsString().equals("ok")) {
                    int oldElo = json.get("old_elo").getAsInt();
                    int newElo = json.get("new_elo").getAsInt();
                    int baseChange = json.get("base_change").getAsInt();
                    int bonus = json.get("bonus").getAsInt();
                    int placements = json.get("placements").getAsInt();
                    MinecraftClient.getInstance().execute(() -> EloBarRenderer.show(oldElo, newElo, baseChange, bonus, placements));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            CompletableFuture.delayedExecutor(2L, TimeUnit.SECONDS).execute(StatsManager::syncWithServer);
        });
    }

    public static void fetchPlayerElo(String playerName) {
        if (fetchingPlayers.contains(playerName)) return;
        fetchingPlayers.add(playerName);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/get_elo?name=" + playerName))
            .header("X-API-Key", ServerConfig.getApiKey())
            .GET()
            .build();
        CompletableFuture<String> future = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            try {
                JsonObject json = GSON.fromJson(body, JsonObject.class);
                String kitName = MatchContext.currentKit != null ? MatchContext.currentKit.apiName : "Sword";
                if (json.has(kitName)) MatchContext.playerEloByName.put(playerName, json.get(kitName).getAsInt());
                if (json.has("selected_cosmetics")) {
                    JsonObject cosJson = json.getAsJsonObject("selected_cosmetics");
                    HashMap<String, String> cosmetics = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : cosJson.entrySet()) {
                        cosmetics.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    playerKitCosmetics.put(playerName, cosmetics);
                }
            } catch (Exception ignored) {}
            finally {
                fetchingPlayers.remove(playerName);
            }
        });
    }

    public static void pushMatchStart() {
        String playerName = MinecraftClient.getInstance().player.getName().getString();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ServerConfig.getApiUrl() + "/match_start?name=" + playerName + "&version=1.4.0-Beta"))
            .header("X-API-Key", ServerConfig.getApiKey())
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    static {
        HTTP_CLIENT = HttpClient.newHttpClient();
        fetchingPlayers = new HashSet<>();
        hasSuccessfullyPulled = false;
        isSyncing = false;
        playerKitCosmetics = new HashMap<>();
    }

    @Environment(value = EnvType.CLIENT)
    public record LeaderboardEntry(String name, int elo, String selectedTitle, Map<String, String> selectedCosmetics) {}

    @Environment(value = EnvType.CLIENT)
    public record MatchHistoryEntry(String opponent, String kit, boolean won, String eloChange, String health, String date) {}

    @Environment(value = EnvType.CLIENT)
    public record PlayerStats(String name, Map<MatchContext.KitType, Integer> kitElo, Map<MatchContext.KitType, Integer> kitMasteryXP, Map<MatchContext.KitType, Integer> kitPlacements, Map<MatchContext.KitType, Integer> totalMatches, Map<MatchContext.KitType, Integer> totalWins, boolean isPublic, List<MatchHistoryEntry> history, String selectedTitle, Map<String, String> selectedCosmetics, boolean isBanned, String banReason) {}
}
