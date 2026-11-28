package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

import java.util.ArrayList;
import java.util.List;

@Header("LandClaimPlugin - Main Configuration")
@Header("Documentation: https://synkfr.github.io/LandClaimPlugin/CONFIGURATION")
public class PluginConfig extends OkaeriConfig {

    @Comment("General plugin preferences and locale selection.")
    public GeneralConfig general = new GeneralConfig();

    @Comment("Chunk claiming rules, gap checks, blocked worlds, and multi-profile mode.")
    public ClaimingConfig claiming = new ClaimingConfig();

    @Comment("Default chunk, member, profile, and warp allowances.")
    public LimitsConfig limits = new LimitsConfig();

    @Comment("Granular LuckPerms/Bukkit permission gating.")
    public PermissionsConfig permissions = new PermissionsConfig();

    @Comment("Server-wide PvP rules and combat tag integration.")
    public PvpConfig pvp = new PvpConfig();

    @Comment("Default and locked visitor permission behavior.")
    public VisitorSettingsConfigSection visitorSettings = new VisitorSettingsConfigSection();

    @Comment("Unclaimed chunk restrictions for survival or town-style servers.")
    public WildernessProtectionConfig wildernessProtection = new WildernessProtectionConfig();

    @Comment("Automatic cleanup for claims owned by inactive players.")
    public DecayConfig decay = new DecayConfig();

    @Comment("Action bar and territory chat notifications.")
    public DisplayConfig display = new DisplayConfig();

    @Comment("In-game held 128x128 territory radar map.")
    public MinimapConfig minimap = new MinimapConfig();

    @Comment("Web map integrations (BlueMap, Dynmap, Squaremap, Pl3xMap).")
    public MapsConfig maps = new MapsConfig();

    @Comment("Storage backend (SQLITE, MYSQL, or MARIADB).")
    public DatabaseConfig database = new DatabaseConfig();

    @Comment("Cross-server cache invalidation via Redis Pub/Sub.")
    public RedisConfig redis = new RedisConfig();

    @Comment("Asynchronous Modrinth version checker.")
    public UpdateCheckerConfig updateChecker = new UpdateCheckerConfig();

    public static class GeneralConfig extends OkaeriConfig {
        @Comment("Internal schema version used to trigger one-time upgrade backups.")
        public String configVersion = "3.5.0";

        @Comment("Locale file to load from plugins/LandClaimPlugin/locales/messages_<language>.yml")
        public String language = "en-US";

        @Comment("File containing blocked claim name words (one word per line).")
        public String bannedClaimNamesFile = "banned-claim-name.txt";

        @Comment("Cooldown in seconds between /claim unstuck uses.")
        public int cooldownUnstuck = 30;

        @Comment("Send native Bedrock forms when Geyser 2.x / Floodgate are available.")
        public boolean geyserForms = true;
    }

    public static class ClaimingConfig extends OkaeriConfig {
        @Comment("Require new claims to touch an existing claim owned by the profile.")
        public boolean requireConnectedClaims = false;

        @Comment("Allow diagonal adjacency when requireConnectedClaims is true.")
        public boolean allowDiagonalConnections = true;

        @Comment("Minimum chunk gap between different players' claims (0 = disabled).")
        public int minClaimGap = 0;

        @Comment("Minimum chunk gap between claims and WorldGuard regions (0 = disabled).")
        public int worldguardGap = 0;

        @Comment({
            "Allow players to own multiple independent claim profiles via /claim profiles.",
            "Warning: disabling this later hides secondary profiles from their owners."
        })
        public boolean multiProfilesEnabled = false;

        @Comment("Maximum number of separate claim profiles a single player can own.")
        public int maxProfilesPerPlayer = 2;

        @Comment("Worlds where players cannot claim land.")
        public List<String> blockWorld = List.of("world_nether", "world_the_end");

        @Comment("Commands blocked when non-members execute them inside another player's claim.")
        public List<String> blockCmd = List.of("setwarp", "warp", "sethome");
    }

    public static class LimitsConfig extends OkaeriConfig {
        @Comment("Base chunk limit per player (override with landclaim.limit.<amount>).")
        public int chunkClaimLimit = 5;

        @Comment("Hard cap on bonus or purchased claim chunks (0 = unlimited).")
        public int maxBonusClaimBlocks = 0;

        @Comment("Hard cap on total combined claim chunks per player (0 = unlimited).")
        public int maxTotalClaimLimit = 0;

        @Comment("Maximum claims a player may belong to as a member.")
        public int maxMemberships = 1;

        @Comment("Default maximum members allowed inside a single claim profile.")
        public int maxClaimMembers = 5;

        @Comment("Base claim warp limit per profile (override with landclaim.warps.limit.<amount>).")
        public int maxWarps = 3;
    }

    public static class PermissionsConfig extends OkaeriConfig {
        @Comment({
            "Require individual permission nodes (landclaim.menu.<menu>, landclaim.unstuck, etc.).",
            "Leave false on casual SMP servers so all players can access standard commands and menus."
        })
        public boolean useSeparatePermission = false;
    }

    public static class MapsConfig extends OkaeriConfig {
        public MapConfig dynmap = new MapConfig();
        public MapConfig bluemap = new MapConfig();
        public MapConfig squaremap = new MapConfig();
        public MapConfig pl3xmap = new MapConfig();
    }

    public static class MapConfig extends OkaeriConfig {
        public boolean enabled = true;
        @Comment("Hex fill color without '#'.")
        public String fillColor = "3366FF";
        public double fillOpacity = 0.3;
        @Comment("Hex border color without '#'.")
        public String borderColor = "3366FF";
        public double borderOpacity = 0.8;
    }

    public static class WildernessProtectionConfig extends OkaeriConfig {
        @Comment("Deny flags listed under deniedFlags in unclaimed chunks (landclaim.admin bypasses).")
        public boolean enabled = false;

        @Comment("Plain Bukkit world names exempt from wilderness protection.")
        public List<String> exceptionWorlds = List.of();

        @Comment("Permission flags denied in unclaimed chunks when enabled is true.")
        public List<String> deniedFlags = List.of(
                "BLOCK_BREAK",
                "BLOCK_PLACE",
                "BLOCK_IGNITE",
                "USE_BUCKETS",
                "USE_FERTILIZER",
                "MODIFY_SIGNS",
                "USE_DOORS",
                "USE_TRAPDOORS",
                "USE_FENCE_GATES",
                "USE_CONTAINERS",
                "USE_WORKSTATIONS",
                "USE_BEDS",
                "USE_REDSTONE",
                "USE_LECTERNS",
                "USE_BELLS",
                "TRAMPLE_CROPS",
                "MODIFY_ARMOR_STANDS",
                "MODIFY_ITEM_FRAMES");
    }

    public static class DatabaseConfig extends OkaeriConfig {
        @Comment("Supported backends: SQLITE, MYSQL, MARIADB.")
        public String type = "SQLITE";
        public String host = "localhost";
        public int port = 3306;
        public String databaseName = "landclaim";
        public String username = "";
        @Comment("Password or override with LANDCLAIM_DB_PASSWORD environment variable.")
        public String password = "";
        public String tablePrefix = "lc_";
        public int maximumPoolSize = 10;
        public int minimumIdle = 2;
        public long connectionTimeout = 30000;
    }

    public static class RedisConfig extends OkaeriConfig {
        public boolean enabled = false;
        public String host = "localhost";
        public int port = 6379;
        public String password = "";
        public String channel = "landclaim:sync";
    }

    public static class DisplayConfig extends OkaeriConfig {
        @Comment("Show territory status in the player action bar.")
        public boolean actionbarEnabled = true;

        @Comment("Update interval in server ticks (20 ticks = 1 second).")
        public int actionbarUpdateInterval = 20;

        @Comment("Send territory enter/leave messages in chat by default.")
        public boolean claimChatNotifications = false;
    }

    public static class VisitorSettingsConfigSection extends OkaeriConfig {
        @Comment("Lock visitor settings across every claim so owners cannot override defaultFlags.")
        public boolean locked = false;

        @Comment("Default visitor flags assigned to new claims (or enforced globally when locked).")
        public List<String> defaultFlags = List.of("DAMAGE_MONSTERS");
    }

    public static class PvpConfig extends OkaeriConfig {
        @Comment("Force PvP on in every claim and block /claim pvp toggling.")
        public boolean forceEnabled = false;

        @Comment("Disable claim PvP sanctuary while a player has an active combat tag.")
        public boolean combatTagBypass = true;
    }

    public static class DecayConfig extends OkaeriConfig {
        public boolean enabled = false;

        @Comment("Days offline before an unexempted owner's claim decays.")
        public int inactiveDays = 14;

        @Comment("Hours between automated decay scans.")
        public int checkIntervalHours = 12;

        @Comment("Online permission node checked alongside stored decay exemptions.")
        public String exemptPermission = "landclaim.decay.exempt";

        @Comment("Check only owner activity (true) or also active members (false).")
        public boolean onlyCheckOwner = true;
    }

    public static class UpdateCheckerConfig extends OkaeriConfig {
        public boolean enabled = true;
        public String modrinthProjectId = "PAlDTriF";
        public String notifyPermission = "landclaim.admin";
    }

    public static class MinimapConfig extends OkaeriConfig {
        public boolean enabled = true;
        public int mapId = -1;
        public String itemName = "<color:#38BDF8><b>Territory Map</b></color>";
        public List<String> itemLore = List.of(
                "<color:#9AA5B4>Hold in hand or off-hand to view nearby claims.",
                "<color:#FBBF24>Right-click to switch zoom level."
        );
    }

    public List<String> validateConfig() {
        List<String> errors = new ArrayList<>();
        if (limits.chunkClaimLimit <= 0) {
            errors.add("limits.chunkClaimLimit must be greater than 0, found: " + limits.chunkClaimLimit);
        }
        if (general.cooldownUnstuck < 0) {
            errors.add("general.cooldownUnstuck cannot be negative, found: " + general.cooldownUnstuck);
        }
        if (limits.maxWarps <= 0) {
            errors.add("limits.maxWarps must be greater than 0, found: " + limits.maxWarps);
        }
        if (limits.maxMemberships <= 0) {
            errors.add("limits.maxMemberships must be greater than 0, found: " + limits.maxMemberships);
        }
        if (limits.maxClaimMembers <= 0) {
            errors.add("limits.maxClaimMembers must be greater than 0, found: " + limits.maxClaimMembers);
        }
        if (claiming.maxProfilesPerPlayer <= 0) {
            errors.add("claiming.maxProfilesPerPlayer must be greater than 0, found: " + claiming.maxProfilesPerPlayer);
        }
        if (display.actionbarUpdateInterval <= 0) {
            errors.add("display.actionbarUpdateInterval must be greater than 0, found: " + display.actionbarUpdateInterval);
        }
        if (claiming.worldguardGap < 0) {
            errors.add("claiming.worldguardGap cannot be negative, found: " + claiming.worldguardGap);
        }
        if (claiming.minClaimGap < 0) {
            errors.add("claiming.minClaimGap cannot be negative, found: " + claiming.minClaimGap);
        }
        if (decay.inactiveDays <= 0) {
            errors.add("decay.inactiveDays must be greater than 0, found: " + decay.inactiveDays);
        }
        if (decay.checkIntervalHours <= 0) {
            errors.add("decay.checkIntervalHours must be greater than 0, found: " + decay.checkIntervalHours);
        }
        if (!database.type.equalsIgnoreCase("SQLITE")
                && !database.type.equalsIgnoreCase("MYSQL")
                && !database.type.equalsIgnoreCase("MARIADB")) {
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
