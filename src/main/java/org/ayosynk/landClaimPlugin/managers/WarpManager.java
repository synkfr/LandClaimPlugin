package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Map<String, Location>> playerWarps = new ConcurrentHashMap<>();

    private FileConfiguration warpsConfig;
    private File warpsFile;

    public WarpManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            try {
                warpsFile.getParentFile().mkdirs();
                warpsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create warps.yml");
                e.printStackTrace();
            }
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        load();
    }

    public void load() {
        playerWarps.clear();
        ConfigurationSection playersSection = warpsConfig.getConfigurationSection("warps");
        if (playersSection == null)
            return;

        for (String uuidStr : playersSection.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid UUID in warps.yml: " + uuidStr);
                continue;
            }

            ConfigurationSection warpsSection = playersSection.getConfigurationSection(uuidStr);
            if (warpsSection == null)
                continue;

            Map<String, Location> warps = new HashMap<>();
            for (String warpName : warpsSection.getKeys(false)) {
                ConfigurationSection warpData = warpsSection.getConfigurationSection(warpName);
                if (warpData == null)
                    continue;

                String worldName = warpData.getString("world");
                World world = worldName != null ? Bukkit.getWorld(worldName) : null;
                if (world == null)
                    continue;

                double x = warpData.getDouble("x");
                double y = warpData.getDouble("y");
                double z = warpData.getDouble("z");
                float yaw = (float) warpData.getDouble("yaw", 0);
                float pitch = (float) warpData.getDouble("pitch", 0);

                warps.put(warpName.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
            }

            if (!warps.isEmpty()) {
                playerWarps.put(playerId, warps);
            }
        }
    }

    public void save() {
        warpsConfig.set("warps", null);
        ConfigurationSection playersSection = warpsConfig.createSection("warps");

        for (Map.Entry<UUID, Map<String, Location>> entry : playerWarps.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());

            for (Map.Entry<String, Location> warpEntry : entry.getValue().entrySet()) {
                ConfigurationSection warpData = playerSection.createSection(warpEntry.getKey());
                Location loc = warpEntry.getValue();

                warpData.set("world", loc.getWorld().getName());
                warpData.set("x", loc.getX());
                warpData.set("y", loc.getY());
                warpData.set("z", loc.getZ());
                warpData.set("yaw", loc.getYaw());
                warpData.set("pitch", loc.getPitch());
            }
        }

        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warps.yml");
            e.printStackTrace();
        }
    }

    public boolean setWarp(UUID playerId, String name, Location location) {
        Map<String, Location> warps = playerWarps.computeIfAbsent(playerId, k -> new HashMap<>());
        warps.put(name.toLowerCase(), location);
        return true;
    }

    public boolean deleteWarp(UUID playerId, String name) {
        Map<String, Location> warps = playerWarps.get(playerId);
        if (warps == null)
            return false;
        return warps.remove(name.toLowerCase()) != null;
    }

    public Location getWarp(UUID playerId, String name) {
        Map<String, Location> warps = playerWarps.get(playerId);
        if (warps == null)
            return null;
        return warps.get(name.toLowerCase());
    }

    public Map<String, Location> getWarps(UUID playerId) {
        return playerWarps.getOrDefault(playerId, Collections.emptyMap());
    }

    public int getWarpLimit(Player player) {
        if (player.hasPermission("landclaim.admin"))
            return Integer.MAX_VALUE;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.warps.limit." + i))
                return i;
        }
        return configManager.getPluginConfig().maxWarps;
    }

    public int getWarpCount(UUID playerId) {
        Map<String, Location> warps = playerWarps.get(playerId);
        return warps != null ? warps.size() : 0;
    }
}
