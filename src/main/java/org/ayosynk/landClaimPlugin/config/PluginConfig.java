package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("LandClaimPlugin - Main Configuration")
public class PluginConfig extends OkaeriConfig {

    @Comment("Toggles the economy features of the plugin.")
    public boolean economyEnabled = false;

    @Comment("Messages Prefix (MiniMessage format)")
    public String prefix = "<dark_gray>[<gold>LandClaim<dark_gray>]</gold> ";

    public boolean requireConnectedClaims = false;
    public boolean allowDiagonalConnections = true;

    @Comment("Protections")
    public boolean preventPvp = true;
    public boolean preventMobGriefing = true;
    public boolean preventExplosionDamage = true;
    public boolean preventHarmEntities = true;

    @Comment("Blocked worlds for claiming")
    public List<String> blockWorld = List.of("world_nether", "world_the_end");

    public List<String> blockCmd = List.of("sethome", "home");

    public int cooldownUnstuck = 30;

    @Comment("Map Integration")
    public MapConfig dynmap = new MapConfig();
    public MapConfig bluemap = new MapConfig();

    public static class MapConfig extends OkaeriConfig {
        public boolean enabled = true;
        public String fillColor = "3366FF";
        public double fillOpacity = 0.3;
        public String borderColor = "3366FF";
        public double borderOpacity = 0.8;
    }

    public int maxHomes = 3;
    public boolean autoClaimDefault = false;
    public boolean autoUnclaimDefault = false;
    public int chunkClaimLimit = 5;

    public int worldguardGap = 0;
    public int minClaimGap = 0;

    public int actionbarUpdateInterval = 20;

    @Comment("Default trust permissions")
    public TrustPermissions defaultTrustPermissions = new TrustPermissions();

    @Comment("Default visitor permissions")
    public VisitorPermissions defaultVisitorPermissions = new VisitorPermissions();

    public static class TrustPermissions extends OkaeriConfig {
        public boolean build = true;
        public boolean interact = true;
        public boolean container = true;
        public boolean teleport = true;
    }

    public static class VisitorPermissions extends OkaeriConfig {
        public boolean build = false;
        public boolean interact = false;
        public boolean container = false;
        public boolean teleport = false;
    }
}
