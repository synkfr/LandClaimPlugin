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

public class HomeManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Map<String, Location>> playerHomes = new ConcurrentHashMap<>();

    private FileConfiguration homesConfig;
    private File homesFile;

    public HomeManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try {
                homesFile.getParentFile().mkdirs();
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create homes.yml");
                e.printStackTrace();
            }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        load();
    }

    public void load() {
        playerHomes.clear();
        ConfigurationSection playersSection = homesConfig.getConfigurationSection("homes");
        if (playersSection == null)
            return;

        for (String uuidStr : playersSection.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid UUID in homes.yml: " + uuidStr);
                continue;
            }

            ConfigurationSection homesSection = playersSection.getConfigurationSection(uuidStr);
            if (homesSection == null)
                continue;

            Map<String, Location> homes = new HashMap<>();
            for (String homeName : homesSection.getKeys(false)) {
                ConfigurationSection homeData = homesSection.getConfigurationSection(homeName);
                if (homeData == null)
                    continue;

                String worldName = homeData.getString("world");
                World world = worldName != null ? Bukkit.getWorld(worldName) : null;
                if (world == null)
                    continue;

                double x = homeData.getDouble("x");
                double y = homeData.getDouble("y");
                double z = homeData.getDouble("z");
                float yaw = (float) homeData.getDouble("yaw", 0);
                float pitch = (float) homeData.getDouble("pitch", 0);

                homes.put(homeName.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
            }

            if (!homes.isEmpty()) {
                playerHomes.put(playerId, homes);
            }
        }
    }

    public void save() {
        homesConfig.set("homes", null);
        ConfigurationSection playersSection = homesConfig.createSection("homes");

        for (Map.Entry<UUID, Map<String, Location>> entry : playerHomes.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());

            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                ConfigurationSection homeData = playerSection.createSection(homeEntry.getKey());
                Location loc = homeEntry.getValue();

                homeData.set("world", loc.getWorld().getName());
                homeData.set("x", loc.getX());
                homeData.set("y", loc.getY());
                homeData.set("z", loc.getZ());
                homeData.set("yaw", loc.getYaw());
                homeData.set("pitch", loc.getPitch());
            }
        }

        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save homes.yml");
            e.printStackTrace();
        }
    }

    public boolean setHome(UUID playerId, String name, Location location) {
        Map<String, Location> homes = playerHomes.computeIfAbsent(playerId, k -> new HashMap<>());
        homes.put(name.toLowerCase(), location);
        return true;
    }

    public boolean deleteHome(UUID playerId, String name) {
        Map<String, Location> homes = playerHomes.get(playerId);
        if (homes == null)
            return false;
        return homes.remove(name.toLowerCase()) != null;
    }

    public Location getHome(UUID playerId, String name) {
        Map<String, Location> homes = playerHomes.get(playerId);
        if (homes == null)
            return null;
        return homes.get(name.toLowerCase());
    }

    public Map<String, Location> getHomes(UUID playerId) {
        return playerHomes.getOrDefault(playerId, Collections.emptyMap());
    }

    public int getHomeLimit(Player player) {
        if (player.hasPermission("landclaim.admin"))
            return Integer.MAX_VALUE;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.homes.limit." + i))
                return i;
        }
        return configManager.getConfig().getInt("max-homes", 3);
    }

    public int getHomeCount(UUID playerId) {
        Map<String, Location> homes = playerHomes.get(playerId);
        return homes != null ? homes.size() : 0;
    }
}
