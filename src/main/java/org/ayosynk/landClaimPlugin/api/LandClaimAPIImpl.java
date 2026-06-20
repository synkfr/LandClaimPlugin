package org.ayosynk.landClaimPlugin.api;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.CombatManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.WarpManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.models.ChunkSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the LandClaimAPI.
 */
public class LandClaimAPIImpl implements LandClaimAPI {

    private final LandClaimPlugin plugin;

    public LandClaimAPIImpl(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    private ClaimManager getClaimManager() {
        return plugin.getClaimManager();
    }

    private ConfigManager getConfigManager() {
        return plugin.getConfigManager();
    }

    private WarpManager getWarpManager() {
        return plugin.getWarpManager();
    }

    private CombatManager getCombatManager() {
        return plugin.getCombatManager();
    }

    // ========== Claim Queries ==========

    @Override
    public boolean isChunkClaimed(String world, int chunkX, int chunkZ) {
        return getClaimManager().isChunkClaimed(new ChunkPosition(world, chunkX, chunkZ));
    }

    @Override
    public boolean isChunkClaimed(ChunkPosition pos) {
        return getClaimManager().isChunkClaimed(pos);
    }

    @Override
    public boolean isLocationClaimed(Location location) {
        return getClaimManager().isChunkClaimed(new ChunkPosition(location.getChunk()));
    }

    @Override
    public ClaimProfile getClaimAt(String world, int chunkX, int chunkZ) {
        return getClaimManager().getProfileAt(new ChunkPosition(world, chunkX, chunkZ));
    }

    @Override
    public ClaimProfile getClaimAt(ChunkPosition pos) {
        return getClaimManager().getProfileAt(pos);
    }

    @Override
    public ClaimProfile getClaimAt(Location location) {
        return getClaimManager().getProfileAt(new ChunkPosition(location.getChunk()));
    }

    @Override
    public List<ClaimProfile> getClaimsByOwner(UUID playerId) {
        return getClaimManager().getOwnedProfiles(playerId);
    }

    @Override
    public List<ClaimProfile> getClaimsByMember(UUID playerId) {
        return getClaimManager().getMemberProfiles(playerId);
    }

    @Override
    public ClaimProfile getClaimByName(String name) {
        return getClaimManager().getProfileByName(name);
    }

    @Override
    public Collection<ClaimProfile> getAllClaimProfiles() {
        return getClaimManager().getAllProfiles();
    }

    @Override
    public ClaimProfile getClaimById(UUID profileId) {
        return getClaimManager().getProfileById(profileId);
    }

    @Override
    public int getTotalChunksByOwner(UUID playerId) {
        return getClaimManager().getTotalClaimedChunks(playerId);
    }

    // ========== Permission Checks ==========

    @Override
    public boolean hasPermission(ClaimProfile profile, UUID playerId, String permission) {
        return PermissionResolver.hasPermission(profile, playerId, permission);
    }

    @Override
    public String getPlayerStatus(ClaimProfile profile, UUID playerId) {
        return PermissionResolver.getPlayerStatus(profile, playerId);
    }

    @Override
    public boolean isOwner(ClaimProfile profile, UUID playerId) {
        return profile.isOwner(playerId);
    }

    @Override
    public boolean isMember(ClaimProfile profile, UUID playerId) {
        return profile.isMember(playerId);
    }

    @Override
    public boolean isTrusted(ClaimProfile profile, UUID playerId) {
        return profile.isTrusted(playerId);
    }

    // ========== Warp Operations ==========

    @Override
    public Map<String, Warp> getWarps(UUID profileId) {
        return getWarpManager().getWarps(profileId);
    }

    @Override
    public Warp getWarp(UUID profileId, String warpName) {
        return getWarpManager().getWarp(profileId, warpName);
    }

    // ========== Combat ==========

    @Override
    public boolean isInCombat(Player player) {
        return getCombatManager().isInCombat(player);
    }

    // ========== Claim Limits ==========

    @Override
    public int getClaimLimit(Player player) {
        return getClaimManager().getClaimLimit(player);
    }

    @Override
    public int getClaimLimit(UUID playerId) {
        return getClaimManager().getClaimLimit(playerId);
    }

    @Override
    public boolean canCreateClaim(UUID playerId) {
        return getClaimManager().canCreateProfile(playerId);
    }

    // ========== Admin Operations ==========

    @Override
    public boolean adminClaimChunk(Player player, Location location) {
        if (!player.hasPermission("landclaim.admin")) {
            return false;
        }

        ChunkPosition pos = new ChunkPosition(location.getChunk());
        if (getClaimManager().isChunkClaimed(pos)) {
            return false;
        }

        ClaimProfile adminProfile = getClaimManager().getProfile(ClaimProfile.ADMIN_PROFILE_ID);
        if (adminProfile == null) {
            adminProfile = new ClaimProfile(ClaimProfile.ADMIN_PROFILE_ID, ClaimProfile.ADMIN_PROFILE_ID, "Admin");
            plugin.getCacheManager().getProfileCache().put(adminProfile.getProfileId(), adminProfile);
        }

        adminProfile.addChunk(pos);
        getClaimManager().addToSpatialIndex(pos, adminProfile);
        getClaimManager().saveAndSync(adminProfile);

        plugin.getVisualizationManager().invalidateCache(adminProfile.getProfileId());
        plugin.getHookManager().refreshMapHooks();

        return true;
    }

    @Override
    public boolean adminUnclaimChunk(Player player, Location location) {
        if (!player.hasPermission("landclaim.admin")) {
            return false;
        }

        ChunkPosition pos = new ChunkPosition(location.getChunk());
        return getClaimManager().unclaimChunk(location.getChunk());
    }

    @Override
    public boolean transferClaim(org.bukkit.entity.Player actor, UUID profileId, UUID newOwnerId) {
        if (!isAuthorizedForTransfer(actor, profileId)) return false;
        return getClaimManager().transferClaimProfile(profileId, newOwnerId);
    }

    @Override
    public int unclaimAll(org.bukkit.entity.Player actor, UUID profileId) {
        // A null actor signals a system-initiated call (e.g. tax
        // auto-unclaim) and bypasses the permission check. A non-null
        // actor must hold landclaim.admin.
        if (actor != null && !actor.hasPermission("landclaim.admin")) return 0;
        return getClaimManager().unclaimAllById(profileId);
    }

    /**
     * Authorize a transfer. The actor must have {@code landclaim.admin}
     * or be the <em>current owner</em> of the claim being transferred.
     * We do NOT allow {@code actor == newOwnerId} as a bypass — that
     * would let any buyer unilaterally take over any claim they want,
     * which is the bug this gate fixes. Marketplace / auction flows
     * where the buyer is not the current owner go through the
     * pre-authorization API (added separately) instead.
     */
    private boolean isAuthorizedForTransfer(org.bukkit.entity.Player actor, UUID profileId) {
        if (actor == null) return false;
        if (actor.hasPermission("landclaim.admin")) return true;
        ClaimProfile source = getClaimManager().getProfileById(profileId);
        return source != null && source.getOwnerId().equals(actor.getUniqueId());
    }

    @Override
    public CompletableFuture<Integer> addBonusBlocks(UUID playerId, int amount) {
        return plugin.getDatabaseManager().getPlayerDao().getPlayer(playerId).thenApply(player -> {
            if (player == null) {
                player = new ClaimPlayer(playerId);
            }
            player.setBonusClaimBlocks(player.getBonusClaimBlocks() + amount);
            plugin.getDatabaseManager().getPlayerDao().savePlayer(player);
            plugin.getCacheManager().getPlayerCache().put(playerId, player);
            return player.getBonusClaimBlocks();
        });
    }

    @Override
    public CompletableFuture<Integer> getBonusBlocks(UUID playerId) {
        return plugin.getDatabaseManager().getPlayerDao().getPlayer(playerId).thenApply(player -> {
            return player != null ? player.getBonusClaimBlocks() : 0;
        });
    }

    // ========== Utility ==========

    @Override
    public long getServerTime() {
        return System.currentTimeMillis();
    }
}