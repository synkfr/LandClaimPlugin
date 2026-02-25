package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("LandClaimPlugin - Main Configuration")
public class PluginConfig extends OkaeriConfig {

    @Comment("Messages Prefix (MiniMessage format)")
    public String prefix = "<dark_gray>[<gold>LandClaim<dark_gray>]</gold> ";

    @Comment("The item used as the Claim Wand (Material name)")
    public String claimWandItem = "GOLDEN_SHOVEL";

    public boolean requireConnectedClaims = false;
    public boolean allowDiagonalConnections = true;

    @Comment("Protections")
    public boolean preventPvp = true;
    public boolean preventMobGriefing = true;
    public boolean preventExplosionDamage = true;
    public boolean preventHarmEntities = true;

    @Comment("Blocked worlds for claiming")
    public List<String> blockWorld = List.of("world_nether", "world_the_end");

    public List<String> blockCmd = List.of("setwarp", "warp");

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

    @Comment("Database Settings (Supported: MYSQL, SQLITE)")
    public DatabaseConfig database = new DatabaseConfig();

    public static class DatabaseConfig extends OkaeriConfig {
        public String type = "SQLITE";
        public String host = "localhost";
        public int port = 3306;
        public String databaseName = "landclaim";
        public String username = "root";
        public String password = "password";
        public String tablePrefix = "lc_";
        public int maximumPoolSize = 10;
        public int minimumIdle = 2;
        public long connectionTimeout = 30000;
    }

    @Comment("Redis Cross-Server Sync Settings")
    public RedisConfig redis = new RedisConfig();

    public static class RedisConfig extends OkaeriConfig {
        public boolean enabled = false;
        public String host = "localhost";
        public int port = 6379;
        public String password = "";
        public String channel = "landclaim:sync";
    }

    public int worldguardGap = 0;
    public int minClaimGap = 0;

    public int actionbarUpdateInterval = 20;

    @Comment("Default trust permissions")
    public TrustPermissions defaultTrustPermissions = new TrustPermissions();

    @Comment("Default maximum warps per player (bypass with landclaim.warps.limit.X)")
    public int maxWarps = 3;

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
