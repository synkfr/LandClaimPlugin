package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header({
    "===========================================================",
    "              LandClaimPlugin - Main Configuration          ",
    "==========================================================="
})
public class PluginConfig extends OkaeriConfig {

    @Comment({
        "Messages Prefix (MiniMessage format)",
        "This prefix will appear before all chat messages sent by the plugin."
    })
    public String prefix = "<dark_gray>[<gold>LandClaim<dark_gray>]</gold> ";

    @Comment({
        "The default language/locale file to load from the locales folder (e.g., en-US, es-ES).",
        "Make sure the corresponding file exists in the plugins/LandClaimPlugin/locales/ directory."
    })
    public String language = "en-US";

    @Comment({
        "Adjacent Claim Connection Check",
        "If true, players can only claim new chunks that are directly adjacent to their existing claims.",
        "If false, players can claim chunks anywhere in the world."
    })
    public boolean requireConnectedClaims = false;

    @Comment({
        "Diagonal Chunk Connection Check",
        "If requireConnectedClaims is true, this determines whether diagonal chunks count as connected (true)",
        "or only orthogonal (north/south/east/west) chunks count as connected (false)."
    })
    public boolean allowDiagonalConnections = true;

    @Comment({
        "Blocked Worlds for Claiming",
        "Players will be unable to claim any land inside the worlds listed below."
    })
    public List<String> blockWorld = List.of("world_nether", "world_the_end");

    @Comment({
        "Blocked Commands in Claims",
        "List of commands that players are strictly blocked from executing while standing inside other players' claims.",
        "Use this to prevent unauthorized warping, setting homes, or teleportation in claimed territories."
    })
    public List<String> blockCmd = List.of("setwarp", "warp", "sethome");

    @Comment({
        "Multi-Profile System",
        "Enable to allow players to manage multiple separate claim profiles via an active profile selector GUI.",
        "WARNING: If you disable this after players have already created multiple profiles, ",
        "they will lose access to all secondary profiles! Switch back to enable them again.",
        "Use with caution."
    })
    public boolean multiProfilesEnabled = false;

    @Comment("Maximum number of separate claim profiles a single player is allowed to own/create.")
    public int maxProfilesPerPlayer = 2;

    @Comment("Cooldown in seconds for the /claim unstuck command to prevent spamming.")
    public int cooldownUnstuck = 30;

    @Comment("Integration settings for web-based live maps (Dynmap, BlueMap, Squaremap, Pl3xmap).")
    public MapConfig dynmap = new MapConfig();
    public MapConfig bluemap = new MapConfig();
    public MapConfig squaremap = new MapConfig();
    public MapConfig pl3xmap = new MapConfig();

    public static class MapConfig extends OkaeriConfig {
        @Comment("Enable/disable this specific map integration.")
        public boolean enabled = true;
        @Comment("Fill color for the claim areas on the map (Hex color code without #).")
        public String fillColor = "3366FF";
        @Comment("Fill opacity for the claim areas on the map (0.0 = fully transparent, 1.0 = fully solid).")
        public double fillOpacity = 0.3;
        @Comment("Border color for the claim areas on the map (Hex color code without #).")
        public String borderColor = "3366FF";
        @Comment("Border opacity for the claim areas on the map (0.0 = fully transparent, 1.0 = fully solid).")
        public double borderOpacity = 0.8;
    }

    @Comment("Default maximum number of chunks a player is allowed to claim (can be bypassed with landclaim.limit.X permission).")
    public int chunkClaimLimit = 5;
    
    @Comment("Maximum number of claims a player is allowed to join as a trusted member (default: 1).")
    public int maxMemberships = 1;

    @Comment("Database connection and backend settings (Supported backends: SQLITE, MYSQL, MARIADB).")
    public DatabaseConfig database = new DatabaseConfig();

    public static class DatabaseConfig extends OkaeriConfig {
        @Comment("Database type: SQLITE, MYSQL, or MARIADB")
        public String type = "SQLITE";
        @Comment("MySQL/MariaDB host address (ignored for SQLite)")
        public String host = "localhost";
        @Comment("MySQL/MariaDB port (ignored for SQLite)")
        public int port = 3306;
        @Comment("Database name")
        public String databaseName = "landclaim";
        @Comment("MySQL/MariaDB username (leave empty for SQLite)")
        public String username = "";
        @Comment({
            "MySQL/MariaDB password (leave empty for SQLite)",
            "For improved security, this can also be set via the LANDCLAIM_DB_PASSWORD environment variable."
        })
        public String password = "";
        @Comment("Table prefix for all plugin tables in the database")
        public String tablePrefix = "lc_";
        @Comment("Maximum number of active database connections in the HikariCP pool")
        public int maximumPoolSize = 10;
        @Comment("Minimum number of idle connections to maintain in the pool")
        public int minimumIdle = 2;
        @Comment("Maximum connection timeout in milliseconds before failing")
        public long connectionTimeout = 30000;
    }

    @Comment("Redis settings for cross-server synchronization (useful for BungeeCord/Velocity proxy networks).")
    public RedisConfig redis = new RedisConfig();

    public static class RedisConfig extends OkaeriConfig {
        @Comment("Enable/disable Redis synchronization.")
        public boolean enabled = false;
        @Comment("Redis server host address.")
        public String host = "localhost";
        @Comment("Redis server port.")
        public int port = 6379;
        @Comment("Redis server password (leave empty if no authentication is required).")
        public String password = "";
        @Comment("Redis Pub/Sub channel name used for synchronization messages.")
        public String channel = "landclaim:sync";
    }

    @Comment({
        "WorldGuard Adjacency Protection Gap",
        "Minimum distance in chunks that must be maintained between a player claim and any WorldGuard region.",
        "Set to 0 to disable this gap restriction."
    })
    public int worldguardGap = 0;

    @Comment({
        "Claim Adjacency Protection Gap",
        "Minimum distance in chunks that must be maintained between claims owned by different players.",
        "Set to 0 to allow players to claim chunks adjacent to other players' claims."
    })
    public int minClaimGap = 0;

    @Comment("Interval in server ticks (20 ticks = 1 second) for updating the player action bar display (boundary titles, claim status, etc.).")
    public int actionbarUpdateInterval = 20;

    @Comment("Default maximum claim warps a player can set (can be bypassed with landclaim.warps.limit.X permission).")
    public int maxWarps = 3;

    @Comment({
        "LuckPerms / Bukkit Permissions Bypass",
        "If true, separate permissions like landclaim.menu.<menu>, landclaim.menu.*, ",
        "and command-specific permissions (e.g., landclaim.unstuck) will be checked.",
        "If false, these separate permissions are bypassed. This is ideal for friendly/vanilla SMP ",
        "servers where players don't want to set up an elaborate permission group system."
    })
    public boolean useSeparatePremission = false;

    @Comment({
        "Banned Claim Names File",
        "The text file containing words (one per line) that cannot be used in claim profiles or renamed claims.",
        "Useful for blocking slurs, swearing, or other inappropriate/reserved names."
    })
    public String bannedClaimNamesFile = "banned-claim-name.txt";
    
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
