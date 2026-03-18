package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for {@link ClaimProfile} persistence.
 * <p>
 * All mutation methods return {@link CompletableFuture} for async execution.
 * Implementations must handle both SQLite and MySQL dialects.
 */
public interface ProfileDao {

    /**
     * Create or migrate all required database tables for profile storage.
     */
    void createTables();

    /**
     * Persist a complete profile (including chunks, roles, members, trusted, allies, visitor flags).
     * Uses a clear-and-reinsert strategy within a transaction.
     *
     * @param profile the profile to save
     * @return future that completes when the save is done
     */
    CompletableFuture<Void> saveProfile(ClaimProfile profile);

    /**
     * Delete a profile and all associated data (chunks, roles, members, etc.).
     *
     * @param ownerId the profile owner's UUID
     * @return future that completes when deletion is done
     */
    CompletableFuture<Void> deleteProfile(UUID ownerId);

    /**
     * Load a single profile by owner UUID, including all nested data.
     *
     * @param ownerId the profile owner's UUID
     * @return future yielding the profile, or null if not found
     */
    CompletableFuture<ClaimProfile> getProfile(UUID ownerId);

    /**
     * Load all profiles from the database.
     *
     * @return future yielding the list of all profiles
     */
    CompletableFuture<List<ClaimProfile>> getAllProfiles();

    /**
     * Find the profile owner UUID where a player is assigned as a member.
     *
     * @param playerId the member's UUID
     * @return future yielding the profile owner UUID, or null if not a member anywhere
     */
    CompletableFuture<UUID> getProfileOwnerByMember(UUID playerId);

    /**
     * Find the profile owner UUID where a player is trusted.
     *
     * @param playerId the trusted player's UUID
     * @return future yielding the profile owner UUID, or null if not trusted anywhere
     */
    CompletableFuture<UUID> getProfileOwnerByTrusted(UUID playerId);
}
