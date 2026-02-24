package org.ayosynk.landClaimPlugin;

import org.ayosynk.landClaimPlugin.commands.CommandHandler;
import org.ayosynk.landClaimPlugin.commands.ClaimTabCompleter;
import org.ayosynk.landClaimPlugin.db.DatabaseManager;
import org.ayosynk.landClaimPlugin.managers.CacheManager;
import org.ayosynk.landClaimPlugin.managers.RedisManager;
import org.ayosynk.landClaimPlugin.hooks.BlueMapHook;
import org.ayosynk.landClaimPlugin.hooks.DynmapHook;
import org.ayosynk.landClaimPlugin.listeners.CommandBlocker;
import org.ayosynk.landClaimPlugin.listeners.EventListener;
import org.ayosynk.landClaimPlugin.listeners.PlayerJoinListener;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.HomeManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.managers.SaveManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class LandClaimPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private RedisManager redisManager;
    private ClaimManager claimManager;
    private TrustManager trustManager;
    private VisualizationManager visualizationManager;
    private SaveManager saveManager;
    private HomeManager homeManager;
    private CommandHandler commandHandler;
    private EventListener eventListener;
    private BlueMapHook blueMapHook;
    private DynmapHook dynmapHook;
    private List<String> blockedCommands = new ArrayList<>();
    private List<String> blockedWorlds = new ArrayList<>();
    private boolean worldGuardEnabled = false;

    @Override
    public void onEnable() {
        try {
            new Metrics(this, 28407);

            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                worldGuardEnabled = true;
                getLogger().info("WorldGuard detected. Enabling region gap protection.");
            }

            configManager = new ConfigManager(this);

            // Initialize Database
            databaseManager = new DatabaseManager(this);
            databaseManager.init();

            // Initialize Cache
            cacheManager = new CacheManager();

            // Initialize Redis
            redisManager = new RedisManager(this);
            redisManager.init();

            claimManager = new ClaimManager(this, configManager);
            trustManager = new TrustManager(this, claimManager, configManager);

            claimManager.initialize();
            trustManager.initialize();

            visualizationManager = new VisualizationManager(this, claimManager, configManager);

            homeManager = new HomeManager(this, configManager);

            saveManager = new SaveManager(this, claimManager, trustManager, homeManager);

            commandHandler = new CommandHandler(this, claimManager, trustManager, configManager, visualizationManager,
                    homeManager);

            eventListener = new EventListener(this, claimManager, trustManager, configManager);
            getServer().getPluginManager().registerEvents(eventListener, this);

            getServer().getPluginManager().registerEvents(
                    new CommandBlocker(this, claimManager, trustManager),
                    this);

            getServer().getPluginManager().registerEvents(
                    new PlayerJoinListener(this, visualizationManager),
                    this);

            // Legacy GUI Listener was removed here

            // Register tab completers
            ClaimTabCompleter tabCompleter = new ClaimTabCompleter();
            if (getCommand("claim") != null) {
                getCommand("claim").setTabCompleter(tabCompleter);
            }
            if (getCommand("unclaim") != null) {
                getCommand("unclaim").setTabCompleter(tabCompleter);
            }
            if (getCommand("unclaimall") != null) {
                getCommand("unclaimall").setTabCompleter(tabCompleter);
            }

            if (getCommand("c") != null) {
                getCommand("c").setTabCompleter(tabCompleter);
            }
            if (getCommand("uc") != null) {
                getCommand("uc").setTabCompleter(tabCompleter);
            }

            reloadConfiguration();

            saveManager.startAutoSave();

            Bukkit.getScheduler().runTask(this, () -> {
                if (configManager.getPluginConfig().bluemap.enabled
                        && Bukkit.getPluginManager().getPlugin("BlueMap") != null) {
                    blueMapHook = new BlueMapHook(LandClaimPlugin.this, claimManager);
                    getLogger().info("BlueMap detected. Enabling map integration.");
                }
                if (configManager.getPluginConfig().dynmap.enabled
                        && Bukkit.getPluginManager().getPlugin("dynmap") != null) {
                    dynmapHook = new DynmapHook(LandClaimPlugin.this, claimManager);
                    getLogger().info("Dynmap detected. Enabling map integration.");
                }
            });

            getLogger().info("LandClaim has been enabled! Loaded " +
                    claimManager.getTotalClaims() + " claims.");
        } catch (Exception e) {
            getLogger().severe("Failed to enable LandClaim: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }

    public void reloadConfiguration() {
        configManager.reloadMainConfig();

        blockedCommands = configManager.getBlockedCommands();
        blockedWorlds = configManager.getPluginConfig().blockWorld;

        blockedCommands = blockedCommands.stream().map(String::toLowerCase).toList();
        blockedWorlds = blockedWorlds.stream().map(String::toLowerCase).toList();

        claimManager.loadClaims();
    }

    @Override
    public void onDisable() {
        try {
            if (saveManager != null) {
                saveManager.saveAll();
                getLogger().info("Saved " + claimManager.getTotalClaims() + " claims.");
            }
            if (homeManager != null) {
                homeManager.save();
                getLogger().info("Saved home data");
            }
            if (commandHandler != null) {
                commandHandler.saveAllPlayerData();
                getLogger().info("Saved player data (auto-claim states)");
            }
            if (visualizationManager != null) {
                visualizationManager.saveAllPlayerData();
                getLogger().info("Saved visualization modes");
            }
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            if (redisManager != null) {
                redisManager.shutdown();
            }
            getLogger().info("LandClaim has been disabled!");
        } catch (Exception e) {
            getLogger().severe("Error while disabling LandClaim: " + e.getMessage());
        }
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

    public TrustManager getTrustManager() {
        return trustManager;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public List<String> getBlockedCommands() {
        return blockedCommands;
    }

    public List<String> getBlockedWorlds() {
        return blockedWorlds;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public BlueMapHook getBlueMapHook() {
        return blueMapHook;
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }

    public void refreshMapHooks() {
        if (blueMapHook != null && blueMapHook.isActive()) {
            blueMapHook.update();
        }
        if (dynmapHook != null && dynmapHook.isActive()) {
            dynmapHook.update();
        }
    }
}