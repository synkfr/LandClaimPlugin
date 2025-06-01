package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final LandClaimPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    private FileConfiguration claimsConfig;
    private File claimsFile;

    private FileConfiguration trustConfig;
    private File trustFile;

    public ConfigManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        // Load main config
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Claims data
        claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsFile.exists()) {
            createEmptyFile(claimsFile);
        }
        claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);

        // Trust data
        trustFile = new File(plugin.getDataFolder(), "trust.yml");
        if (!trustFile.exists()) {
            createEmptyFile(trustFile);
        }
        trustConfig = YamlConfiguration.loadConfiguration(trustFile);
    }

    private void createEmptyFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create file: " + file.getName());
            e.printStackTrace();
        }
    }

    public void reloadMainConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getClaimsConfig() {
        return claimsConfig;
    }

    public FileConfiguration getTrustConfig() {
        return trustConfig;
    }

    public boolean requireConnectedClaims() {
        return getConfig().getBoolean("require-connected-claims", false);
    }

    public boolean allowDiagonalConnections() {
        return getConfig().getBoolean("allow-diagonal-connections", true);
    }

    public boolean preventPvP() {
        return getConfig().getBoolean("prevent-pvp", true);
    }

    public boolean preventMobGriefing() {
        return getConfig().getBoolean("prevent-mob-griefing", true);
    }

    public boolean preventExplosionDamage() {
        return getConfig().getBoolean("prevent-explosion-damage", true);
    }

    public boolean isWorldBlocked(String worldName) {
        List<String> blockedWorlds = getConfig().getStringList("block-world");
        return blockedWorlds.contains(worldName);
    }

    public List<String> getBlockedCommands() {
        return getConfig().getStringList("block-cmd");
    }

    public int getUnstuckCooldown() {
        return getConfig().getInt("cooldown-unstuck", 30);
    }

    public void saveClaimsConfig() {
        try {
            claimsConfig.save(claimsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save claims.yml: " + e.getMessage());
        }
    }

    public void saveTrustConfig() {
        try {
            trustConfig.save(trustFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save trust.yml: " + e.getMessage());
        }
    }
}