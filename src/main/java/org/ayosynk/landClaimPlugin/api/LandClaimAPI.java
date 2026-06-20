package org.ayosynk.landClaimPlugin.api;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for external plugins to integrate with LandClaimPlugin.
 *
 * Usage:
 * <pre>
 * LandClaimAPI api = LandClaimAPI.getInstance();
 * ClaimProfile profile = api.getClaimAt(player.getLocation());
 * </pre>
 */
public interface LandClaimAPI {

    /**
     * Get the singleton API instance.
     */
    static LandClaimAPI getInstance() {
        return LandClaimPlugin.getInstance();
    }

    // ========== Claim Queries ==========

    /**
     * Check if a chunk is claimed.
     *
     * @param world The world name
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return true if claimed
     */
    boolean isChunkClaimed(String world, int chunkX, int chunkZ);

    /**
     * Check if a chunk is claimed.
     *
     * @param pos The chunk position
     * @return true if claimed
     */
    boolean isChunkClaimed(ChunkPosition pos);

    /**
     * Check if a location's chunk is claimed.
     *
     * @param location The location
     * @return true if the chunk is claimed
     */
    boolean isLocationClaimed(Location location);

    /**
     * Get the claim profile at a specific position.
     *
     * @param world The world name
     * @param chunkX The chunk X coordinate
     * @param chunkZ The chunk Z coordinate
     * @return The ClaimProfile, or null if unclaimed
     */
    ClaimProfile getClaimAt(String world, int chunkX, int chunkZ);

    /**
     * Get the claim profile at a specific position.
     *
     * @param pos The chunk position
     * @return The ClaimProfile, or null if unclaimed
     */
    ClaimProfile getClaimAt(ChunkPosition pos);

    /**
     * Get the claim profile at a location.
     *
     * @param location The location
     * @return The ClaimProfile, or null if unclaimed
     */
    ClaimProfile getClaimAt(Location location);

    /**
     * Get all claims owned by a player.
     *
     * @param playerId The player's UUID
     * @return List of claim profiles owned by this player
     */
    List<ClaimProfile> getClaimsByOwner(UUID playerId);

    /**
     * Get all claims where a player is a member.
     *
     * @param playerId The player's UUID
     * @return List of claim profiles where this player is a member
     */
    List<ClaimProfile> getClaimsByMember(UUID playerId);

    /**
     * Get every claim profile on the server, regardless of owner. The
     * returned collection is a snapshot — concurrent modifications to
     * the underlying cache are not reflected. Intended for admin tools
     * and addons that need to iterate every claim (e.g. a global
     * tax manager or a server-wide marketplace).
     *
     * @return Collection of every ClaimProfile currently loaded
     */
    Collection<ClaimProfile> getAllClaimProfiles();

    /**
     * Look up a claim profile by its unique profile ID.
     *
     * @param profileId The claim profile's UUID
     * @return The ClaimProfile, or null if no claim with that ID exists
     */
    ClaimProfile getClaimById(UUID profileId);

    /**
     * Get a claim by its name.
     *
     * @param name The claim name
     * @return The ClaimProfile, or null if not found
     */
    ClaimProfile getClaimByName(String name);

    /**
     * Get total chunks claimed by a player.
     *
     * @param playerId The player's UUID
     * @return Total chunk count across all their claims
     */
    int getTotalChunksByOwner(UUID playerId);

    // ========== Permission Checks ==========

    /**
     * Check if a player can perform an action in a claim.
     *
     * @param profile The claim profile
     * @param playerId The player's UUID
     * @param permission The permission flag (e.g., "USE_DOORS", "BLOCK_BREAK")
     * @return true if the player has permission
     */
    boolean hasPermission(ClaimProfile profile, UUID playerId, String permission);

    /**
     * Get a player's status in a claim.
     *
     * @param profile The claim profile
     * @param playerId The player's UUID
     * @return "owner", "member", "trusted", or "visitor"
     */
    String getPlayerStatus(ClaimProfile profile, UUID playerId);

    /**
     * Check if a player is the owner of a claim.
     *
     * @param profile The claim profile
     * @param playerId The player's UUID
     * @return true if the player is the owner
     */
    boolean isOwner(ClaimProfile profile, UUID playerId);

    /**
     * Check if a player is a member of a claim.
     *
     * @param profile The claim profile
     * @param playerId The player's UUID
     * @return true if the player is a member
     */
    boolean isMember(ClaimProfile profile, UUID playerId);

    /**
     * Check if a player is trusted in a claim.
     *
     * @param profile The claim profile
     * @param playerId The player's UUID
     * @return true if the player is trusted
     */
    boolean isTrusted(ClaimProfile profile, UUID playerId);

    // ========== Warp Operations ==========

    /**
     * Get all warps in a claim.
     *
     * @param profileId The profile UUID
     * @return Map of warp name to Warp object
     */
    Map<String, Warp> getWarps(UUID profileId);

    /**
     * Get a specific warp.
     *
     * @param profileId The profile UUID
     * @param warpName The warp name
     * @return The Warp, or null if not found
     */
    Warp getWarp(UUID profileId, String warpName);

    // ========== Combat ==========

    /**
     * Check if a player is currently in combat.
     *
     * @param player The player
     * @return true if in combat
     */
    boolean isInCombat(Player player);

    // ========== Claim Limits ==========

    /**
     * Get the chunk claim limit for a player.
     *
     * @param player The player
     * @return The maximum chunks they can claim
     */
    int getClaimLimit(Player player);

    /**
     * Get the chunk claim limit for a player by UUID.
     *
     * @param playerId The player's UUID
     * @return The maximum chunks they can claim
     */
    int getClaimLimit(UUID playerId);

    /**
     * Check if a player can create a new claim.
     *
     * @param playerId The player's UUID
     * @return true if they can create a new claim
     */
    boolean canCreateClaim(UUID playerId);

    // ========== Admin Operations ==========

    /**
     * Force-claim a chunk for admin (bypasses restrictions).
     * Requires landclaim.admin permission on the player.
     *
     * @param player The player performing the action
     * @param location The location to claim
     * @return true if successful
     */
    boolean adminClaimChunk(Player player, Location location);

    /**
     * Force-unclaim a chunk (bypasses ownership check).
     * Requires landclaim.admin permission on the player.
     *
     * @param player The player performing the action
     * @param location The location to unclaim
     * @return true if successful
     */
    boolean adminUnclaimChunk(Player player, Location location);

    /**
     * Transfer ownership of a claim to a new owner. All chunks owned by
     * the current claim profile are reassigned to the new owner and the
     * persistent state is updated. The old owner loses access (except via
     * role/trust entries, which are preserved). Intended for admin
     * commands.
     *
     * <p>The {@code actor} parameter is required so the API can enforce
     * a permission check. The actor is authorized if either:</p>
     * <ul>
     *   <li>The actor has the {@code landclaim.admin} permission.</li>
     *   <li>The actor is the <em>current owner</em> of the claim being
     *       transferred.</li>
     * </ul>
     *
     * <p>A non-owner actor (e.g. a buyer in a future marketplace flow)
     * cannot invoke this method directly. Such flows should call
     * {@link org.ayosynk.landClaimPlugin.managers.ClaimManager#transferClaimProfile}
     * after performing their own server-side validation.</p>
     *
     * @param actor The player initiating the transfer; used for permission check
     * @param profileId The claim profile's UUID
     * @param newOwnerId The UUID of the player who will own the claim
     * @return true if the transfer succeeded, false if the claim doesn't exist or the actor is not authorized
     */
    boolean transferClaim(Player actor, UUID profileId, UUID newOwnerId);

    /**
     * Unclaim every chunk owned by a profile (e.g. for tax auto-unclaim
     * of an offline player). Differs from a series of adminUnclaimChunk
     * calls in that the chunks are removed in one transaction and the
     * claim profile is deleted.
     *
     * <p>Requires the {@code actor} to have {@code landclaim.admin}.
     * For programmatic tax auto-unclaim, pass
     * {@code Bukkit.getConsoleSender()} (the console has all permissions).</p>
     *
     * @param actor The player initiating the unclaim; used for permission check
     * @param profileId The claim profile to fully unclaim
     * @return number of chunks that were unclaimed (0 if profile not found or actor lacks permission)
     */
    int unclaimAll(Player actor, UUID profileId);

    /**
     * Add bonus claim blocks to a player.
     *
     * @param playerId The player's UUID
     * @param amount Amount to add (can be negative to subtract)
     * @return The new total bonus blocks
     */
    CompletableFuture<Integer> addBonusBlocks(UUID playerId, int amount);

    /**
     * Get a player's bonus claim blocks.
     *
     * @param playerId The player's UUID
     * @return The bonus block count
     */
    CompletableFuture<Integer> getBonusBlocks(UUID playerId);

    // ========== Utility ==========

    /**
     * Get the current server time in millis (for event timestamps).
     *
     * @return Current time in milliseconds
     */
    long getServerTime();
}