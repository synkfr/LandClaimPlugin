package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Claim;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for legacy {@link Claim} persistence.
 *
 * @deprecated Use {@link ProfileDao} for the V2 profile-based system.
 */
@Deprecated
public interface ClaimDao {

    /** Create or migrate claim-related database tables. */
    void createTables();

    /** Persist a claim. */
    CompletableFuture<Void> saveClaim(Claim claim);

    /** Delete a claim by ID. */
    CompletableFuture<Void> deleteClaim(UUID claimId);

    /** Load a single claim by ID. */
    CompletableFuture<Claim> getClaim(UUID claimId);

    /** Load all claims owned by a player. */
    CompletableFuture<List<Claim>> getClaimsByOwner(UUID ownerId);

    /** Load all claims. */
    CompletableFuture<List<Claim>> getAllClaims();
}
