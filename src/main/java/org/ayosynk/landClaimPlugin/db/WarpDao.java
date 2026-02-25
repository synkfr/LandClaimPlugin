package org.ayosynk.landClaimPlugin.db;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarpDao {

    void createTables();

    CompletableFuture<Map<UUID, Map<String, Location>>> loadAllWarps();

    void saveWarp(UUID playerId, String name, Location location);

    void deleteWarp(UUID playerId, String name);
}
