package org.ayosynk.landClaimPlugin.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Claim {

    private final UUID id;
    private final Set<ChunkPosition> chunks = new HashSet<>();
    private UUID ownerId;

    // Sub-zone functionality
    private UUID parentClaimId;
    private String name;

    // Economy & Tracking
    private long claimedAt;
    private long expireAt;

    private final Map<UUID, String> playerRoles = new HashMap<>();

    // Per-claim visitor permissions (e.g., "BLOCK_BREAK", "USE_DOORS")
    // applies to anyone without an explicit role
    private final Set<String> visitorFlags = new HashSet<>();

    public Claim(UUID id, UUID ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        this.claimedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public Set<ChunkPosition> getChunks() {
        return chunks;
    }

    public void addChunk(ChunkPosition position) {
        this.chunks.add(position);
    }

    public void removeChunk(ChunkPosition position) {
        this.chunks.remove(position);
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

    public Set<String> getVisitorFlags() {
        return visitorFlags;
    }

    public boolean hasVisitorFlag(String flag) {
        return visitorFlags.contains(flag.toUpperCase());
    }

    public void addVisitorFlag(String flag) {
        visitorFlags.add(flag.toUpperCase());
    }

    public void removeVisitorFlag(String flag) {
        visitorFlags.remove(flag.toUpperCase());
    }
}
