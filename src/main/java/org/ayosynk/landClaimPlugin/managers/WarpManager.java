package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Map<String, Location>> playerWarps = new ConcurrentHashMap<>();

    public WarpManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void loadFromDatabase() {
        plugin.getDatabaseManager().getWarpDao().loadAllWarps().thenAccept(warps -> {
            playerWarps.clear();
            playerWarps.putAll(warps);
            plugin.getLogger().info("Loaded " + warps.size() + " players' warps from database.");
        });
    }

    public void save() {
        // No longer needed, changes are saved to DB instantly
    }

    public boolean setWarp(UUID playerId, String name, Location location) {
        Map<String, Location> warps = playerWarps.computeIfAbsent(playerId, k -> new HashMap<>());
        warps.put(name.toLowerCase(), location);
        plugin.getDatabaseManager().getWarpDao().saveWarp(playerId, name.toLowerCase(), location);
        return true;
    }

    public boolean deleteWarp(UUID playerId, String name) {
        Map<String, Location> warps = playerWarps.get(playerId);
        if (warps == null)
            return false;

        boolean removed = warps.remove(name.toLowerCase()) != null;
        if (removed) {
            plugin.getDatabaseManager().getWarpDao().deleteWarp(playerId, name.toLowerCase());
        }
        return removed;
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
