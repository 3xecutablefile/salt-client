package com.saltclient.module;

import com.saltclient.module.impl.hud.*;
import com.saltclient.module.impl.combat.HitColorModule;
import com.saltclient.module.impl.camera.FreeLookModule;
import com.saltclient.module.impl.camera.PerspectiveModule;
import com.saltclient.module.impl.crosshair.CrosshairEditorModule;
import com.saltclient.module.impl.crosshair.CustomCrosshairModule;
import com.saltclient.module.impl.crosshair.DynamicCrosshairModule;
import com.saltclient.module.impl.chat.ChatOpacityModule;
import com.saltclient.module.impl.chat.EmoteMenuModule;
import com.saltclient.module.impl.chat.GlobalChatModule;
import com.saltclient.module.impl.movement.AutoSprintModule;
import com.saltclient.module.impl.movement.ElytraSwapModule;
import com.saltclient.module.impl.movement.InventoryWalkModule;
import com.saltclient.module.impl.misc.AutoRespawnModule;
import com.saltclient.module.impl.misc.GuiModule;
import com.saltclient.module.impl.misc.FontSelectorModule;
import com.saltclient.module.impl.misc.AsmrKeyboardModule;
import com.saltclient.module.impl.misc.InventorySorterModule;
import com.saltclient.module.impl.misc.ReplayIndicatorModule;
import com.saltclient.module.impl.misc.ScreenshotHelperModule;
import com.saltclient.module.impl.performance.FontRendererModule;
import com.saltclient.module.impl.performance.FpsBoostModule;
import com.saltclient.module.impl.performance.RamCleanerModule;
import com.saltclient.module.impl.visual.BlockHighlightModule;
import com.saltclient.module.impl.visual.EntityHighlightModule;
import com.saltclient.module.impl.visual.FullBrightModule;
import com.saltclient.module.impl.visual.MotionBlurModule;
import com.saltclient.module.impl.visual.ZoomModule;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byId = new HashMap<>();

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public Optional<Module> byId(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public boolean isEnabled(String id) {
        Module m = byId.get(id);
        return m != null && m.isEnabled();
    }

    public void register(Module module) {
        if (byId.containsKey(module.getId())) {
            throw new IllegalStateException("Duplicate module id: " + module.getId());
        }
        modules.add(module);
        byId.put(module.getId(), module);
    }

    public void onTick(MinecraftClient mc) {
        for (Module m : modules) {
            m.onBindTick(mc);
            if (m.isEnabled()) m.onTick(mc);
        }
    }

    public void onHudRender(DrawContext ctx) {
        HudLayout.beginFrame(MinecraftClient.getInstance());
        HudPos.beginFrame();
        for (Module m : modules) {
            if (m.isEnabled()) m.onHudRender(ctx);
        }
    }

    public void registerDefaults() {
        register(new KeystrokesModule());
        register(new KeyOverlayModule());
        register(new MouseButtonsModule());
        register(new MouseCpsGraphModule());
        register(new ClickHeatmapModule());
        register(new CpsCounterModule());
        register(new LeftClickCpsModule());
        register(new RightClickCpsModule());
        register(new FpsCounterModule());
        register(new PingCounterModule());
        register(new ServerTpsModule());
        register(new ComboCounterModule());
        register(new ReachDisplayModule());
        register(new TargetHudModule());
        register(new ArmorStatusModule());
        register(new PotionHudModule());
        register(new CoordinatesModule());
        register(new DirectionHudModule());
        register(new SessionTimeModule());
        register(new MatchTimerModule());
        register(new PlayerHudModule());
        register(new KillCounterModule());
        register(new DeathCounterModule());
        register(new StreakCounterModule());
        register(new RespawnTimerModule());
        register(new TabListPlayerCountModule());
        register(new PerformanceGraphModule());
        register(new MemoryUsageHudModule());
        register(new CpuTempHudModule());
        register(new GpuTempHudModule());
        register(new TabListPingModule());
        register(new HudScaleModule());
        register(new HudEditorModule());

        register(new ToggleModule("damageindicator", "DamageIndicator", "Show damage feedback markers.", ModuleCategory.HUD));
        register(new ToggleModule("noscoreboard", "NoScoreboard", "Hide scoreboard sidebar.", ModuleCategory.HUD));
        register(new ToggleModule("minimalhud", "MinimalHUD", "Simplify vanilla HUD.", ModuleCategory.HUD));
        register(new ToggleModule("cleanhud", "CleanHUD", "Hide vanilla HUD.", ModuleCategory.HUD));

        register(new ToggleModule("chatfilter", "ChatFilter", "Filter some common chat spam patterns.", ModuleCategory.CHAT));
        register(new ToggleModule("chathighlight", "ChatHighlight", "Highlight messages that mention your name.", ModuleCategory.CHAT));
        register(new ToggleModule("nameprotect", "NameProtect", "Replace your username in chat messages.", ModuleCategory.CHAT));
        register(new ToggleModule("chattimestamp", "ChatTimestamp", "Prefix chat with timestamps.", ModuleCategory.CHAT));
        register(new ToggleModule("chatcleaner", "ChatCleaner", "Deduplicate some chat spam.", ModuleCategory.CHAT));
        register(new ToggleModule("chatautogg", "ChatAutoGG", "Auto-send gg.", ModuleCategory.CHAT));
        register(new ChatOpacityModule());
        register(new EmoteMenuModule());
        register(new GlobalChatModule());

        register(new GuiModule());
        register(new FontSelectorModule());
        register(new AsmrKeyboardModule());
        register(new AutoRespawnModule());
        register(new InventorySorterModule());
        register(new ToggleModule("autoconfigsave", "AutoConfigSave", "Auto-save config on changes.", ModuleCategory.MISC));
        register(new ScreenshotHelperModule());
        register(new ReplayIndicatorModule());

        register(new PerspectiveModule());
        register(new FreeLookModule());
        register(new ZoomModule());
        register(new ToggleModule("zoomscroll", "ZoomScroll", "Adjust zoom with scroll.", ModuleCategory.CAMERA));
        register(new ToggleModule("nobobview", "NoBobView", "Disable view bobbing.", ModuleCategory.CAMERA));
        register(new FullBrightModule());
        register(new MotionBlurModule());

        register(new CrosshairEditorModule());
        register(new CustomCrosshairModule());
        register(new DynamicCrosshairModule());

        register(new HitColorModule());
        register(new ToggleModule("hitsound", "HitSound", "Play a sound on hit.", ModuleCategory.COMBAT));
        register(new ToggleModule("killsound", "KillSound", "Play a sound on kill.", ModuleCategory.COMBAT));

        register(new ToggleModule("lowfire", "LowFire", "Disable fire overlay.", ModuleCategory.VISUAL));
        register(new ToggleModule("clearwater", "ClearWater", "Disable underwater overlay + reduce water fog.", ModuleCategory.VISUAL));
        register(new ToggleModule("nohurtcam", "NoHurtCam", "Disable hurt camera tilt.", ModuleCategory.VISUAL));
        register(new ToggleModule("timechanger", "TimeChanger", "Client-side time override.", ModuleCategory.VISUAL));
        register(new ToggleModule("weatherchanger", "WeatherChanger", "Client-side weather override.", ModuleCategory.VISUAL));
        register(new ToggleModule("fogremover", "FogRemover", "Reduce fog.", ModuleCategory.VISUAL));
        register(new ToggleModule("clouddisabler", "CloudDisabler", "Disable clouds.", ModuleCategory.VISUAL));
        register(new ToggleModule("shadowdisabler", "ShadowDisabler", "Disable entity shadows.", ModuleCategory.VISUAL));
        register(new BlockHighlightModule());
        register(new EntityHighlightModule());

        register(new ToggleModule("animationlimiter", "AnimationLimiter", "Limit some animations.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("particlereducer", "ParticleReducer", "Reduce particles.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("entityculling", "EntityCulling", "Reduce entity render distance.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("blockculling", "BlockCulling", "Reduce biome blend radius.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("dynamicfps", "DynamicFPS", "Reduce FPS when idle.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("backgroundfpslimit", "BackgroundFPSLimit", "Limit FPS when unfocused.", ModuleCategory.PERFORMANCE));
        register(new FpsBoostModule());
        register(new ToggleModule("fastlighting", "FastLighting", "Disable ambient occlusion.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("fastmath", "FastMath", "Reduce some effects calculations.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("lowgraphicsmode", "LowGraphicsMode", "Low graphics preset.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("hudcache", "HUDCache", "Cache HUD strings.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("uiblurtoggle", "UIBlurToggle", "Disable UI blur.", ModuleCategory.PERFORMANCE));
        register(new FontRendererModule());
        register(new ToggleModule("textureoptimizer", "TextureOptimizer", "Texture-related option tweaks.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("mipmapoptimizer", "MipmapOptimizer", "Disable mipmaps.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("unfocusedfpssaver", "UnfocusedFPSSaver", "Limit FPS in menus.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("idlefpslock", "IdleFPSLock", "Lock FPS lower while idle.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("networkoptimizer", "NetworkOptimizer", "Enable native transport when possible.", ModuleCategory.PERFORMANCE));
        register(new RamCleanerModule());
        register(new ToggleModule("gcoptimizer", "GCOptimizer", "Auto-GC under memory pressure.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("threadoptimizer", "ThreadOptimizer", "Chunk builder tweaks.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("soundengineoptimizer", "SoundEngineOptimizer", "Disable some audio features.", ModuleCategory.PERFORMANCE));

        register(new ElytraSwapModule());
        register(new InventoryWalkModule());
        register(new ToggleModule("togglesprint", "ToggleSprint", "Enable vanilla toggle sprint option.", ModuleCategory.MOVEMENT));
        register(new AutoSprintModule());
        register(new ToggleModule("togglesneak", "ToggleSneak", "Enable vanilla toggle sneak option.", ModuleCategory.MOVEMENT));
        register(new ToggleModule("quickdrop", "QuickDrop", "Drop full stacks without CTRL.", ModuleCategory.MOVEMENT));
    }
}
