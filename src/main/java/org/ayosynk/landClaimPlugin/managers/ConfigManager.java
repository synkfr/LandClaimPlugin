package org.ayosynk.landClaimPlugin.managers;

import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.MessagesConfig;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigManager {
    private final LandClaimPlugin plugin;

    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;

    // Legacy data configs (Will be moved to DB/Redis later)
    private FileConfiguration claimsConfig;
    private File claimsFile;
    private FileConfiguration trustConfig;
    private File trustFile;
    private FileConfiguration playerDataConfig;
    private File playerDataFile;

    public ConfigManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        this.pluginConfig = eu.okaeri.configs.ConfigManager.create(PluginConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "config.yml"));
            it.saveDefaults();
            it.load(true);
        });

        this.messagesConfig = eu.okaeri.configs.ConfigManager.create(MessagesConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(plugin.getDataFolder(), "messages.yml"));
            it.saveDefaults();
            it.load(true);
        });

        claimsFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!claimsFile.exists())
            createEmptyFile(claimsFile);
        claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);

        trustFile = new File(plugin.getDataFolder(), "trust.yml");
        if (!trustFile.exists())
            createEmptyFile(trustFile);
        trustConfig = YamlConfiguration.loadConfiguration(trustFile);

        playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists())
            createEmptyFile(playerDataFile);
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    private void createEmptyFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create file: " + file.getName());
        }
    }

    public void reloadMainConfig() {
        pluginConfig.load();
        messagesConfig.load();
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getClaimsConfig() {
        return claimsConfig;
    }

    public FileConfiguration getTrustConfig() {
        return trustConfig;
    }

    public FileConfiguration getPlayerDataConfig() {
        return playerDataConfig;
    }

    public void savePlayerData() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException ignored) {
        }
    }

    public void reloadPlayerData() {
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    public void saveClaimsConfig() {
        try {
            claimsConfig.save(claimsFile);
        } catch (IOException ignored) {
        }
    }

    public void saveTrustConfig() {
        try {
            trustConfig.save(trustFile);
        } catch (IOException ignored) {
        }
    }

    // --- Legacy wrapper methods to bridge until other classes are transformed ---

    public boolean requireConnectedClaims() {
        return pluginConfig.requireConnectedClaims;
    }

    public boolean allowDiagonalConnections() {
        return pluginConfig.allowDiagonalConnections;
    }

    public boolean preventPvP() {
        return pluginConfig.preventPvp;
    }

    public boolean preventMobGriefing() {
        return pluginConfig.preventMobGriefing;
    }

    public boolean preventExplosionDamage() {
        return pluginConfig.preventExplosionDamage;
    }

    public boolean preventHarmEntities() {
        return pluginConfig.preventHarmEntities;
    }

    public boolean isWorldBlocked(String worldName) {
        return pluginConfig.blockWorld.contains(worldName);
    }

    public List<String> getBlockedCommands() {
        return pluginConfig.blockCmd;
    }

    public int getUnstuckCooldown() {
        return pluginConfig.cooldownUnstuck;
    }

    public Color getVisualizationColor(String type) {
        return Color.LIME;
    } // Temporary stub for v2 border

    public double getParticleSpacing() {
        return 0.5;
    } // Temporary stub

    public int getVisualizationUpdateInterval() {
        return 20;
    } // Temporary stub

    public boolean getDefaultTrustPermission(String permission) {
        // In v2 we use custom Roles. Bridging for now:
        if (permission.equals("build"))
            return pluginConfig.defaultTrustPermissions.build;
        if (permission.equals("interact"))
            return pluginConfig.defaultTrustPermissions.interact;
        if (permission.equals("container"))
            return pluginConfig.defaultTrustPermissions.container;
        if (permission.equals("teleport"))
            return pluginConfig.defaultTrustPermissions.teleport;
        return true;
    }

    public boolean getDefaultVisitorPermission(String permission) {
        if (permission.equals("build"))
            return pluginConfig.defaultVisitorPermissions.build;
        if (permission.equals("interact"))
            return pluginConfig.defaultVisitorPermissions.interact;
        if (permission.equals("container"))
            return pluginConfig.defaultVisitorPermissions.container;
        if (permission.equals("teleport"))
            return pluginConfig.defaultVisitorPermissions.teleport;
        return false;
    }

    public int getWorldGuardGap() {
        return pluginConfig.worldguardGap;
    }

    public int getMinClaimGap() {
        return pluginConfig.minClaimGap;
    }

    public boolean logAutoSaveMessage() {
        return true;
    } // Temporary

    public String getDefaultVisualizationMode() {
        return "OFF";
    }

    public int getActionBarUpdateInterval() {
        return pluginConfig.actionbarUpdateInterval;
    }

    // --- MiniMessage formatting ---

    public String getMessage(String key, String... replacements) {
        String template = getRawMessageString(key);
        for (int i = 0; i < replacements.length; i += 2) {
            template = template.replace(replacements[i], replacements[i + 1]);
        }
        Component comp = MiniMessage.miniMessage().deserialize(pluginConfig.prefix + template);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getRawMessage(String key, String... replacements) {
        String template = getRawMessageString(key);
        for (int i = 0; i < replacements.length; i += 2) {
            template = template.replace(replacements[i], replacements[i + 1]);
        }
        Component comp = MiniMessage.miniMessage().deserialize(template);
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String getActionBarMessage(String key) {
        Component comp = MiniMessage.miniMessage().deserialize(getRawMessageString(key));
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    private String getRawMessageString(String key) {
        try {
            // Convert kebab-case to camelCase if needed
            String camelCaseKey = key;
            if (key.contains("-")) {
                StringBuilder sb = new StringBuilder();
                boolean nextUpper = false;
                for (char c : key.toCharArray()) {
                    if (c == '-') {
                        nextUpper = true;
                    } else if (nextUpper) {
                        sb.append(Character.toUpperCase(c));
                        nextUpper = false;
                    } else {
                        sb.append(c);
                    }
                }
                camelCaseKey = sb.toString();
            }

            var field = messagesConfig.getClass().getField(camelCaseKey);
            return (String) field.get(messagesConfig);
        } catch (Exception e) {
            return "<red>Message not found: " + key;
        }
    }
}