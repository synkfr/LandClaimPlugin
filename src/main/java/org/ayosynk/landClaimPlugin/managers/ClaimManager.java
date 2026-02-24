package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.ayosynk.landClaimPlugin.models.ChunkSelection;

public class ClaimManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, ChunkSelection> playerSelections = new HashMap<>();

    public ClaimManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        loadClaims();
    }

    public int getTotalClaims() {
        return (int) plugin.getCacheManager().getClaimCache().estimatedSize();
    }

    public void loadClaims() {
        plugin.getLogger().info("Loading claims from database...");
        plugin.getDatabaseManager().getClaimDao().getAllClaims().thenAccept(claims -> {
            for (Claim claim : claims) {
                plugin.getCacheManager().getClaimCache().put(claim.getId(), claim);
            }
            plugin.getLogger().info("Loaded " + claims.size() + " claims.");
        });
    }

    public Claim getClaimAt(ChunkPosition pos) {
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getChunks().contains(pos) && claim.getParentClaimId() == null) {
                return claim;
            }
        }
        return null;
    }

    public Claim getSubClaimAt(ChunkPosition pos) {
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getChunks().contains(pos) && claim.getParentClaimId() != null) {
                return claim;
            }
        }
        return null;
    }

    public boolean isChunkClaimed(ChunkPosition pos) {
        return getClaimAt(pos) != null;
    }

    public UUID getChunkOwner(ChunkPosition pos) {
        Claim claim = getClaimAt(pos);
        return claim != null ? claim.getOwnerId() : null;
    }

    public Set<Claim> getPlayerClaims(UUID playerId) {
        Set<Claim> claims = new HashSet<>();
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getOwnerId().equals(playerId) && claim.getParentClaimId() == null) {
                claims.add(claim);
            }
        }
        return claims;
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
            player.sendMessage(
                    configManager.getMessage("already-claimed", "{owner}", ownerName != null ? ownerName : "Unknown"));
            return false;
        }

        UUID playerId = player.getUniqueId();
        int claimLimit = getClaimLimit(player);
        Set<Claim> claims = getPlayerClaims(playerId);
        int currentTotalChunks = claims.stream().mapToInt(c -> c.getChunks().size()).sum();

        if (currentTotalChunks >= claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "{limit}", String.valueOf(claimLimit)));
            return false;
        }

        if (configManager.requireConnectedClaims() && currentTotalChunks > 0) {
            boolean isConnected = isConnectedToOwnClaims(pos, playerId);
            if (!isConnected) {
                player.sendMessage(configManager.getMessage("not-connected"));
                return false;
            }
        }

        int worldGuardGap = configManager.getWorldGuardGap();
        if (worldGuardGap > 0) {
            if (isTooCloseToWorldGuardRegion(pos, worldGuardGap)) {
                player.sendMessage(
                        configManager.getMessage("too-close-to-worldguard", "{gap}", String.valueOf(worldGuardGap)));
                return false;
            }
        }

        int minGap = configManager.getMinClaimGap();
        if (minGap > 0) {
            if (isTooCloseToOtherClaim(worldName, pos, playerId, minGap)) {
                player.sendMessage(
                        configManager.getMessage("too-close-to-other-claim", "{gap}", String.valueOf(minGap)));
                return false;
            }
        }

        Claim newClaim = new Claim(UUID.randomUUID(), playerId);
        newClaim.addChunk(pos);
        long expireDays = 30L; // TODO: Pull from config
        newClaim.setExpireAt(System.currentTimeMillis() + (expireDays * 24 * 60 * 60 * 1000));

        // Save to cache immediately
        plugin.getCacheManager().getClaimCache().put(newClaim.getId(), newClaim);

        // Save to DB async
        plugin.getDatabaseManager().getClaimDao().saveClaim(newClaim).thenRun(() -> {
            // Sync with Redis
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", newClaim.getId());
            }
        });

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return true;
    }

    public int claimChunks(Player player, Set<ChunkPosition> chunksToClaim) {
        if (chunksToClaim.isEmpty())
            return 0;

        String worldName = chunksToClaim.iterator().next().world();
        if (configManager.isWorldBlocked(worldName)) {
            player.sendMessage(configManager.getMessage("world-blocked"));
            return 0;
        }

        UUID playerId = player.getUniqueId();
        int claimLimit = getClaimLimit(player);
        Set<Claim> claims = getPlayerClaims(playerId);
        int currentTotalChunks = claims.stream().mapToInt(c -> c.getChunks().size()).sum();

        if (currentTotalChunks + chunksToClaim.size() > claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "{limit}", String.valueOf(claimLimit)));
            return 0;
        }

        // Validate all chunks
        for (ChunkPosition pos : chunksToClaim) {
            if (!pos.world().equals(worldName))
                return 0;

            if (isChunkClaimed(pos)) {
                UUID owner = getChunkOwner(pos);
                String ownerName = plugin.getServer().getOfflinePlayer(owner).getName();
                player.sendMessage(
                        configManager.getMessage("already-claimed", "{owner}",
                                ownerName != null ? ownerName : "Unknown"));
                return 0;
            }

            int worldGuardGap = configManager.getWorldGuardGap();
            if (worldGuardGap > 0) {
                if (isTooCloseToWorldGuardRegion(pos, worldGuardGap)) {
                    player.sendMessage(
                            configManager.getMessage("too-close-to-worldguard", "{gap}",
                                    String.valueOf(worldGuardGap)));
                    return 0;
                }
            }

            int minGap = configManager.getMinClaimGap();
            if (minGap > 0) {
                if (isTooCloseToOtherClaim(worldName, pos, playerId, minGap)) {
                    player.sendMessage(
                            configManager.getMessage("too-close-to-other-claim", "{gap}", String.valueOf(minGap)));
                    return 0;
                }
            }
        }

        if (configManager.requireConnectedClaims() && currentTotalChunks > 0) {
            boolean connected = false;
            for (ChunkPosition pos : chunksToClaim) {
                if (isConnectedToOwnClaims(pos, playerId)) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                player.sendMessage(configManager.getMessage("not-connected"));
                return 0;
            }
        }

        Claim newClaim = new Claim(UUID.randomUUID(), playerId);
        for (ChunkPosition pos : chunksToClaim) {
            newClaim.addChunk(pos);
        }
        long expireDays = 30L; // TODO: Pull from config
        newClaim.setExpireAt(System.currentTimeMillis() + (expireDays * 24 * 60 * 60 * 1000));

        plugin.getCacheManager().getClaimCache().put(newClaim.getId(), newClaim);

        plugin.getDatabaseManager().getClaimDao().saveClaim(newClaim).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", newClaim.getId());
            }
        });

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return chunksToClaim.size();
    }

    public boolean unclaimChunk(Chunk chunk) {
        ChunkPosition pos = new ChunkPosition(chunk);

        Claim claim = getSubClaimAt(pos);
        if (claim == null) {
            claim = getClaimAt(pos);
        }

        if (claim == null)
            return false;

        UUID owner = claim.getOwnerId();
        UUID claimId = claim.getId();
        UUID parentClaimId = claim.getParentClaimId();

        // Remove from cache
        plugin.getCacheManager().getClaimCache().invalidate(claimId);

        // Delete from DB async
        plugin.getDatabaseManager().getClaimDao().deleteClaim(claimId).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", claimId);
            }
        });

        // Also if this is a parent claim, delete all sub-claims inside it
        if (parentClaimId == null) {
            for (Claim c : plugin.getCacheManager().getClaimCache().asMap().values()) {
                if (claimId.equals(c.getParentClaimId())) {
                    UUID subId = c.getId();
                    plugin.getCacheManager().getClaimCache().invalidate(subId);
                    plugin.getDatabaseManager().getClaimDao().deleteClaim(subId);
                    if (plugin.getRedisManager() != null) {
                        plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", subId);
                    }
                }
            }
        }

        plugin.getVisualizationManager().invalidateCache(owner);
        plugin.refreshMapHooks();
        return true;
    }

    public int unclaimAll(UUID playerId) {
        Set<Claim> claims = getPlayerClaims(playerId);
        if (claims.isEmpty())
            return 0;

        int count = 0;
        for (Claim claim : claims) {
            plugin.getCacheManager().getClaimCache().invalidate(claim.getId());
            plugin.getDatabaseManager().getClaimDao().deleteClaim(claim.getId());
            count++;

            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", claim.getId());
            }
        }

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return count;
    }

    private boolean isTooCloseToOtherClaim(String worldName, ChunkPosition pos, UUID playerId, int minGap) {
        for (int dx = -minGap; dx <= minGap; dx++) {
            for (int dz = -minGap; dz <= minGap; dz++) {
                if (dx == 0 && dz == 0)
                    continue;
                ChunkPosition neighbor = new ChunkPosition(worldName, pos.x() + dx, pos.z() + dz);
                Claim claim = getClaimAt(neighbor);
                if (claim != null && !claim.getOwnerId().equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTooCloseToWorldGuardRegion(ChunkPosition pos, int gap) {
        if (!plugin.isWorldGuardEnabled())
            return false;

        World world = Bukkit.getWorld(pos.world());
        if (world == null)
            return false;

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            if (regionManager == null)
                return false;

            int chunkX = pos.x();
            int chunkZ = pos.z();

            for (int dx = -gap; dx <= gap; dx++) {
                for (int dz = -gap; dz <= gap; dz++) {
                    int checkX = (chunkX + dx) * 16;
                    int checkZ = (chunkZ + dz) * 16;

                    int[][] points = {
                            { checkX, checkZ }, { checkX + 15, checkZ },
                            { checkX, checkZ + 15 }, { checkX + 15, checkZ + 15 },
                            { checkX + 8, checkZ + 8 }
                    };

                    for (int[] point : points) {
                        BlockVector3 blockVector = BlockVector3.at(point[0], 64, point[1]);
                        ApplicableRegionSet regions = regionManager.getApplicableRegions(blockVector);
                        if (regions.size() > 0) {
                            for (ProtectedRegion region : regions) {
                                if (!region.getId().equals("__global__")) {
                                    return true;
                                }
                            }
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
        Set<Claim> claims = getPlayerClaims(playerId);
        if (claims.isEmpty())
            return false;

        boolean allowDiagonals = configManager.allowDiagonalConnections();
        for (ChunkPosition neighbor : pos.getNeighbors(allowDiagonals)) {
            Claim claim = getClaimAt(neighbor);
            if (claim != null && claim.getOwnerId().equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    public int getClaimLimit(Player player) {
        if (player.hasPermission("landclaim.admin"))
            return Integer.MAX_VALUE;

        int limit = configManager.getPluginConfig().chunkClaimLimit;
        for (int i = 100; i > 0; i--) {
            if (player.hasPermission("landclaim.limit." + i)) {
                limit = i;
                break;
            }
        }

        org.ayosynk.landClaimPlugin.models.ClaimPlayer cp = plugin.getCacheManager().getPlayerCache()
                .getIfPresent(player.getUniqueId());
        if (cp != null) {
            limit += cp.getBonusClaimBlocks();
        }
        return limit;
    }

    public ChunkSelection getSelection(UUID playerId) {
        return playerSelections.computeIfAbsent(playerId, k -> new ChunkSelection());
    }

    public void clearSelection(UUID playerId) {
        playerSelections.remove(playerId);
        plugin.getVisualizationManager().clearSelectionDisplays(playerId);
    }
}
