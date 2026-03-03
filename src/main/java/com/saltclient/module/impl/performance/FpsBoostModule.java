package com.saltclient.module.impl.performance;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.EnumSetting;

public final class FpsBoostModule extends Module {

    public enum Level {
        LITE("Lite"),
        BALANCED("Balanced"),
        MAX("Max");

        public final String label;

        Level(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumSetting<Level> level = addSetting(new EnumSetting<>(
        "level", "Level",
        "How aggressive the FPS boost is.",
        Level.BALANCED, Level.values()
    ));

    public FpsBoostModule() {
        super("fpsboost", "FPS Boost",
            "Preset performance tweaks. Lite = cosmetic only. Balanced = default. Max = aggressive.",
            ModuleCategory.PERFORMANCE, true);
    }

    public Level getLevel() {
        return level.getValue();
    }
}
