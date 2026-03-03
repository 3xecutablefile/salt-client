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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

@Environment(value = EnvType.CLIENT)
public class RankedLeaderboardHelper {
    private static final String API_BASE_URL = ServerConfig.getApiUrl();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static void getLeaderboard(String kitName, Consumer<List<LeaderboardEntry>> callback) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_BASE_URL + "/leaderboard?kit=" + kitName.replace(" ", "%20"))).GET().build();
        CompletableFuture<String> future = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
        future.thenAccept(body -> {
            ArrayList<LeaderboardEntry> entries = new ArrayList<>();
            try {
                JsonArray array = GSON.fromJson(body, JsonArray.class);
                int position = 1;
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    int elo = obj.has(kitName) ? obj.get(kitName).getAsInt() : 0;
                    entries.add(new LeaderboardEntry(name, elo, MatchContext.getRankDisplay(elo, position), MatchContext.getEloColor(elo, position), getRankItem(elo, position)));
                    ++position;
                }
                callback.accept(entries);
            } catch (Exception e) { callback.accept(new ArrayList<>()); }
        });
    }

    private static Item getRankItem(int elo, int pos) {
        if (pos <= 100 && elo >= 3400) return Items.DRAGON_BREATH;
        if (elo >= 3400) return Items.NETHERITE_INGOT;
        if (elo >= 2800) return Items.DIAMOND;
        if (elo >= 2400) return Items.EMERALD;
        if (elo >= 2000) return Items.AMETHYST_SHARD;
        if (elo >= 1600) return Items.GOLD_INGOT;
        if (elo >= 1200) return Items.LAPIS_LAZULI;
        if (elo >= 800) return Items.IRON_INGOT;
        if (elo >= 400) return Items.COAL;
        if (elo > 0) return Items.LEATHER;
        return Items.BARRIER;
    }

    @Environment(value = EnvType.CLIENT)
    public record LeaderboardEntry(String name, int elo, String rankName, Formatting color, Item icon) {}
}
