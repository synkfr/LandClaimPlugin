package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Warp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarpDao {

    void createTables();

    CompletableFuture<Map<UUID, Map<String, Warp>>> loadAllWarps();

    void saveWarp(UUID ownerId, Warp warp);

    void deleteWarp(UUID ownerId, String name);
}
