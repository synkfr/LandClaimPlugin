package org.ayosynk.landClaimPlugin.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages Caffeine in-memory caches for claims, players, and profiles.
 * <p>
 * Caches reduce database reads by keeping frequently accessed data in memory.
 * When Redis is enabled, cache invalidation is propagated across servers.
 *
 * <table>
 *   <tr><th>Cache</th><th>Key</th><th>Value</th><th>Expiry</th><th>Max Size</th></tr>
 *   <tr><td>profileCache</td><td>UUID (owner)</td><td>ClaimProfile</td><td>None</td><td>10,000</td></tr>
 *   <tr><td>claimCache</td><td>UUID (claim id)</td><td>Claim</td><td>30 min access</td><td>10,000</td></tr>
 *   <tr><td>playerCache</td><td>UUID (player)</td><td>ClaimPlayer</td><td>None</td><td>5,000</td></tr>
 * </table>
 */
public class CacheManager {

    private final Cache<UUID, Claim> claimCache;
    private final Cache<UUID, ClaimPlayer> playerCache;
    private final Cache<UUID, ClaimProfile> profileCache;

    public CacheManager() {
        this.claimCache = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        this.playerCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .build();

        this.profileCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }

    public Cache<UUID, Claim> getClaimCache() {
        return claimCache;
    }

    public Cache<UUID, ClaimPlayer> getPlayerCache() {
        return playerCache;
    }

    public Cache<UUID, ClaimProfile> getProfileCache() {
        return profileCache;
    }

}
