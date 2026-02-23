package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Claim;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClaimDao {
    void createTables();

    CompletableFuture<Void> saveClaim(Claim claim);

    CompletableFuture<Void> deleteClaim(UUID claimId);

    CompletableFuture<Claim> getClaim(UUID claimId);

    CompletableFuture<List<Claim>> getClaimsByOwner(UUID ownerId);

    CompletableFuture<List<Claim>> getAllClaims();
}
