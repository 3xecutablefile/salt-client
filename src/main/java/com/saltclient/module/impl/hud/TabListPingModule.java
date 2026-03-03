package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.module.ModuleManager;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public final class TabListPingModule extends Module {
    public TabListPingModule() {
        super("tablistping", "TabListPing", "Show your ping in the HUD.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry == null) return;

        int ping = entry.getLatency();
        String pingStr;
        if (ping < 0) return;
        else if (ping < 150) pingStr = ping + "ms";
        else if (ping < 300) pingStr = ping + "ms";
        else if (ping < 600) pingStr = ping + "ms";
        else pingStr = ping + "ms";

        String text = "Ping: " + pingStr;

        int y = HudLayout.nextTopRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(text) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "tablistping", text, x, y, 0xFF8BE0FF, 0xAA0E121A);
    }
}
