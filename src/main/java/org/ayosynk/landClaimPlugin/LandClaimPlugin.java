package org.ayosynk.landClaimPlugin;

import org.ayosynk.landClaimPlugin.api.LandClaimAPI;
import org.ayosynk.landClaimPlugin.api.LandClaimAPIImpl;
import org.ayosynk.landClaimPlugin.commands.CommandHandler;
import org.ayosynk.landClaimPlugin.db.DatabaseManager;
import org.ayosynk.landClaimPlugin.managers.*;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class responsible only for lifecycle, setup, and dependency
 * injection.
 * All logic and feature management is delegated to specific managers.
 *
 * Implements LandClaimAPI for external plugin integration.
 */
public class LandClaimPlugin extends JavaPlugin implements LandClaimAPI {

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

    // API delegate for interface methods
    private LandClaimAPIImpl apiDelegate;

    @Override
    public void onLoad() {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            loadWorldGuardHook();
        }
    }

    private void loadWorldGuardHook() {
        org.ayosynk.landClaimPlugin.hooks.wg.WorldGuardHook.onLoad();
    }

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

            // 10. Initialize public API for external plugins
            apiDelegate = new LandClaimAPIImpl(this);

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

    public void reloadPlugin() {
        configManager.reloadMainConfig();
        if (hookManager != null) {
            hookManager.refreshMapHooks();
        }
        if (visualizationManager != null) {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                visualizationManager.invalidateCache(p.getUniqueId());
            }
        }
    }

    // --- LandClaimAPI Implementation (delegated to apiDelegate) ---

    @Override
    public boolean isChunkClaimed(String world, int chunkX, int chunkZ) {
        return apiDelegate.isChunkClaimed(world, chunkX, chunkZ);
    }

    @Override
    public boolean isChunkClaimed(org.ayosynk.landClaimPlugin.models.ChunkPosition pos) {
        return apiDelegate.isChunkClaimed(pos);
    }

    @Override
    public boolean isLocationClaimed(org.bukkit.Location location) {
        return apiDelegate.isLocationClaimed(location);
    }

    @Override
    public ClaimProfile getClaimAt(String world, int chunkX, int chunkZ) {
        return apiDelegate.getClaimAt(world, chunkX, chunkZ);
    }

    @Override
    public ClaimProfile getClaimAt(org.ayosynk.landClaimPlugin.models.ChunkPosition pos) {
        return apiDelegate.getClaimAt(pos);
    }

    @Override
    public ClaimProfile getClaimAt(org.bukkit.Location location) {
        return apiDelegate.getClaimAt(location);
    }

    @Override
    public java.util.List<ClaimProfile> getClaimsByOwner(java.util.UUID playerId) {
        return apiDelegate.getClaimsByOwner(playerId);
    }

    @Override
    public java.util.List<ClaimProfile> getClaimsByMember(java.util.UUID playerId) {
        return apiDelegate.getClaimsByMember(playerId);
    }

    @Override
    public ClaimProfile getClaimByName(String name) {
        return apiDelegate.getClaimByName(name);
    }

    @Override
    public java.util.Collection<ClaimProfile> getAllClaimProfiles() {
        return apiDelegate.getAllClaimProfiles();
    }

    @Override
    public ClaimProfile getClaimById(java.util.UUID profileId) {
        return apiDelegate.getClaimById(profileId);
    }

    @Override
    public int getTotalChunksByOwner(java.util.UUID playerId) {
        return apiDelegate.getTotalChunksByOwner(playerId);
    }

    @Override
    public boolean hasPermission(ClaimProfile profile, java.util.UUID playerId, String permission) {
        return apiDelegate.hasPermission(profile, playerId, permission);
    }

    @Override
    public String getPlayerStatus(ClaimProfile profile, java.util.UUID playerId) {
        return apiDelegate.getPlayerStatus(profile, playerId);
    }

    @Override
    public boolean isOwner(ClaimProfile profile, java.util.UUID playerId) {
        return apiDelegate.isOwner(profile, playerId);
    }

    @Override
    public boolean isMember(ClaimProfile profile, java.util.UUID playerId) {
        return apiDelegate.isMember(profile, playerId);
    }

    @Override
    public boolean isTrusted(ClaimProfile profile, java.util.UUID playerId) {
        return apiDelegate.isTrusted(profile, playerId);
    }

    @Override
    public java.util.Map<String, Warp> getWarps(java.util.UUID profileId) {
        return apiDelegate.getWarps(profileId);
    }

    @Override
    public Warp getWarp(java.util.UUID profileId, String warpName) {
        return apiDelegate.getWarp(profileId, warpName);
    }

    @Override
    public boolean isInCombat(org.bukkit.entity.Player player) {
        return apiDelegate.isInCombat(player);
    }

    @Override
    public int getClaimLimit(org.bukkit.entity.Player player) {
        return apiDelegate.getClaimLimit(player);
    }

    @Override
    public int getClaimLimit(java.util.UUID playerId) {
        return apiDelegate.getClaimLimit(playerId);
    }

    @Override
    public boolean canCreateClaim(java.util.UUID playerId) {
        return apiDelegate.canCreateClaim(playerId);
    }

    @Override
    public boolean adminClaimChunk(org.bukkit.entity.Player player, org.bukkit.Location location) {
        return apiDelegate.adminClaimChunk(player, location);
    }

    @Override
    public boolean adminUnclaimChunk(org.bukkit.entity.Player player, org.bukkit.Location location) {
        return apiDelegate.adminUnclaimChunk(player, location);
    }

    @Override
    public boolean transferClaim(org.bukkit.entity.Player actor, java.util.UUID profileId, java.util.UUID newOwnerId) {
        return apiDelegate.transferClaim(actor, profileId, newOwnerId);
    }

    @Override
    public int unclaimAll(org.bukkit.entity.Player actor, java.util.UUID profileId) {
        return apiDelegate.unclaimAll(actor, profileId);
    }

    @Override
    public java.util.concurrent.CompletableFuture<Integer> addBonusBlocks(java.util.UUID playerId, int amount) {
        return apiDelegate.addBonusBlocks(playerId, amount);
    }

    @Override
    public java.util.concurrent.CompletableFuture<Integer> getBonusBlocks(java.util.UUID playerId) {
        return apiDelegate.getBonusBlocks(playerId);
    }

    @Override
    public long getServerTime() {
        return apiDelegate.getServerTime();
    }
}