package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.ayosynk.landClaimPlugin.utils.ConfigUpdater;
import org.bukkit.Color;
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

    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        updateConfig();
        loadConfigs();
    }

    private void updateConfig() {
        ConfigUpdater.updateConfig(plugin);
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

        // Messages
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
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
        // Reload messages.yml as well
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
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

    public boolean preventHarmEntities() {
        return getConfig().getBoolean("prevent-harm-entities", true);
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

    public Color getVisualizationColor(String type) {
        String colorStr = getConfig().getString("visualization." + type, "0,255,0");
        String[] rgb = colorStr.split(",");
        try {
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return type.equals("always-color") ? Color.LIME : Color.YELLOW;
        }
    }

    public double getParticleSpacing() {
        return getConfig().getDouble("visualization.particle-spacing", 0.5);
    }

    public int getVisualizationUpdateInterval() {
        return getConfig().getInt("visualization.update-interval", 20);
    }

    public boolean getDefaultTrustPermission(String permission) {
        return getConfig().getBoolean("default-trust-permissions." + permission, true);
    }

    public boolean getDefaultVisitorPermission(String permission) {
        return getConfig().getBoolean("default-visitor-permissions." + permission, false);
    }

    public int getWorldGuardGap() {
        return getConfig().getInt("worldguard-gap", 0);
    }

    public int getMinClaimGap() {
        return getConfig().getInt("min-claim-gap", 0);
    }

    public boolean logAutoSaveMessage() {
        return getConfig().getBoolean("log-auto-save-message", true);
    }

    public String getDefaultVisualizationMode() {
        return getConfig().getString("visualization-default", "OFF");
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

    public String getMessage(String key, String... replacements) {
        String prefix = getConfig().getString("prefix", "&8[&6LandClaim&8]&r ");
        String message = messagesConfig.getString(key, "&cMessage not found: " + key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i+1]);
        }
        return ChatUtils.colorize(prefix + message);
    }
}