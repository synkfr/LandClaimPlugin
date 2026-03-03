package org.ayosynk.landClaimPlugin.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheManager {

    private final Cache<UUID, Claim> claimCache;
    private final Cache<UUID, ClaimPlayer> playerCache;

    public CacheManager() {
        this.claimCache = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(10000) // Support large servers
                .build();

        this.playerCache = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(5000)
                .build();
    }

    public Cache<UUID, Claim> getClaimCache() {
        return claimCache;
    }

    public Cache<UUID, ClaimPlayer> getPlayerCache() {
        return playerCache;
    }

}
