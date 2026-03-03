/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class MatchContext {
    public static KitType currentKit = null;
    public static KitType currentQueuedKit = null;
    public static boolean kitSelectedInMenu = false;
    public static KitType lastMatchKit = null;
    public static boolean inMatch = false;
    public static boolean isMatchActive = false;
    public static boolean isRanked = false;
    public static boolean rankedEnabled = true;
    public static boolean isBanned = false;
    public static String banReason = "";
    public static boolean wasInPartyBool = false;
    public static boolean duelRequestSent = false;
    public static String lastScore = "";
    public static String opponentName = "Unknown";
    public static float lastRoundHealth = 0.0f;
    public static int sessionKills = 0;
    public static int sessionDeaths = 0;
    public static boolean pendingAbandonmentPenalty = false;
    public static KitType abandonedKit = null;
    public static final Map<KitType, Integer> kitElo = new HashMap<>();
    public static final Map<KitType, Integer> kitMasteryXP = new HashMap<>();
    public static final Map<KitType, Integer> placementMatches = new HashMap<>();
    public static final Map<KitType, Integer> placementWins = new HashMap<>();
    public static final Map<KitType, Integer> totalMatches = new HashMap<>();
    public static final Map<KitType, Integer> totalWins = new HashMap<>();
    public static final Map<KitType, Integer> winStreak = new HashMap<>();
    public static final Map<String, Integer> playerEloByName = new HashMap<>();
    public static boolean statsPublic = true;

    public static boolean isAllowedServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) return true;
        ServerInfo serverInfo = client.getCurrentServerEntry();
        return serverInfo != null && ServerConfig.isSupportedServer();
    }

    public static void reset() { reset(false); }

    public static void reset(boolean cleanExit) {
        if (inMatch && isRanked && (currentKit != null || currentQueuedKit != null) && !cleanExit) {
            updateElo(currentKit != null ? currentKit : currentQueuedKit, false);
        }
        if (currentKit != null) lastMatchKit = currentKit;
        else if (currentQueuedKit != null) lastMatchKit = currentQueuedKit;
        currentKit = null;
        currentQueuedKit = null;
        kitSelectedInMenu = false;
        inMatch = false;
        isMatchActive = false;
        isRanked = false;
        duelRequestSent = false;
        lastScore = "";
        if (!opponentName.equals("Unknown")) playerEloByName.remove(opponentName);
        opponentName = "Unknown";
        lastRoundHealth = 0.0f;
    }

    public static void updateElo(KitType kit, boolean won) {
        if (kit == null) return;
        int xpGained = won ? 25 : 10;
        kitMasteryXP.put(kit, kitMasteryXP.getOrDefault(kit, 0) + xpGained);
        if (!isRanked) {
            StatsManager.save();
            StatsManager.pushToGlobal();
            return;
        }
        inMatch = false;
        isMatchActive = false;
        lastMatchKit = kit;
        totalMatches.put(kit, totalMatches.getOrDefault(kit, 0) + 1);
        if (won) totalWins.put(kit, totalWins.getOrDefault(kit, 0) + 1);
        float health = lastRoundHealth;
        if (won && health == 0.0f && MinecraftClient.getInstance().player != null) {
            health = MinecraftClient.getInstance().player.getHealth();
        }
        StatsManager.save();
        StatsManager.pushToGlobal();
        StatsManager.pushMatchResult(kit, won, health, opponentName);
        if (MinecraftClient.getInstance().player != null) {
            Formatting resultColor = won ? Formatting.GREEN : Formatting.RED;
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§8§m----------------------------------------"), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§6§lRANKED MATCH SUMMARY"), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§7Opponent: §f" + opponentName), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§7Result: ").append(Text.literal(won ? "WIN" : "LOSS").formatted(resultColor)), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§7Mastery XP: §a+" + xpGained + " §8(§f" + kitMasteryXP.get(kit) + " Total§8)"), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§e§oSyncing ELO with server..."), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§8§m----------------------------------------"), false);
        }
    }

    public static int getMasteryLevel(int xp) { return (int) Math.floor(Math.sqrt(xp / 10.0)) + 1; }
    public static int getXPForLevel(int level) { return level <= 1 ? 0 : (int) (Math.pow(level - 1, 2.0) * 10.0); }

    public static Identifier getRankIcon(int elo, int leaderboardPos) {
        String name = "barrier";
        if (leaderboardPos <= 100 && elo >= 3400) name = "dragon_breath";
        else if (elo >= 3400) name = "netherite_ingot";
        else if (elo >= 2800) name = "diamond";
        else if (elo >= 2400) name = "emerald";
        else if (elo >= 2000) name = "amethyst_shard";
        else if (elo >= 1600) name = "gold_ingot";
        else if (elo >= 1200) name = "lapis_lazuli";
        else if (elo >= 800) name = "iron_ingot";
        else if (elo >= 400) name = "coal";
        else if (elo > 0) name = "leather";
        return Identifier.of("textures/item/" + name + ".png");
    }

    public static Identifier getMasteryIconPath(KitType kit, int level) {
        switch (kit.ordinal()) {
            case 0: return level >= 51 ? Identifier.of("textures/item/netherite_sword.png") : level >= 41 ? Identifier.of("textures/item/diamond_sword.png") : level >= 31 ? Identifier.of("textures/item/golden_sword.png") : level >= 21 ? Identifier.of("textures/item/iron_sword.png") : level >= 11 ? Identifier.of("textures/item/stone_sword.png") : Identifier.of("textures/item/wooden_sword.png");
            case 1: return level >= 51 ? Identifier.of("textures/item/netherite_axe.png") : level >= 41 ? Identifier.of("textures/item/diamond_axe.png") : level >= 31 ? Identifier.of("textures/item/golden_axe.png") : level >= 21 ? Identifier.of("textures/item/iron_axe.png") : level >= 11 ? Identifier.of("textures/item/stone_axe.png") : Identifier.of("textures/item/wooden_axe.png");
            case 2: return level >= 41 ? Identifier.of("textures/item/enchanted_golden_apple.png") : level >= 21 ? Identifier.of("textures/item/golden_apple.png") : Identifier.of("textures/item/apple.png");
            case 3: return level >= 21 ? Identifier.of("textures/item/mace.png") : Identifier.of("textures/item/heavy_core.png");
            case 4: return Identifier.of("textures/item/trident.png");
            case 5: return level >= 41 ? Identifier.of("textures/item/netherite_chestplate.png") : level >= 21 ? Identifier.of("textures/item/netherite_ingot.png") : Identifier.of("textures/item/netherite_scrap.png");
            case 6: return level >= 31 ? Identifier.of("textures/item/lingering_potion.png") : level >= 21 ? Identifier.of("textures/item/splash_potion.png") : level >= 11 ? Identifier.of("textures/item/potion.png") : Identifier.of("textures/item/glass_bottle.png");
            case 7: return Identifier.of("textures/item/ender_pearl.png");
            case 8: return level >= 41 ? Identifier.of("textures/item/nether_star.png") : level >= 21 ? Identifier.of("textures/item/end_crystal.png") : Identifier.of("textures/item/ghast_tear.png");
            default: return Identifier.of("textures/item/wooden_shovel.png");
        }
    }

    public static String getRankDisplay(int elo, int leaderboardPos) {
        if (leaderboardPos <= 100 && elo >= 3400) return "Dragon";
        if (elo >= 3400) return "Netherite";
        if (elo >= 2800) return "Diamond";
        if (elo >= 2400) return "Emerald";
        if (elo >= 2000) return "Amethyst";
        if (elo >= 1600) return "Gold";
        if (elo >= 1200) return "Lapis";
        if (elo >= 800) return "Iron";
        if (elo >= 400) return "Coal";
        if (elo > 0) return "Leather";
        return "Unranked";
    }

    public static Formatting getEloColor(int elo, int leaderboardPos) {
        if (leaderboardPos <= 100 && elo >= 3400) return Formatting.LIGHT_PURPLE;
        if (elo >= 3400) return Formatting.DARK_PURPLE;
        if (elo >= 2800) return Formatting.AQUA;
        if (elo >= 2400) return Formatting.GREEN;
        if (elo >= 2000) return Formatting.DARK_GREEN;
        if (elo >= 1600) return Formatting.GOLD;
        if (elo >= 1200) return Formatting.BLUE;
        if (elo >= 800) return Formatting.WHITE;
        if (elo >= 400) return Formatting.DARK_GRAY;
        if (elo > 0) return Formatting.GOLD;
        return Formatting.RED;
    }

    static {
        for (KitType type : KitType.values()) {
            kitElo.put(type, 0);
            kitMasteryXP.put(type, 0);
            placementMatches.put(type, 0);
            placementWins.put(type, 0);
            totalMatches.put(type, 0);
            totalWins.put(type, 0);
            winStreak.put(type, 0);
        }
    }

    @Environment(value = EnvType.CLIENT)
    public enum KitType {
        SWORD("Sword", Items.DIAMOND_SWORD, Identifier.of("mcpvp", "textures/item/sword_icon.png")),
        AXE("Axe", Items.DIAMOND_AXE, Identifier.of("mcpvp", "textures/item/axe_icon.png")),
        UHC("UHC", Items.ENCHANTED_GOLDEN_APPLE, Identifier.of("mcpvp", "textures/item/uhc_icon.png")),
        MACE("Mace", Items.MACE, Identifier.of("mcpvp", "textures/item/mace_icon.png")),
        SPEAR("Spear PVP", Items.TRIDENT, Identifier.of("mcpvp", "textures/item/spear.png")),
        NETHERITE_OP("Netherite OP", Items.NETHERITE_HELMET, Identifier.of("mcpvp", "textures/item/netheriteop_icon.png")),
        POT("Pot", Items.SPLASH_POTION, Identifier.of("textures/item/potion.png")),
        SMP("SMP", Items.ENDER_PEARL, Identifier.of("textures/item/ender_pearl.png")),
        CRYSTAL("Crystal", Items.OBSIDIAN, Identifier.of("mcpvp", "textures/item/crystal_icon.png"));

        public final String apiName;
        public final Item icon;
        public final Identifier customTexture;

        KitType(String apiName, Item icon, Identifier customTexture) {
            this.apiName = apiName;
            this.icon = icon;
            this.customTexture = customTexture;
        }

        public static KitType fromItem(Item item) {
            if (item == Items.DIAMOND_SWORD) return SWORD;
            if (item == Items.DIAMOND_AXE) return AXE;
            if (item == Items.MACE) return MACE;
            if (item == Items.ENCHANTED_GOLDEN_APPLE) return UHC;
            if (item == Items.TRIDENT) return SPEAR;
            if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE) return NETHERITE_OP;
            if (item == Items.SPLASH_POTION) return POT;
            if (item == Items.ENDER_PEARL || item == Items.SHIELD) return SMP;
            if (item == Items.OBSIDIAN) return CRYSTAL;
            return null;
        }

        public static KitType fromName(String name) {
            for (KitType kit : values()) {
                if (kit.apiName.equalsIgnoreCase(name)) return kit;
            }
            return null;
        }
    }
}
