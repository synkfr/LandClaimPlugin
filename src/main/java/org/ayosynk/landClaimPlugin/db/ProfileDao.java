package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileDao {
    void createTables();

    CompletableFuture<Void> saveProfile(ClaimProfile profile);

    CompletableFuture<Void> deleteProfile(UUID ownerId);

    CompletableFuture<ClaimProfile> getProfile(UUID ownerId);

    CompletableFuture<List<ClaimProfile>> getAllProfiles();

    /**
     * Find the profile where a player is a member or trusted.
     * Returns null if the player is not associated with any profile.
     */
    CompletableFuture<UUID> getProfileOwnerByMember(UUID playerId);

    CompletableFuture<UUID> getProfileOwnerByTrusted(UUID playerId);
}
