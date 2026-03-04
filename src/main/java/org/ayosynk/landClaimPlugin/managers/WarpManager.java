package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Map<String, Warp>> playerWarps = new ConcurrentHashMap<>();

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

    public boolean setWarp(UUID ownerId, String name, Location location, Material icon) {
        Map<String, Warp> warps = playerWarps.computeIfAbsent(ownerId, k -> new HashMap<>());
        Warp warp = new Warp(name, location, icon);
        warps.put(name.toLowerCase(), warp);
        plugin.getDatabaseManager().getWarpDao().saveWarp(ownerId, warp);
        return true;
    }

    public boolean deleteWarp(UUID ownerId, String name) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        if (warps == null)
            return false;

        boolean removed = warps.remove(name.toLowerCase()) != null;
        if (removed) {
            plugin.getDatabaseManager().getWarpDao().deleteWarp(ownerId, name.toLowerCase());
        }
        return removed;
    }

    public Warp getWarp(UUID ownerId, String name) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        if (warps == null)
            return null;
        return warps.get(name.toLowerCase());
    }

    public Map<String, Warp> getWarps(UUID ownerId) {
        return playerWarps.getOrDefault(ownerId, Collections.emptyMap());
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

    public int getWarpCount(UUID ownerId) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        return warps != null ? warps.size() : 0;
    }
}
