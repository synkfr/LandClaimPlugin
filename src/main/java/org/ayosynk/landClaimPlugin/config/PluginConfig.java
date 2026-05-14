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

    @Comment("The default language file to load from the locales folder (e.g., en-US, es-ES)")
    public String language = "en-US";

    public boolean requireConnectedClaims = false;
    public boolean allowDiagonalConnections = true;

    @Comment("Blocked worlds for claiming")
    public List<String> blockWorld = List.of("world_nether", "world_the_end");

    public List<String> blockCmd = List.of("setwarp", "warp", "sethome");

    @Comment({
        "Multi-Profile System",
        "Enable to allow players to manage multiple claims via an active profile selector.",
        "WARNING: If you disable this after players have already created multiple profiles, ",
        "they will lose access to all but their primary profile! (Unless they switch back)",
        "Use with caution."
    })
    public boolean multiProfilesEnabled = false;
    @Comment("Maximum number of profiles a single player is allowed to create (own)")
    public int maxProfilesPerPlayer = 2;

    public int cooldownUnstuck = 30;

    @Comment("Map Integration")
    public MapConfig dynmap = new MapConfig();
    public MapConfig bluemap = new MapConfig();
    public MapConfig squaremap = new MapConfig();
    public MapConfig pl3xmap = new MapConfig();

    public static class MapConfig extends OkaeriConfig {
        public boolean enabled = true;
        public String fillColor = "3366FF";
        public double fillOpacity = 0.3;
        public String borderColor = "3366FF";
        public double borderOpacity = 0.8;
    }

    public boolean autoClaimDefault = false;
    public boolean autoUnclaimDefault = false;
    public int chunkClaimLimit = 5;
    
    @Comment("Maximum number of claims a player can be a member of (default: 1)")
    public int maxMemberships = 1;

    @Comment("Database Settings (Supported: MYSQL, SQLITE)")
    public DatabaseConfig database = new DatabaseConfig();

    public static class DatabaseConfig extends OkaeriConfig {
        @Comment("Database type: SQLITE or MYSQL")
        public String type = "SQLITE";
        @Comment("MySQL/MariaDB host (ignored for SQLite)")
        public String host = "localhost";
        @Comment("MySQL/MariaDB port (ignored for SQLite)")
        public int port = 3306;
        @Comment("Database name")
        public String databaseName = "landclaim";
        @Comment("MySQL/MariaDB username (leave empty for SQLite)")
        public String username = "";
        @Comment({"MySQL/MariaDB password (leave empty for SQLite)",
            "Can also be set via LANDCLAIM_DB_PASSWORD environment variable for security"})
        public String password = "";
        @Comment("Table prefix for all plugin tables")
        public String tablePrefix = "lc_";
        @Comment("Maximum number of database connections in the pool")
        public int maximumPoolSize = 10;
        @Comment("Minimum number of idle connections in the pool")
        public int minimumIdle = 2;
        @Comment("Connection timeout in milliseconds")
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

    @Comment("Default maximum warps per player (bypass with landclaim.warps.limit.X)")
    public int maxWarps = 3;
    
    /**
     * Validate configuration values and return a list of error messages.
     * Empty list means no errors.
     */
    public java.util.List<String> validateConfig() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        if (chunkClaimLimit <= 0) {
            errors.add("chunkClaimLimit must be greater than 0, found: " + chunkClaimLimit);
        }
        if (cooldownUnstuck < 0) {
            errors.add("cooldownUnstuck cannot be negative, found: " + cooldownUnstuck);
        }
        if (maxWarps <= 0) {
            errors.add("maxWarps must be greater than 0, found: " + maxWarps);
        }
        if (actionbarUpdateInterval <= 0) {
            errors.add("actionbarUpdateInterval must be greater than 0, found: " + actionbarUpdateInterval);
        }
        if (worldguardGap < 0) {
            errors.add("worldguardGap cannot be negative, found: " + worldguardGap);
        }
        if (minClaimGap < 0) {
            errors.add("minClaimGap cannot be negative, found: " + minClaimGap);
        }
        // Database config validation
        if (!database.type.equalsIgnoreCase("SQLITE") && !database.type.equalsIgnoreCase("MYSQL") && 
                !database.type.equalsIgnoreCase("MARIADB")) {
            errors.add("database.type must be SQLITE, MYSQL, or MARIADB, found: " + database.type);
        }
        if (database.port <= 0 || database.port > 65535) {
            errors.add("database.port must be between 1 and 65535, found: " + database.port);
        }
        if (database.maximumPoolSize <= 0) {
            errors.add("database.maximumPoolSize must be greater than 0, found: " + database.maximumPoolSize);
        }
        if (database.minimumIdle < 0) {
            errors.add("database.minimumIdle cannot be negative, found: " + database.minimumIdle);
        }
        if (database.connectionTimeout <= 0) {
            errors.add("database.connectionTimeout must be greater than 0, found: " + database.connectionTimeout);
        }
        // Redis config validation
        if (redis.enabled) {
            if (redis.port <= 0 || redis.port > 65535) {
                errors.add("redis.port must be between 1 and 65535, found: " + redis.port);
            }
            if (redis.channel.isEmpty()) {
                errors.add("redis.channel cannot be empty");
            }
        }
        return errors;
    }
}
