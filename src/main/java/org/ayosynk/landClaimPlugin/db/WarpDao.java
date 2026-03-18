package org.ayosynk.landClaimPlugin.db;

import org.ayosynk.landClaimPlugin.models.Warp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Data access object for {@link Warp} persistence.
 * Warps are stored per-profile owner with case-insensitive names.
 */
public interface WarpDao {

    /** Create or migrate warp tables. */
    void createTables();

    /**
     * Load all warps for all players.
     *
     * @return future yielding owner UUID → (warp name → Warp) mapping
     */
    CompletableFuture<Map<UUID, Map<String, Warp>>> loadAllWarps();

    /**
     * Create or update a warp. Writes synchronously to the database.
     *
     * @param ownerId the profile owner's UUID
     * @param warp    the warp to persist
     */
    void saveWarp(UUID ownerId, Warp warp);

    /**
     * Delete a warp by name. Writes synchronously to the database.
     *
     * @param ownerId the profile owner's UUID
     * @param name    the warp name (case-insensitive)
     */
    void deleteWarp(UUID ownerId, String name);
}
