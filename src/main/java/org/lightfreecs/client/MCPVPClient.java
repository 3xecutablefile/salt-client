/*
 * Remapped to yarn mappings for 1.21.4
 */
package org.lightfreecs.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lightfreecs.client.gui.EloScreen;

@Environment(value = EnvType.CLIENT)
public class MCPVPClient implements ClientModInitializer {
    private static KeyBinding eloKeyBinding;

    public void onInitializeClient() {
        StatsManager.init();
        KitIdentifier.init();
        CosmeticManager.init();
        VersionManager.init();
        ServerPreloader.preload();
        StatsManager.syncWithServer();
        ModelLoadingPlugin.register(new ModelLoaderPlugin());
        eloKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mcpvp.elo_status", InputUtil.Type.KEYSYM, 74, "category.mcpvp.ranked"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (eloKeyBinding.wasPressed()) {
                if (client.inGameHud != null) continue;
                client.setScreen(new EloScreen());
            }
        });
        ClientSendMessageEvents.COMMAND.register(command -> {
            String baseCommand = command.split(" ")[0].toLowerCase();
            if ((baseCommand.equals("leave") || baseCommand.equals("resign")) && MatchContext.inMatch && MatchContext.isRanked) {
                MatchContext.KitType kit = MatchContext.currentKit != null ? MatchContext.currentKit : MatchContext.currentQueuedKit;
                if (kit != null) {
                    MatchContext.updateElo(kit, false);
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) client.player.sendMessage(Text.literal("§c§l[RankedMod - " + ServerConfig.getDisplayName() + "] §fMatch resigned via /" + baseCommand + ". Loss recorded.").formatted(Formatting.RED), false);
                }
                MatchContext.reset(true);
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("ranked").then(ClientCommandManager.literal("status").executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            String kitName = MatchContext.currentKit != null ? MatchContext.currentKit.name() : "None";
            String score = !MatchContext.lastScore.isEmpty() ? MatchContext.lastScore : "0 - 0";
            String serverStatus = client.getCurrentServerEntry() != null && ServerConfig.isSupportedServer() ? "Verified (" + ServerConfig.getCurrentServer() + ")" : "Not Connected to supported server";
            ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal("--- Ranked Mod Status ---").formatted(Formatting.GOLD));
            ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal("Current Detected Kit: ").formatted(Formatting.GRAY).append(Text.literal(kitName).formatted(Formatting.WHITE)));
            ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal("Current Session Score: ").formatted(Formatting.GRAY).append(Text.literal(score).formatted(Formatting.WHITE)));
            ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal("Server Connection: ").formatted(Formatting.GRAY).append(Text.literal(serverStatus).formatted(Formatting.WHITE)));
            ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal("--- Your ELO Stats ---").formatted(Formatting.GOLD));
            for (var entry : MatchContext.kitElo.entrySet()) {
                ((FabricClientCommandSource) context.getSource()).sendFeedback(Text.literal(entry.getKey().name() + ": ").formatted(Formatting.GRAY).append(Text.literal(entry.getValue().toString()).formatted(Formatting.WHITE)).append(Text.literal(" (" + MatchContext.getRankDisplay(entry.getValue(), 101) + ")")));
            }
            return 1;
        }))));
    }

    public static void debugLog(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("[RankedMod-Debug] ").formatted(Formatting.GOLD).append(Text.literal(message).formatted(Formatting.WHITE)), false);
    }
}
