package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.ClaimPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    void createTables();

    CompletableFuture<Void> savePlayer(ClaimPlayer player);

    CompletableFuture<ClaimPlayer> getPlayer(UUID playerId);
}
