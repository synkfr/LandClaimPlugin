package org.ayosynk.landClaimPlugin.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;

import java.util.UUID;

/**
 * Called when a claim is deleted (abandoned or admin unclaimed).
 *
 * This event is fired AFTER the claim has been deleted from the database.
 */
public class ClaimDeleteEvent extends ClaimEvent {

    private static final HandlerList handlers = new HandlerList();
    private final DeleteReason reason;

    public enum DeleteReason {
        /** Player used /claim abandon command */
        PLAYER_ABANDON,
        /** Admin force-unclaimed the chunk */
        ADMIN_UNCLAIM,
        /** Ownership transfer (old claim deleted) */
        OWNERSHIP_TRANSFER,
        /** Plugin or migration operation */
        SYSTEM
    }

    public ClaimDeleteEvent(@NotNull ClaimProfile profile, @NotNull ChunkPosition chunk,
                            @Nullable UUID deleterId, @NotNull DeleteReason reason) {
        super(profile, chunk, deleterId);
        this.reason = reason;
    }

    /**
     * Get the reason why the claim was deleted.
     */
    @NotNull
    public DeleteReason getReason() {
        return reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}