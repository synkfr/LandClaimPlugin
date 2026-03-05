package org.ayosynk.landClaimPlugin;

import org.ayosynk.landClaimPlugin.commands.CommandHandler;
import org.ayosynk.landClaimPlugin.db.DatabaseManager;
import org.ayosynk.landClaimPlugin.managers.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class responsible only for lifecycle, setup, and dependency
 * injection.
 * All logic and feature management is delegated to specific managers.
 */
public class LandClaimPlugin extends JavaPlugin {

    private static LandClaimPlugin instance;

    // Managers (Final architectural access points)
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private RedisManager redisManager;
    private ClaimManager claimManager;
    private CombatManager combatManager;
    private VisualizationManager visualizationManager;
    private WarpManager warpManager;
    private CommandHandler commandHandler;
    private ListenerManager listenerManager;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // 1. Enable bStats metrics
            new Metrics(this, 28407);

            // Run V1 legacy file cleanup BEFORE Okaeri ConfigManager writes defaults
            org.ayosynk.landClaimPlugin.migration.V1LegacyMigrator.preConfigCleanup(this);

            // 2. Initialize core configs
            configManager = new ConfigManager(this);

            // 3. Initialize Database and Cache
            databaseManager = new DatabaseManager(this);
            databaseManager.init();
            cacheManager = new CacheManager();

            // 4. Initialize Network/Bungee syncing (Redis)
            redisManager = new RedisManager(this);
            redisManager.init();

            // 5. Run V1 → V2 SQL legacy migration from the backed-up claims file
            org.ayosynk.landClaimPlugin.migration.V1LegacyMigrator.migrateClaims(this);

            // 6. Initialize business logic managers
            combatManager = new CombatManager(this);
            claimManager = new ClaimManager(this, configManager);
            visualizationManager = new VisualizationManager(this, claimManager, configManager);
            warpManager = new WarpManager(this, configManager);

            // Load persistent warp data before resolving claims
            warpManager.loadFromDatabase().thenRun(() -> {
                claimManager.initialize();
                getLogger().info("Warp and Claim systems initialized.");
            });

            // 7. Initialize and register commands
            commandHandler = new CommandHandler(this, claimManager, configManager, visualizationManager, warpManager);

            // 8. Initialize and register listeners
            listenerManager = new ListenerManager(this, claimManager, configManager, visualizationManager);
            listenerManager.registerAll();

            // 9. Initialize third-party plugins (WorldGuard, Maps)
            hookManager = new HookManager(this, claimManager, configManager);
            hookManager.init();

            // Refresh settings once more
            configManager.reloadMainConfig();

            getLogger().info(
                    "LandClaim has been successfully enabled! Loaded " + claimManager.getTotalClaims() + " claims.");
        } catch (Exception e) {
            getLogger().severe("Failed to enable LandClaim: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Shutdown commands executor gracefully
            if (commandHandler != null) {
                commandHandler.shutdown();
            }

            // Save persistent data synchronously before exit
            if (warpManager != null) {
                warpManager.save();
                getLogger().info("Saved warp data.");
            }

            // Clear visual effects
            if (visualizationManager != null) {
                visualizationManager.cleanupLocalDisplays();
                getLogger().info("Cleared active visualization displays.");
            }

            // Close external connections
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            if (redisManager != null) {
                redisManager.shutdown();
            }

            getLogger().info("LandClaim has been successfully disabled!");
        } catch (Exception e) {
            getLogger().severe("Error while disabling LandClaim: " + e.getMessage());
        }
    }

    // --- Singleton & Manager Accessors ---

    public static LandClaimPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }
}