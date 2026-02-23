package org.ayosynk.landClaimPlugin.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Claim {

    private final UUID id;
    private final ChunkPosition chunkPosition;
    private UUID ownerId;

    // Sub-zone functionality
    private UUID parentClaimId;
    private String name;

    // Economy & Tracking
    private long claimedAt;
    private long expireAt;

    // Role assignments: Player UUID -> Role Name
    private final Map<UUID, String> playerRoles = new HashMap<>();

    public Claim(UUID id, ChunkPosition chunkPosition, UUID ownerId) {
        this.id = id;
        this.chunkPosition = chunkPosition;
        this.ownerId = ownerId;
        this.claimedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public ChunkPosition getChunkPosition() {
        return chunkPosition;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getParentClaimId() {
        return parentClaimId;
    }

    public void setParentClaimId(UUID parentClaimId) {
        this.parentClaimId = parentClaimId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public Map<UUID, String> getPlayerRoles() {
        return playerRoles;
    }

    public void setPlayerRole(UUID playerId, String roleName) {
        if (roleName == null) {
            playerRoles.remove(playerId);
        } else {
            playerRoles.put(playerId, roleName);
        }
    }

    public String getPlayerRole(UUID playerId) {
        return playerRoles.get(playerId);
    }
}
