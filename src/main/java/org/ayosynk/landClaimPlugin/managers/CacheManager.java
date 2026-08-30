package org.ayosynk.landClaimPlugin.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;

import java.util.UUID;

/**
 * Manages Caffeine in-memory caches for players and claim profiles.
 * <p>
 * Caches reduce database reads by keeping frequently accessed data in memory.
 * When Redis is enabled, cache invalidation is propagated across servers.
 *
 * <table>
 *   <tr><th>Cache</th><th>Key</th><th>Value</th><th>Expiry</th><th>Max Size</th></tr>
 *   <tr><td>profileCache</td><td>UUID (owner/profile)</td><td>ClaimProfile</td><td>None</td><td>10,000</td></tr>
 *   <tr><td>playerCache</td><td>UUID (player)</td><td>ClaimPlayer</td><td>None</td><td>5,000</td></tr>
 * </table>
 */
public class CacheManager {

    private final Cache<UUID, ClaimPlayer> playerCache;
    private final Cache<UUID, ClaimProfile> profileCache;

    public CacheManager() {
        this.playerCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .recordStats()
                .build();

        this.profileCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    public Cache<UUID, ClaimPlayer> getPlayerCache() {
        return playerCache;
    }

    public Cache<UUID, ClaimProfile> getProfileCache() {
        return profileCache;
    }

    /**
     * Log cache statistics for debugging and monitoring.
     */
    public void logCacheStats() {
        CacheStats profileStats = profileCache.stats();
        CacheStats playerStats = playerCache.stats();

        LandClaimPlugin plugin = LandClaimPlugin.getInstance();
        if (plugin == null) return;

        plugin.getLogger().info("=== Cache Statistics ===");
        plugin.getLogger().info("Profile Cache: hitRate=" + String.format("%.2f", profileStats.hitRate() * 100)
                + "%, hits=" + profileStats.hitCount() + ", misses=" + profileStats.missCount()
                + ", size=" + profileCache.estimatedSize());
        plugin.getLogger().info("Player Cache: hitRate=" + String.format("%.2f", playerStats.hitRate() * 100)
                + "%, hits=" + playerStats.hitCount() + ", misses=" + playerStats.missCount()
                + ", size=" + playerCache.estimatedSize());
    }

}
