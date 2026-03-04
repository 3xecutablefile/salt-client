# Salt Client (Fabric 1.21.4)

Salt Client is a client-side utility modpack/client for Fabric 1.21.4, built to run on desktop and stay usable on Pojav Launcher (Android).

## Requirements

- Minecraft `1.21.4`
- Fabric Loader `>=0.16.9`
- Fabric API for `1.21.4`
- Java `21`

## Install

1. Download the latest jar from GitHub Releases or Modrinth.
2. Put it in your `mods/` folder.
3. Launch Minecraft with your Fabric profile.

## Controls and command defaults

- Open Salt menu: `Right Shift`
- Zoom (when enabled): hold `C`
- Perspective (when enabled): hold `V`
- FreeLook (when enabled): hold `B`
- Ranked/ELO screen: `J`
- Ranked status command: `/ranked status`

In the Salt menu: left click toggles a module, right click opens module settings.

## What is built in

- 104 built-in modules across HUD, chat, camera, crosshair, visual, performance, movement, combat, and misc categories.
- HUD editor (`HUDEditor`) for drag/drop HUD placement, single-element reset, and reset-all.
- Crosshair customization (`CrosshairEditor`, `CustomCrosshair`, `DynamicCrosshair`).
- Pojav-friendly performance tweaks (mostly option-based, with lightweight mixins where needed).
- In-game Modrinth browser/installer support for mods, texture packs, and worlds.
- Ranked integration features (kit detection, ELO/stat tracking, leaderboard/cosmetics UI, and quick status command).

## Module list by category

- HUD (37): `ArmorStatus`, `CleanHUD`, `ClickHeatmap`, `ComboCounter`, `Coordinates`, `CPSCounter`, `CPUTempHUD`, `DamageIndicator`, `DeathCounter`, `DirectionHUD`, `FPSCounter`, `GPUTempHUD`, `HUDEditor`, `HUDScale`, `KeyOverlay`, `Keystrokes`, `KillCounter`, `LeftClickCPS`, `MatchTimer`, `MemoryUsageHUD`, `MinimalHUD`, `MouseButtons`, `MouseCPSGraph`, `NoScoreboard`, `PerformanceGraph`, `PingCounter`, `PlayerHUD`, `PotionHUD`, `ReachDisplay`, `RespawnTimer`, `RightClickCPS`, `ServerTPS`, `SessionTime`, `StreakCounter`, `TabListPing`, `TabListPlayerCount`, `TargetHUD`
- CHAT (9): `ChatAutoGG`, `ChatCleaner`, `ChatFilter`, `ChatHighlight`, `ChatOpacity`, `ChatTimestamp`, `EmoteMenu`, `GlobalChat`, `NameProtect`
- CAMERA (5): `FreeLook`, `NoBobView`, `Perspective`, `Zoom`, `ZoomScroll`
- CROSSHAIR (3): `CrosshairEditor`, `CustomCrosshair`, `DynamicCrosshair`
- VISUAL (12): `BlockHighlight`, `ClearWater`, `CloudDisabler`, `EntityHighlight`, `FogRemover`, `FullBright`, `LowFire`, `MotionBlur`, `NoHurtCam`, `ShadowDisabler`, `TimeChanger`, `WeatherChanger`
- PERFORMANCE (22): `AnimationLimiter`, `BackgroundFPSLimit`, `BlockCulling`, `DynamicFPS`, `EntityCulling`, `FastLighting`, `FastMath`, `FontRenderer`, `FPS Boost`, `GCOptimizer`, `HUDCache`, `IdleFPSLock`, `LowGraphicsMode`, `MipmapOptimizer`, `NetworkOptimizer`, `ParticleReducer`, `RAMCleaner`, `SoundEngineOptimizer`, `TextureOptimizer`, `ThreadOptimizer`, `UIBlurToggle`, `UnfocusedFPSSaver`
- MOVEMENT (5): `AutoSprint`, `ElytraSwap`, `QuickDrop`, `ToggleSneak`, `ToggleSprint`
- COMBAT (3): `HitColor`, `HitSound`, `KillSound`
- MISC (8): `ASMRKeyboard`, `AutoConfigSave`, `AutoRespawn`, `FontSelector`, `GUI`, `InventorySorter`, `ReplayIndicator`, `ScreenshotHelper`

## Build from source

```sh
./gradlew --no-daemon build
```

On Android `/sdcard/` (no executable bit):

```sh
sh gradlew --no-daemon build
```

Build output jar:

- `build/libs/salt-client-<version>.jar`

## Release automation

GitHub Actions is configured to:

- create a tag `v<mod_version>` when changes land on `main`
- build and create a GitHub release for `v*` tags
- publish the built jar to Modrinth
