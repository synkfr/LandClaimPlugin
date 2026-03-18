package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.ClaimPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for {@link ClaimPlayer} persistence.
 * Stores per-player preferences like auto-claim state and visualization mode.
 */
public interface PlayerDao {

    /** Create or migrate player data tables. */
    void createTables();

    /** Persist player preferences. */
    CompletableFuture<Void> savePlayer(ClaimPlayer player);

    /** Load player preferences by UUID. Returns null if not found. */
    CompletableFuture<ClaimPlayer> getPlayer(UUID playerId);
}
