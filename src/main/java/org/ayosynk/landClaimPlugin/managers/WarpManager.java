package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Bukkit;
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

    public java.util.concurrent.CompletableFuture<Map<UUID, Map<String, Warp>>> loadFromDatabase() {
        return plugin.getDatabaseManager().getWarpDao().loadAllWarps().thenApply(warps -> {
            playerWarps.clear();
            playerWarps.putAll(warps);
            plugin.getLogger().info("Loaded " + warps.size() + " players' warps from database.");
            return warps;
        });
    }

    /**
     * Flush any pending warp data to the database.
     * <p>
     * Warp changes are persisted immediately via individual DB writes
     * ({@code saveWarp}/{@code deleteWarp}), so this method is a no-op.
     * Retained for lifecycle symmetry with {@code onDisable()}.
     */
    public void save() {
        // Warp changes are already persisted to DB immediately on set/delete.
    }

    public boolean setWarp(UUID ownerId, String name, Location location, Material icon) {
        return setWarp(ownerId, name, location, icon, false);
    }

    public boolean setWarp(UUID ownerId, String name, Location location, Material icon, boolean isPublic) {
        Map<String, Warp> warps = playerWarps.computeIfAbsent(ownerId, k -> new HashMap<>());
        String key = name.toLowerCase();
        Warp warp = new Warp(name, location, icon, isPublic);

        ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
        Player player = Bukkit.getPlayer(ownerId);
        if (profile != null) {
            org.ayosynk.landClaimPlugin.api.event.WarpCreateEvent event =
                    new org.ayosynk.landClaimPlugin.api.event.WarpCreateEvent(profile, warp, player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
        }

        warps.put(key, warp);
        plugin.getDatabaseManager().getWarpDao().saveWarp(ownerId, warp);
        return true;
    }

    public Boolean toggleWarpPublic(UUID ownerId, String name) {
        return toggleWarpPublic(ownerId, name, null);
    }

    public Boolean toggleWarpPublic(UUID ownerId, String name, Player player) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        if (warps == null) return null;
        Warp warp = warps.get(name.toLowerCase());
        if (warp == null) return null;
        boolean newValue = !warp.isPublic();

        ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
        if (profile != null) {
            org.ayosynk.landClaimPlugin.api.event.WarpPrivacyChangeEvent event =
                    new org.ayosynk.landClaimPlugin.api.event.WarpPrivacyChangeEvent(profile, warp, player, newValue);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return null;
            }
        }

        warp.setPublic(newValue);
        plugin.getDatabaseManager().getWarpDao().saveWarp(ownerId, warp);
        return newValue;
    }

    public boolean deleteWarp(UUID ownerId, String name) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        if (warps == null)
            return false;

        Warp removed = warps.remove(name.toLowerCase());
        if (removed != null) {
            plugin.getDatabaseManager().getWarpDao().deleteWarp(ownerId, removed.getName());
            ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
            Player player = Bukkit.getPlayer(ownerId);
            if (profile != null) {
                Bukkit.getPluginManager().callEvent(
                        new org.ayosynk.landClaimPlugin.api.event.WarpDeleteEvent(profile, removed, player));
            }
            return true;
        }
        return false;
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

    /**
     * Return every warp across every owner that has been marked public.
     * Result is a snapshot map (owner UUID -> warp name -> Warp). The
     * snapshot is taken under the per-owner map's monitor; subsequent
     * toggles of a warp's public flag will not retroactively change the
     * returned collection.
     */
    public Map<UUID, Map<String, Warp>> getAllPublicWarps() {
        Map<UUID, Map<String, Warp>> publicWarps = new HashMap<>();
        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : playerWarps.entrySet()) {
            Map<String, Warp> ownerPublic = null;
            for (Map.Entry<String, Warp> warpEntry : ownerEntry.getValue().entrySet()) {
                if (warpEntry.getValue().isPublic()) {
                    if (ownerPublic == null) {
                        ownerPublic = new HashMap<>();
                        publicWarps.put(ownerEntry.getKey(), ownerPublic);
                    }
                    ownerPublic.put(warpEntry.getKey(), warpEntry.getValue());
                }
            }
        }
        return publicWarps;
    }

    /**
     * Find a public warp by its name across all owners. Returns the first
     * match found. Useful for resolving "warp name only" lookups (e.g.
     * the public warps GUI click action or future commands).
     */
    public java.util.Map.Entry<UUID, Warp> findPublicWarp(String name) {
        if (name == null) return null;
        String needle = name.toLowerCase();
        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : playerWarps.entrySet()) {
            Warp warp = ownerEntry.getValue().get(needle);
            if (warp != null && warp.isPublic()) {
                return new java.util.AbstractMap.SimpleEntry<>(ownerEntry.getKey(), warp);
            }
        }
        return null;
    }

    public List<Map.Entry<UUID, Warp>> findAllPublicWarps(String name) {
        if (name == null) return Collections.emptyList();
        String needle = name.toLowerCase();
        List<Map.Entry<UUID, Warp>> matches = new ArrayList<>();
        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : playerWarps.entrySet()) {
            Warp warp = ownerEntry.getValue().get(needle);
            if (warp != null && warp.isPublic()) {
                matches.add(new java.util.AbstractMap.SimpleEntry<>(ownerEntry.getKey(), warp));
            }
        }
        return matches;
    }

    public java.util.Map.Entry<UUID, Warp> findPublicWarpByOwner(String ownerQuery, String name) {
        if (ownerQuery == null || name == null) return null;
        String needle = name.toLowerCase();
        String targetOwner = ownerQuery.toLowerCase();

        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : playerWarps.entrySet()) {
            UUID ownerId = ownerEntry.getKey();
            Warp warp = ownerEntry.getValue().get(needle);
            if (warp == null || !warp.isPublic()) continue;

            if (ownerId.toString().equalsIgnoreCase(targetOwner)) {
                return new java.util.AbstractMap.SimpleEntry<>(ownerId, warp);
            }

            Player online = Bukkit.getPlayer(ownerId);
            if (online != null && online.getName().equalsIgnoreCase(targetOwner)) {
                return new java.util.AbstractMap.SimpleEntry<>(ownerId, warp);
            }

            String offlineName = Bukkit.getOfflinePlayer(ownerId).getName();
            if (offlineName != null && offlineName.equalsIgnoreCase(targetOwner)) {
                return new java.util.AbstractMap.SimpleEntry<>(ownerId, warp);
            }

            ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
            if (profile != null && profile.getName() != null && profile.getName().equalsIgnoreCase(targetOwner)) {
                return new java.util.AbstractMap.SimpleEntry<>(ownerId, warp);
            }
        }
        return null;
    }

    public int getWarpLimit(Player player) {
        if (player.hasPermission("landclaim.admin"))
            return Integer.MAX_VALUE;
        int limit = configManager.getPluginConfig().maxWarps;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.warps.limit." + i)) {
                limit = i;
                break;
            }
        }
        org.ayosynk.landClaimPlugin.models.ClaimProfile profile = plugin.getClaimManager().getActiveProfile(player);
        if (profile != null) {
            limit += profile.getBonusWarpSlots();
        }
        return limit;
    }

    public int getWarpCount(UUID ownerId) {
        Map<String, Warp> warps = playerWarps.get(ownerId);
        return warps != null ? warps.size() : 0;
    }
}
