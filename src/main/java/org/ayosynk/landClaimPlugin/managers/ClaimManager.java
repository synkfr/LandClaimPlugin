package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;


import java.util.*;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.ayosynk.landClaimPlugin.models.ChunkSelection;

public class ClaimManager {
    private final LandClaimPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, ChunkSelection> playerSelections = new HashMap<>();

    // Map of <ReceiverProfileOwnerId, Set<SenderProfileOwnerId>> for pending ally
    // invites
    private final Map<UUID, Set<UUID>> pendingAllyInvites = new HashMap<>();

    // Pending member invites: Invitee UUID -> Owner UUID
    private final Map<UUID, UUID> pendingMemberInvites = new ConcurrentHashMap<>();

    // Pending trust invites: Invitee UUID -> Owner UUID
    private final Map<UUID, UUID> pendingTrustInvites = new ConcurrentHashMap<>();
    
    // Spatial index: ChunkPosition -> ClaimProfile for O(1) lookups
    private final Map<ChunkPosition, ClaimProfile> chunkToProfileMap = new ConcurrentHashMap<>();
    
    // Helper methods for spatial index management (package-private for RedisManager access)
    public void addToSpatialIndex(ChunkPosition chunk, ClaimProfile profile) {
        chunkToProfileMap.put(chunk, profile);
    }
    
    void removeFromSpatialIndex(ChunkPosition chunk) {
        chunkToProfileMap.remove(chunk);
    }
    
    void rebuildProfileIndex(ClaimProfile profile) {
        // Remove all existing entries for this profile
        chunkToProfileMap.values().removeIf(p -> p.getProfileId().equals(profile.getProfileId()));
        // Add all current chunks
        for (ChunkPosition chunk : profile.getOwnedChunks()) {
            chunkToProfileMap.put(chunk, profile);
        }
    }
    
    void removeAllChunksForProfile(UUID ownerId) {
        chunkToProfileMap.values().removeIf(p -> p.getProfileId().equals(ownerId));
    }

    public ClaimManager(LandClaimPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void initialize() {
        loadProfiles();
    }

    // ========== Profile-based methods ==========

    /**
     * Load all profiles from the database into the cache and rebuild spatial index.
     */
    public void loadProfiles() {
        plugin.getLogger().info("Loading claim profiles from database...");
        plugin.getDatabaseManager().getProfileDao().getAllProfiles()
            .thenAccept(profiles -> {
                // Clear existing spatial index
                chunkToProfileMap.clear();
                
                for (ClaimProfile profile : profiles) {
                    // Populate warps from WarpManager
                    Map<String, Warp> profileWarps = plugin.getWarpManager().getWarps(profile.getProfileId());
                    if (!profileWarps.isEmpty()) {
                        for (Warp warp : profileWarps.values()) {
                            profile.addWarp(warp);
                        }
                    }
                    // Add to cache
                    plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                    // Update spatial index for all chunks
                    for (ChunkPosition chunk : profile.getOwnedChunks()) {
                        chunkToProfileMap.put(chunk, profile);
                    }
                }
                plugin.getLogger().info("Loaded " + profiles.size() + " claim profiles. Spatial index contains " + 
                        chunkToProfileMap.size() + " chunks.");
            })
            .exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to load claim profiles: " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
    }

    /**
     * Get all cached profiles.
     */
    public Collection<ClaimProfile> getAllProfiles() {
        return plugin.getCacheManager().getProfileCache().asMap().values();
    }

    /**
     * Get the profile that owns a specific chunk position.
     * Uses spatial index for O(1) lookup.
     */
    public ClaimProfile getProfileAt(ChunkPosition pos) {
        return chunkToProfileMap.get(pos);
    }

    /**
     * Get a player's own profile (they must be the owner).
     */
    public ClaimProfile getProfile(UUID ownerId) {
        return plugin.getCacheManager().getProfileCache().getIfPresent(ownerId);
    }

    /**
     * Get the active profile for a player.
     * If multi-profiles are disabled, returns their default owned profile.
     * If enabled, returns the profile selected as active (or the first owned/member profile).
     */
    public ClaimProfile getActiveProfile(Player player) {
        if (!plugin.getConfigManager().isMultiProfilesEnabled()) {
            return getProfile(player.getUniqueId());
        }
        
        org.ayosynk.landClaimPlugin.models.ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(player.getUniqueId());
        if (cp != null && cp.getActiveProfileId() != null) {
            ClaimProfile active = getProfile(cp.getActiveProfileId());
            if (active != null && (active.isOwner(player.getUniqueId()) || active.isMember(player.getUniqueId()))) {
                return active;
            }
        }
        
        // Fallback to first owned profile
        java.util.List<ClaimProfile> owned = getOwnedProfiles(player.getUniqueId());
        if (!owned.isEmpty()) {
            ClaimProfile first = owned.get(0);
            if (cp != null) {
                cp.setActiveProfileId(first.getProfileId());
                plugin.getDatabaseManager().getPlayerDao().savePlayer(cp);
            }
            return first;
        }
        
        return null;
    }

    /**
     * Get all profiles owned by a player.
     */
    public java.util.List<ClaimProfile> getOwnedProfiles(UUID realOwnerId) {
        java.util.List<ClaimProfile> owned = new java.util.ArrayList<>();
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.isOwner(realOwnerId)) {
                owned.add(profile);
            }
        }
        return owned;
    }

    /**
     * Get total chunks claimed across all profiles owned by a player.
     */
    public int getTotalClaimedChunks(UUID realOwnerId) {
        int total = 0;
        for (ClaimProfile profile : getOwnedProfiles(realOwnerId)) {
            total += profile.getOwnedChunks().size();
        }
        return total;
    }

    /**
     * Get a profile by its global name or by the owner's username.
     */
    public ClaimProfile getProfileByNameOrOwner(String query) {
        // 1. Try exact match for claim name
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.getName().equalsIgnoreCase(query)) {
                return profile;
            }
        }
        
        // 2. Try match for owner's username
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            org.bukkit.OfflinePlayer op = plugin.getServer().getOfflinePlayer(profile.getOwnerId());
            if (op.getName() != null && op.getName().equalsIgnoreCase(query)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Get a profile by its global name (only claim name).
     */
    public ClaimProfile getProfileByName(String name) {
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Check if a claim name is globally unique.
     */
    public boolean isClaimNameUnique(String name) {
        return getProfileByName(name) == null;
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
        return profile != null ? profile.getProfileId() : null;
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

    // ========== Ally Invites ==========

    public void addAllyInvite(UUID receiverOwnerId, UUID senderOwnerId) {
        pendingAllyInvites.computeIfAbsent(receiverOwnerId, k -> new HashSet<>()).add(senderOwnerId);
    }

    public void removeAllyInvite(UUID receiverOwnerId, UUID senderOwnerId) {
        Set<UUID> senders = pendingAllyInvites.get(receiverOwnerId);
        if (senders != null) {
            senders.remove(senderOwnerId);
            if (senders.isEmpty()) {
                pendingAllyInvites.remove(receiverOwnerId);
            }
        }
    }

    public void sendAllyInvite(Player sender, ClaimProfile targetProfile) {
        addAllyInvite(targetProfile.getProfileId(), sender.getUniqueId());
        sender.sendMessage(configManager.getMessage("ally-invite-sent", "<name>", targetProfile.getName()));

        Player targetOwner = Bukkit.getPlayer(targetProfile.getProfileId());
        if (targetOwner != null && targetOwner.isOnline()) {
            targetOwner.sendMessage(configManager.getMessage("ally-invite-received", "<name>", sender.getName()));
        }
    }

    public boolean hasAllyInvite(UUID receiverOwnerId, UUID senderOwnerId) {
        Set<UUID> senders = pendingAllyInvites.get(receiverOwnerId);
        return senders != null && senders.contains(senderOwnerId);
    }

    // ========== Member Invites ==========

    public void sendMemberInvite(Player sender, Player target, ClaimProfile profile) {
        pendingMemberInvites.put(target.getUniqueId(), profile.getProfileId());

        sender.sendMessage(configManager.getMessage("member-invited", "<player>", target.getName()));
        target.sendMessage(configManager.getMessage("invite-received", "<owner>", profile.getName()));
    }

    public UUID getAndRemoveMemberInvite(UUID inviteeId) {
        return pendingMemberInvites.remove(inviteeId);
    }

    // ========== Trust Invites ==========

    public void sendTrustInvite(Player sender, Player target, ClaimProfile profile) {
        pendingTrustInvites.put(target.getUniqueId(), sender.getUniqueId());

        sender.sendMessage(configManager.getMessage("trust-invited", "<player>", target.getName()));
        target.sendMessage(configManager.getMessage("trust-invite-received", "<owner>", sender.getName()));
    }

    public UUID getAndRemoveTrustInvite(UUID inviteeId) {
        return pendingTrustInvites.remove(inviteeId);
    }

    /**
     * Get all profiles where the player is a member.
     */
    public java.util.List<ClaimProfile> getMemberProfiles(UUID playerId) {
        java.util.List<ClaimProfile> list = new java.util.ArrayList<>();
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.isMember(playerId)) {
                list.add(profile);
            }
        }
        return list;
    }

    /**
     * Check if a player can create a profile.
     * They cannot if they already own one, or if they are a member/trusted in others
     * beyond what's allowed.
     */
    public boolean canCreateProfile(UUID playerId) {
        if (plugin.getConfigManager().isMultiProfilesEnabled()) {
            return true; // Limit checks handled individually when creating profile.
        }

        if (getProfile(playerId) != null) return false;
        
        for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (profile.isMember(playerId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create or get the player's profile, then claim the given chunk.
     * Auto-creates a profile if the player doesn't have one.
     * Supports Delegated Claiming if the player has the CLAIM_LAND role flag.
     */
    public boolean claimChunk(Player player, Chunk chunk) {
        // Enforce landclaim.claim at the lowest level so /claim, auto-claim,
        // the claim-map GUI click, and any API caller all respect the permission.
        if (!player.hasPermission("landclaim.claim") && !player.hasPermission("landclaim.admin")) {
            player.sendMessage(configManager.getMessage("access-denied"));
            return false;
        }

        String worldName = chunk.getWorld().getName();
        if (configManager.isWorldBlocked(worldName)) {
            player.sendMessage(configManager.getMessage("world-blocked"));
            return false;
        }

        ChunkPosition pos = new ChunkPosition(chunk);
        if (isChunkClaimed(pos)) {
            UUID owner = getChunkOwner(pos);
            ClaimProfile ownerProfile = getProfile(owner);
            String ownerName = ownerProfile != null ? ownerProfile.getDisplayOwnerName() : plugin.getServer().getOfflinePlayer(owner).getName();
            player.sendMessage(
                    configManager.getMessage("already-claimed", "<owner>", ownerName != null ? ownerName : "Unknown"));
            return false;
        }

        UUID playerId = player.getUniqueId();
        ClaimProfile targetProfile = getActiveProfile(player);

        // Delegated claiming check
        if (targetProfile == null) {
            java.util.List<ClaimProfile> memberProfiles = getMemberProfiles(playerId);
            if (!memberProfiles.isEmpty()) {
                // Find one where they have CLAIM_LAND
                ClaimProfile delegated = null;
                for (ClaimProfile mp : memberProfiles) {
                    String roleName = mp.getMemberRole(playerId);
                    if (roleName != null) {
                        org.ayosynk.landClaimPlugin.models.Role role = mp.getRoleByName(roleName);
                        if (role != null && role.hasFlag("CLAIM_LAND")) {
                            delegated = mp;
                            break;
                        }
                    }
                }

                if (delegated != null) {
                    targetProfile = delegated;
                } else {
                    player.sendMessage(configManager.getMessage("cannot-claim-as-member"));
                    return false;
                }
            } else {
                // Not a member anywhere, create their own profile
                String defaultName = player.getName() + "'s Claim";
                targetProfile = new ClaimProfile(playerId, defaultName);
                plugin.getCacheManager().getProfileCache().put(playerId, targetProfile); // Pre-cache so limits work
            }
        }

        int claimLimit = getClaimLimit(targetProfile.getOwnerId());
        int currentTotalChunks = targetProfile.getOwnedChunks().size();
        int globalTotalChunks = getTotalClaimedChunks(targetProfile.getOwnerId());

        if (globalTotalChunks >= claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "<limit>", String.valueOf(claimLimit)));
            return false;
        }

        if (configManager.requireConnectedClaims() && currentTotalChunks > 0) {
            boolean isConnected = isConnectedToOwnChunks(pos, targetProfile);
            if (!isConnected) {
                player.sendMessage(configManager.getMessage("not-connected"));
                return false;
            }
        }

        int worldGuardGap = configManager.getWorldGuardGap();
        if (worldGuardGap > 0) {
            if (isTooCloseToWorldGuardRegion(pos, worldGuardGap)) {
                player.sendMessage(
                        configManager.getMessage("too-close-to-worldguard", "<gap>", String.valueOf(worldGuardGap)));
                return false;
            }
        }

        int minGap = configManager.getMinClaimGap();
        if (minGap > 0) {
            if (isTooCloseToOtherProfile(worldName, pos, targetProfile.getProfileId(), minGap)) { // ensure targetProfile owner ID
                player.sendMessage(
                        configManager.getMessage("too-close-to-other-claim", "<gap>", String.valueOf(minGap)));
                return false;
            }
        }

        targetProfile.addChunk(pos);
        addToSpatialIndex(pos, targetProfile);

        // Save to cache
        plugin.getCacheManager().getProfileCache().put(targetProfile.getProfileId(), targetProfile);

        // Save to DB async
        saveAndSync(targetProfile);

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.getHookManager().refreshMapHooks();
        return true;
    }

    /**
     * Claim multiple chunks at once (for selection-based claiming).
     */
    public int claimChunks(Player player, Set<ChunkPosition> chunksToClaim) {
        // Enforce landclaim.claim at the lowest level so /claim, auto-claim,
        // the claim-map GUI click, and any API caller all respect the permission.
        if (!player.hasPermission("landclaim.claim") && !player.hasPermission("landclaim.admin")) {
            player.sendMessage(configManager.getMessage("access-denied"));
            return 0;
        }

        if (chunksToClaim.isEmpty())
            return 0;

        String worldName = chunksToClaim.iterator().next().world();

        UUID playerId = player.getUniqueId();

        // Check if player is member/trusted elsewhere
        for (ClaimProfile p : plugin.getCacheManager().getProfileCache().asMap().values()) {
            if (!p.isOwner(playerId) && (p.isMember(playerId) || p.isTrusted(playerId))) {
                player.sendMessage(configManager.getMessage("cannot-claim-as-member"));
                return 0;
            }
        }

        ClaimProfile profile = getActiveProfile(player);
        if (profile == null) {
            String defaultName = player.getName() + "'s Claim";
            profile = new ClaimProfile(playerId, defaultName);
        }

        int claimLimit = getClaimLimit(player);
        int currentTotalChunks = profile.getOwnedChunks().size();

        if (currentTotalChunks + chunksToClaim.size() > claimLimit) {
            player.sendMessage(configManager.getMessage("claim-limit-reached", "<limit>", String.valueOf(claimLimit)));
            return 0;
        }

        // Validate all chunks are in same world and that world is not blocked
        for (ChunkPosition pos : chunksToClaim) {
            if (!pos.world().equals(worldName))
                return 0;
            if (configManager.isWorldBlocked(pos.world())) {
                player.sendMessage(configManager.getMessage("world-blocked"));
                return 0;
            }
        }
        
        // Validate each chunk for other restrictions
        for (ChunkPosition pos : chunksToClaim) {
            if (isChunkClaimed(pos)) {
                UUID owner = getChunkOwner(pos);
                ClaimProfile ownerProfile = getProfile(owner);
                String ownerName = ownerProfile != null ? ownerProfile.getDisplayOwnerName() : plugin.getServer().getOfflinePlayer(owner).getName();
                player.sendMessage(
                        configManager.getMessage("already-claimed", "<owner>",
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
            addToSpatialIndex(pos, profile);
        }

        plugin.getCacheManager().getProfileCache().put(playerId, profile);
        saveAndSync(profile);

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.getHookManager().refreshMapHooks();
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

        UUID owner = profile.getProfileId();
        profile.removeChunk(pos);
        removeFromSpatialIndex(pos);

        // If the profile has no more chunks, keep the profile (owner may reclaim later)
        plugin.getCacheManager().getProfileCache().put(owner, profile);
        saveAndSync(profile);

        plugin.getVisualizationManager().invalidateCache(owner);
        plugin.getHookManager().refreshMapHooks();
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

        // Remove all chunks from spatial index
        for (ChunkPosition chunk : profile.getOwnedChunks()) {
            removeFromSpatialIndex(chunk);
        }

        // Remove from cache
        plugin.getCacheManager().getProfileCache().invalidate(playerId);

        // Delete from DB atomically
        plugin.getDatabaseManager().getProfileDao().deleteProfile(playerId)
            .thenRun(() -> {
                if (plugin.getRedisManager() != null) {
                    plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", playerId);
                }
            })
            .exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to delete profile for " + playerId + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });

        plugin.getVisualizationManager().invalidateCache(playerId);
        plugin.getHookManager().refreshMapHooks();
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
        plugin.getDatabaseManager().getProfileDao().deleteProfile(oldOwnerId)
            .thenRun(() -> {
                plugin.getDatabaseManager().getProfileDao().saveProfile(profile)
                    .thenRun(() -> {
                        if (plugin.getRedisManager() != null) {
                            plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", oldOwnerId);
                            plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", newOwnerId);
                        }
                    })
                    .exceptionally(throwable -> {
                        plugin.getLogger().severe("Failed to save transferred profile for " + newOwnerId + ": " + throwable.getMessage());
                        throwable.printStackTrace();
                        return null;
                    });
            })
            .exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to delete old profile for " + oldOwnerId + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });

        plugin.getVisualizationManager().invalidateCache(oldOwnerId);
        plugin.getVisualizationManager().invalidateCache(newOwnerId);
        plugin.getHookManager().refreshMapHooks();
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
            if (claim.getProfileId().equals(playerId) && claim.getParentClaimId() == null) {
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

    public void saveAndSync(ClaimProfile profile) {
        plugin.getDatabaseManager().getProfileDao().saveProfile(profile)
            .thenRun(() -> {
                if (plugin.getRedisManager() != null) {
                    plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE", profile.getProfileId());
                }
            })
            .exceptionally(throwable -> {
                plugin.getLogger().severe("Failed to save profile for " + profile.getProfileId() + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
    }

    private boolean isTooCloseToOtherProfile(String worldName, ChunkPosition pos, UUID playerId, int minGap) {
        for (int dx = -minGap; dx <= minGap; dx++) {
            for (int dz = -minGap; dz <= minGap; dz++) {
                if (dx == 0 && dz == 0)
                    continue;
                ChunkPosition neighbor = new ChunkPosition(worldName, pos.x() + dx, pos.z() + dz);
                ClaimProfile profile = getProfileAt(neighbor);
                if (profile != null && !profile.getProfileId().equals(playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTooCloseToWorldGuardRegion(ChunkPosition pos, int gap) {
        if (!plugin.getHookManager().isWorldGuardEnabled())
            return false;

        return org.ayosynk.landClaimPlugin.hooks.wg.WorldGuardHook.isTooCloseToWorldGuardRegion(pos, gap);
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
        return getClaimLimit(player.getUniqueId(), player);
    }

    public int getClaimLimit(UUID playerId) {
        return getClaimLimit(playerId, Bukkit.getPlayer(playerId));
    }

    private int getClaimLimit(UUID playerId, Player player) {
        int limit = configManager.getPluginConfig().chunkClaimLimit;
        
        if (player != null) {
            if (player.hasPermission("landclaim.admin"))
                return Integer.MAX_VALUE;

            for (org.bukkit.permissions.PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
                String perm = permInfo.getPermission();
                if (perm.startsWith("landclaim.limit.")) {
                    try {
                        int amount = Integer.parseInt(perm.substring(16)); // length of "landclaim.limit."
                        if (amount > limit) {
                            limit = amount;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        org.ayosynk.landClaimPlugin.models.ClaimPlayer cp = plugin.getCacheManager().getPlayerCache()
                .getIfPresent(playerId);
        // If not in cache, we could load from DB, but usually if they have bonus blocks we'd have it in cache or DB.
        // For now, use cache.
        if (cp != null) {
            limit += cp.getBonusClaimBlocks();
        } else {
            // Fallback to fetch from DB synchronously if not in cache (since claiming is synchronous mostly, but this is a quick fix)
            org.ayosynk.landClaimPlugin.models.ClaimPlayer dbCp = null;
            try {
                dbCp = plugin.getDatabaseManager().getPlayerDao().getPlayer(playerId).join();
            } catch (Exception ignored) {}
            if (dbCp != null) {
                limit += dbCp.getBonusClaimBlocks();
            }
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
