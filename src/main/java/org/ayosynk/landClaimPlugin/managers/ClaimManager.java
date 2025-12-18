package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<ChunkPosition, UUID> claimedChunks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<ChunkPosition>> playerClaims = new ConcurrentHashMap<>();

    public ClaimManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        loadClaims();
    }

    public int getTotalClaims() {
        return claimedChunks.size();
    }

    public void loadClaims() {
        claimedChunks.clear();
        playerClaims.clear();

        FileConfiguration config = configManager.getClaimsConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");
        if (claimsSection == null) return;

        for (String playerIdStr : claimsSection.getKeys(false)) {
            UUID ownerId;
            try {
                ownerId = UUID.fromString(playerIdStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid player UUID: " + playerIdStr);
                continue;
            }

            List<String> chunkStrings = claimsSection.getStringList(playerIdStr);
            for (String chunkStr : chunkStrings) {
                String[] parts = chunkStr.split(",");
                if (parts.length != 3) {
                    plugin.getLogger().warning("Skipping invalid chunk entry: " + chunkStr);
                    continue;
                }

                try {
                    String world = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    ChunkPosition pos = new ChunkPosition(world, x, z);

                    claimedChunks.put(pos, ownerId);
                    playerClaims.computeIfAbsent(ownerId, k -> new HashSet<>()).add(pos);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Skipping chunk entry with invalid coordinates: " + chunkStr);
                }
            }
        }
    }

    public void saveClaims() {
        FileConfiguration config = configManager.getClaimsConfig();
        config.set("claims", null);

        ConfigurationSection claimsSection = config.createSection("claims");
        for (Map.Entry<UUID, Set<ChunkPosition>> entry : playerClaims.entrySet()) {
            List<String> chunkStrings = new ArrayList<>();
            for (ChunkPosition pos : entry.getValue()) {
                chunkStrings.add(pos.toString());
            }
            claimsSection.set(entry.getKey().toString(), chunkStrings);
        }

        configManager.saveClaimsConfig();
    }

    public boolean claimChunk(Player player, Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        if (configManager.isWorldBlocked(worldName)) {
            player.sendMessage(configManager.getMessage("world-blocked"));
            return false;
        }

        ChunkPosition pos = new ChunkPosition(chunk);
        if (isChunkClaimed(pos)) {
            UUID owner = getChunkOwner(pos);
            String ownerName = plugin.getServer().getOfflinePlayer(owner).getName();
            player.sendMessage(configManager.getMessage("already-claimed", "{owner}", ownerName != null ? ownerName : "Unknown"));
            return false;
        }

        UUID playerId = player.getUniqueId();
        int claimLimit = getClaimLimit(player);
        Set<ChunkPosition> claims = playerClaims.getOrDefault(playerId, new HashSet<>());
        if (claims.size() >= claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "{limit}", String.valueOf(claimLimit)));
            return false;
        }

        if (configManager.requireConnectedClaims() && !claims.isEmpty()) {
            boolean isConnected = isConnectedToOwnClaims(pos, playerId);
            if (!isConnected) {
                player.sendMessage(configManager.getMessage("not-connected"));
                return false;
            }
        }

        int worldGuardGap = configManager.getWorldGuardGap();
        if (worldGuardGap > 0) {
            if (isTooCloseToWorldGuardRegion(pos, worldGuardGap)) {
                player.sendMessage(configManager.getMessage("too-close-to-worldguard", "{gap}", String.valueOf(worldGuardGap)));
                return false;
            }
        }

        int minGap = configManager.getMinClaimGap();
        if (minGap > 0) {
            if (isTooCloseToOtherClaim(worldName, pos, playerId, minGap)) {
                player.sendMessage(configManager.getMessage("too-close-to-other-claim", "{gap}", String.valueOf(minGap)));
                return false;
            }
        }

        claimedChunks.put(pos, playerId);
        claims.add(pos);
        playerClaims.put(playerId, claims);

        plugin.getVisualizationManager().invalidateCache(playerId);
        
        // Mark claims as dirty for debounced save
        if (plugin.getSaveManager() != null) {
            plugin.getSaveManager().markClaimsDirty();
        }
        return true;
    }

    private boolean isTooCloseToOtherClaim(String worldName, ChunkPosition pos, UUID playerId, int minGap) {
        for (int dx = -minGap; dx <= minGap; dx++) {
            for (int dz = -minGap; dz <= minGap; dz++) {
                if (dx == 0 && dz == 0) continue;
                ChunkPosition neighbor = new ChunkPosition(worldName, pos.getX() + dx, pos.getZ() + dz);
                if (claimedChunks.containsKey(neighbor)) {
                    UUID owner = claimedChunks.get(neighbor);
                    if (!owner.equals(playerId)) return true;
                }
            }
        }
        return false;
    }

    private boolean isTooCloseToWorldGuardRegion(ChunkPosition pos, int gap) {
        if (!plugin.isWorldGuardEnabled()) return false;

        World world = Bukkit.getWorld(pos.getWorld());
        if (world == null) return false;

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            if (regionManager == null) return false;

            // Check the chunk area plus gap chunks around it
            int chunkX = pos.getX();
            int chunkZ = pos.getZ();
            
            // Check all chunks within gap distance
            for (int dx = -gap; dx <= gap; dx++) {
                for (int dz = -gap; dz <= gap; dz++) {
                    int checkX = (chunkX + dx) * 16;
                    int checkZ = (chunkZ + dz) * 16;
                    
                    // Check corners and center of the chunk
                    int[][] points = {
                        {checkX, checkZ},
                        {checkX + 15, checkZ},
                        {checkX, checkZ + 15},
                        {checkX + 15, checkZ + 15},
                        {checkX + 8, checkZ + 8}
                    };
                    
                    for (int[] point : points) {
                        BlockVector3 blockVector = BlockVector3.at(point[0], 64, point[1]);
                        for (ProtectedRegion region : regionManager.getApplicableRegions(blockVector)) {
                            // Found a WorldGuard region within gap distance
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking WorldGuard regions: " + e.getMessage());
        }

        return false;
    }

    private boolean isConnectedToOwnClaims(ChunkPosition pos, UUID playerId) {
        Set<ChunkPosition> claims = playerClaims.get(playerId);
        if (claims == null || claims.isEmpty()) return false;

        boolean allowDiagonals = configManager.allowDiagonalConnections();
        for (ChunkPosition neighbor : pos.getNeighbors(allowDiagonals)) {
            if (claims.contains(neighbor)) {
                return true;
            }
        }
        return false;
    }

    public boolean unclaimChunk(Chunk chunk) {
        ChunkPosition pos = new ChunkPosition(chunk);
        if (!isChunkClaimed(pos)) return false;

        UUID owner = claimedChunks.remove(pos);
        if (owner != null) {
            Set<ChunkPosition> claims = playerClaims.get(owner);
            if (claims != null) {
                claims.remove(pos);
                if (claims.isEmpty()) {
                    playerClaims.remove(owner);
                }
            }
            plugin.getVisualizationManager().invalidateCache(owner);
            
            // Mark claims as dirty for debounced save
            if (plugin.getSaveManager() != null) {
                plugin.getSaveManager().markClaimsDirty();
            }
            return true;
        }
        return false;
    }

    public int unclaimAll(UUID playerId) {
        Set<ChunkPosition> claims = playerClaims.getOrDefault(playerId, new HashSet<>());
        if (claims.isEmpty()) return 0;

        Set<ChunkPosition> toRemove = new HashSet<>(claims);
        int count = 0;
        for (ChunkPosition pos : toRemove) {
            World world = Bukkit.getWorld(pos.getWorld());
            if (world != null) {
                Chunk chunk = world.getChunkAt(pos.getX(), pos.getZ());
                if (unclaimChunk(chunk)) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isChunkClaimed(ChunkPosition pos) {
        return claimedChunks.containsKey(pos);
    }

    public UUID getChunkOwner(ChunkPosition pos) {
        return claimedChunks.get(pos);
    }

    public Set<ChunkPosition> getPlayerClaims(UUID playerId) {
        return playerClaims.getOrDefault(playerId, Collections.emptySet());
    }

    public int getClaimLimit(Player player) {
        if (player.hasPermission("landclaim.admin")) return Integer.MAX_VALUE;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.limit." + i)) return i;
        }
        return configManager.getConfig().getInt("chunk-claim-limit", 5);
    }
}
