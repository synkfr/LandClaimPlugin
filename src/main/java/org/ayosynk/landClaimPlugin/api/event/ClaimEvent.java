package org.ayosynk.landClaimPlugin.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;

import java.util.UUID;

/**
 * Base class for all claim-related events.
 */
public abstract class ClaimEvent extends Event {

    private final ClaimProfile profile;
    private final ChunkPosition chunk;
    private final UUID creatorId;
    private final long timestamp;

    public ClaimEvent(@NotNull ClaimProfile profile, @NotNull ChunkPosition chunk, @Nullable UUID creatorId) {
        this.profile = profile;
        this.chunk = chunk;
        this.creatorId = creatorId;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the claim profile associated with this event.
     */
    @NotNull
    public ClaimProfile getProfile() {
        return profile;
    }

    /**
     * Get the chunk position where this event occurred.
     */
    @NotNull
    public ChunkPosition getChunk() {
        return chunk;
    }

    /**
     * Get the UUID of the player who triggered this event (may be null).
     */
    @Nullable
    public UUID getCreatorId() {
        return creatorId;
    }

    /**
     * Get the timestamp when this event occurred.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the claim ID (profile ID).
     */
    @NotNull
    public UUID getClaimId() {
        return profile.getProfileId();
    }

    /**
     * Get the claim name.
     */
    @NotNull
    public String getClaimName() {
        return profile.getName();
    }

    /**
     * Get the world where this event occurred.
     */
    @NotNull
    public String getWorld() {
        return chunk.world();
    }

    /**
     * Get the chunk X coordinate.
     */
    public int getChunkX() {
        return chunk.x();
    }

    /**
     * Get the chunk Z coordinate.
     */
    public int getChunkZ() {
        return chunk.z();
    }
}