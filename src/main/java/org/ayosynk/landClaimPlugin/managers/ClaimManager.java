package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
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
        loadProfiles();
    }

    // ========== Profile-based methods ==========

    /**
     * Load all profiles from the database into the cache.
     */
    public void loadProfiles() {
        plugin.getLogger().info("Loading claim profiles from database...");
        plugin.getDatabaseManager().getProfileDao().getAllProfiles().thenAccept(profiles -> {
            for (ClaimProfile profile : profiles) {
                plugin.getCacheManager().getProfileCache().put(profile.getOwnerId(), profile);
            }
            plugin.getLogger().info("Loaded " + profiles.size() + " claim profiles.");
        });
    }

    /**
     * Get the profile that owns a specific chunk position.
     */
    public ClaimProfile getProfileAt(ChunkPosition pos) {
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.ownsChunk(pos)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Get a player's own profile (they must be the owner).
     */
    public ClaimProfile getProfile(UUID ownerId) {
        return plugin.getCacheManager().getProfileCache().getIfPresent(ownerId);
    }

    /**
     * Check if a chunk is claimed by any profile.
     */
    public boolean isChunkClaimed(ChunkPosition pos) {
        return getProfileAt(pos) != null;
    }

    /**
     * Get the owner UUID of the chunk, or null if unclaimed.
     */
    public UUID getChunkOwner(ChunkPosition pos) {
        ClaimProfile profile = getProfileAt(pos);
        return profile != null ? profile.getOwnerId() : null;
    }

    /**
     * Get total number of claimed chunks across all profiles.
     */
    public int getTotalClaims() {
        int total = 0;
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            total += profile.getOwnedChunks().size();
        }
        return total;
    }

    /**
     * Check if a player can create a profile (not owner/member/trusted elsewhere).
     */
    public boolean canCreateProfile(UUID playerId) {
        // Already owns a profile
        if (getProfile(playerId) != null)
            return false;

        // Is a member or trusted in another profile
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.isMember(playerId) || profile.isTrusted(playerId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create or get the player's profile, then claim the given chunk.
     * Auto-creates a profile if the player doesn't have one.
     */
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

        // Check if player is a member/trusted elsewhere — cannot claim
        for (ClaimProfile p : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (!p.isOwner(playerId) && (p.isMember(playerId) || p.isTrusted(playerId))) {
                player.sendMessage(configManager.getMessage("cannot-claim-as-member"));
                return false;
            }
        }

        // Find or create the player's profile
        ClaimProfile profile = getProfile(playerId);
        if (profile == null) {
            String defaultName = player.getName() + "'s Claim";
            profile = new ClaimProfile(playerId, defaultName);
        }

        int claimLimit = getClaimLimit(player);
        int currentTotalChunks = profile.getOwnedChunks().size();

        if (currentTotalChunks >= claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "{limit}", String.valueOf(claimLimit)));
            return false;
        }

        if (configManager.requireConnectedClaims() && currentTotalChunks > 0) {
            boolean isConnected = isConnectedToOwnChunks(pos, profile);
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
            if (isTooCloseToOtherProfile(worldName, pos, playerId, minGap)) {
                player.sendMessage(
                        configManager.getMessage("too-close-to-other-claim", "{gap}", String.valueOf(minGap)));
                return false;
            }
        }

        profile.addChunk(pos);

        // Save to cache
        plugin.getCacheManager().getProfileCache().put(playerId, profile);

        // Save to DB async
        saveAndSync(profile);

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return true;
    }

    /**
     * Claim multiple chunks at once (for selection-based claiming).
     */
    public int claimChunks(Player player, Set<ChunkPosition> chunksToClaim) {
        if (chunksToClaim.isEmpty())
            return 0;

        String worldName = chunksToClaim.iterator().next().world();
        if (configManager.isWorldBlocked(worldName)) {
            player.sendMessage(configManager.getMessage("world-blocked"));
            return 0;
        }

        UUID playerId = player.getUniqueId();

        // Check if player is member/trusted elsewhere
        for (ClaimProfile p : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (!p.isOwner(playerId) && (p.isMember(playerId) || p.isTrusted(playerId))) {
                player.sendMessage(configManager.getMessage("cannot-claim-as-member"));
                return 0;
            }
        }

        ClaimProfile profile = getProfile(playerId);
        if (profile == null) {
            String defaultName = player.getName() + "'s Claim";
            profile = new ClaimProfile(playerId, defaultName);
        }

        int claimLimit = getClaimLimit(player);
        int currentTotalChunks = profile.getOwnedChunks().size();

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
                if (isTooCloseToOtherProfile(worldName, pos, playerId, minGap)) {
                    player.sendMessage(
                            configManager.getMessage("too-close-to-other-claim", "{gap}", String.valueOf(minGap)));
                    return 0;
                }
            }
        }

        if (configManager.requireConnectedClaims() && currentTotalChunks > 0) {
            boolean connected = false;
            for (ChunkPosition pos : chunksToClaim) {
                if (isConnectedToOwnChunks(pos, profile)) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                player.sendMessage(configManager.getMessage("not-connected"));
                return 0;
            }
        }

        for (ChunkPosition pos : chunksToClaim) {
            profile.addChunk(pos);
        }

        plugin.getCacheManager().getProfileCache().put(playerId, profile);
        saveAndSync(profile);

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return chunksToClaim.size();
    }

    /**
     * Unclaim a single chunk from its owning profile.
     */
    public boolean unclaimChunk(Chunk chunk) {
        ChunkPosition pos = new ChunkPosition(chunk);
        ClaimProfile profile = getProfileAt(pos);
        if (profile == null)
            return false;

        UUID owner = profile.getOwnerId();
        profile.removeChunk(pos);

        // If the profile has no more chunks, keep the profile (owner may reclaim later)
        plugin.getCacheManager().getProfileCache().put(owner, profile);
        saveAndSync(profile);

        plugin.getVisualizationManager().invalidateCache(owner);
        plugin.refreshMapHooks();
        return true;
    }

    /**
     * Abandon the player's entire profile — unclaim all chunks and delete all data.
     */
    public int abandonProfile(UUID playerId) {
        ClaimProfile profile = getProfile(playerId);
        if (profile == null)
            return 0;

        int count = profile.getOwnedChunks().size();

        // Remove from cache
        plugin.getCacheManager().getProfileCache().invalidate(playerId);

        // Delete from DB atomically
        plugin.getDatabaseManager().getProfileDao().deleteProfile(playerId).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", playerId);
            }
        });

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.refreshMapHooks();
        return count;
    }

    /**
     * Transfer ownership of an entire profile to a new owner.
     */
    public boolean transferOwnership(UUID oldOwnerId, UUID newOwnerId) {
        ClaimProfile profile = getProfile(oldOwnerId);
        if (profile == null)
            return false;

        // New owner cannot already own a profile
        if (getProfile(newOwnerId) != null)
            return false;

        // Remove from old owner cache
        plugin.getCacheManager().getProfileCache().invalidate(oldOwnerId);

        // Transfer
        profile.setOwnerId(newOwnerId);

        // Save under new owner
        plugin.getCacheManager().getProfileCache().put(newOwnerId, profile);

        // Delete old profile from DB, save new
        plugin.getDatabaseManager().getProfileDao().deleteProfile(oldOwnerId).thenRun(() -> {
            plugin.getDatabaseManager().getProfileDao().saveProfile(profile).thenRun(() -> {
                if (plugin.getRedisManager() != null) {
                    plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", oldOwnerId);
                    plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", newOwnerId);
                }
            });
        });

        plugin.getVisualizationManager().invalidateCache(oldOwnerId);
        plugin.getVisualizationManager().invalidateCache(newOwnerId);
        plugin.refreshMapHooks();
        return true;
    }

    // ========== Backward compatibility ==========

    /**
     * @deprecated Use getProfileAt() instead. Kept for compatibility during
     *             migration.
     */
    @Deprecated
    public Claim getClaimAt(ChunkPosition pos) {
        // Legacy: scan old claim cache
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getChunks().contains(pos) && claim.getParentClaimId() == null) {
                return claim;
            }
        }
        return null;
    }

    @Deprecated
    public Claim getSubClaimAt(ChunkPosition pos) {
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getChunks().contains(pos) && claim.getParentClaimId() != null) {
                return claim;
            }
        }
        return null;
    }

    @Deprecated
    public Set<Claim> getPlayerClaims(UUID playerId) {
        Set<Claim> claims = new HashSet<>();
        for (Claim claim : plugin.getCacheManager().getClaimCache().asMap().values()) {
            if (claim.getOwnerId().equals(playerId) && claim.getParentClaimId() == null) {
                claims.add(claim);
            }
        }
        return claims;
    }

    @Deprecated
    public int unclaimAll(UUID playerId) {
        return abandonProfile(playerId);
    }

    // ========== Internal helpers ==========

    private void saveAndSync(ClaimProfile profile) {
        plugin.getDatabaseManager().getProfileDao().saveProfile(profile).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", profile.getOwnerId());
            }
        });
    }

    private boolean isTooCloseToOtherProfile(String worldName, ChunkPosition pos, UUID playerId, int minGap) {
        for (int dx = -minGap; dx <= minGap; dx++) {
            for (int dz = -minGap; dz <= minGap; dz++) {
                if (dx == 0 && dz == 0)
                    continue;
                ChunkPosition neighbor = new ChunkPosition(worldName, pos.x() + dx, pos.z() + dz);
                ClaimProfile profile = getProfileAt(neighbor);
                if (profile != null && !profile.getOwnerId().equals(playerId)) {
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

    private boolean isConnectedToOwnChunks(ChunkPosition pos, ClaimProfile profile) {
        if (profile.getOwnedChunks().isEmpty())
            return false;

        boolean allowDiagonals = configManager.allowDiagonalConnections();
        for (ChunkPosition neighbor : pos.getNeighbors(allowDiagonals)) {
            if (profile.ownsChunk(neighbor)) {
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
    }
}
