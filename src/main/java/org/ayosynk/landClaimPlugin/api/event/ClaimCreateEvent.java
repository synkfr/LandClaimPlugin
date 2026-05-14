package org.ayosynk.landClaimPlugin.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;

import java.util.UUID;

/**
 * Called when a new claim is created.
 *
 * This event is fired AFTER the claim has been created in the database.
 */
public class ClaimCreateEvent extends ClaimEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public ClaimCreateEvent(@NotNull ClaimProfile profile, @NotNull ChunkPosition chunk, @Nullable UUID creatorId) {
        super(profile, chunk, creatorId);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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