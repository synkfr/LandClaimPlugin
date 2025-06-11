package org.ayosynk.landClaimPlugin;

import org.ayosynk.landClaimPlugin.commands.ClaimTabCompleter;
import org.ayosynk.landClaimPlugin.listeners.CommandBlocker;
import org.ayosynk.landClaimPlugin.listeners.EventListener;
import org.ayosynk.landClaimPlugin.commands.CommandHandler;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.utils.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class LandClaimPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ClaimManager claimManager;
    private TrustManager trustManager;
    private VisualizationManager visualizationManager;
    private CommandHandler commandHandler;
    private List<String> blockedCommands = new ArrayList<>();
    private List<String> blockedWorlds = new ArrayList<>();
    private int autoSaveTaskId = -1;

    @Override
    public void onEnable() {
        try {
            configManager = new ConfigManager(this);
            claimManager = new ClaimManager(this, configManager);
            trustManager = new TrustManager(this, claimManager, configManager);

            claimManager.initialize();
            trustManager.initialize();

            visualizationManager = new VisualizationManager(this, claimManager, configManager);

            commandHandler = new CommandHandler(this, claimManager, trustManager, configManager, visualizationManager);

            getServer().getPluginManager().registerEvents(
                    new EventListener(this, claimManager, trustManager, configManager),
                    this
            );

            getServer().getPluginManager().registerEvents(
                    new CommandBlocker(this, claimManager, trustManager),
                    this
            );

            if (getCommand("claim") != null) {
                getCommand("claim").setTabCompleter(new ClaimTabCompleter());
            }
            if (getCommand("unclaim") != null) {
                getCommand("unclaim").setTabCompleter(new ClaimTabCompleter());
            }
            if (getCommand("unclaimall") != null) {
                getCommand("unclaimall").setTabCompleter(new ClaimTabCompleter());
            }

            reloadConfiguration();

            scheduleAutoSave();

            getLogger().info("LandClaim has been enabled! Loaded " +
                    claimManager.getTotalClaims() + " claims and " +
                    trustManager.getTotalTrusts() + " trust relationships");
        } catch (Exception e) {
            getLogger().severe("Failed to enable LandClaim: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void scheduleAutoSave() {
        // Auto-save every 5 minutes (12000 ticks)
        autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getLogger().info("Auto-saving claims and trust data...");
            claimManager.saveClaims();
            trustManager.saveTrustedPlayers();
        }, 12000L, 12000L);
    }

    public void reloadConfiguration() {
        ConfigUpdater.updateConfig(this);

        configManager.reloadMainConfig();

        blockedCommands = configManager.getBlockedCommands();
        blockedWorlds = configManager.getConfig().getStringList("block-world");

        blockedCommands = blockedCommands.stream().map(String::toLowerCase).toList();
        blockedWorlds = blockedWorlds.stream().map(String::toLowerCase).toList();

        claimManager.loadClaims();
        trustManager.loadTrustedPlayers();
    }

    @Override
    public void onDisable() {
        try {
            if (autoSaveTaskId != -1) {
                Bukkit.getScheduler().cancelTask(autoSaveTaskId);
            }

            if (claimManager != null) {
                claimManager.saveClaims();
                getLogger().info("Saved " + claimManager.getTotalClaims() + " claims");
            }
            if (trustManager != null) {
                trustManager.saveTrustedPlayers();
                getLogger().info("Saved trust data for " + trustManager.getTotalTrusts() + " relationships");
            }
            getLogger().info("LandClaim has been disabled!");
        } catch (Exception e) {
            getLogger().severe("Error while disabling LandClaim: " + e.getMessage());
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
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

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public List<String> getBlockedCommands() {
        return blockedCommands;
    }

    public List<String> getBlockedWorlds() {
        return blockedWorlds;
    }
}