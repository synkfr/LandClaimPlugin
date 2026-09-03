package org.ayosynk.landClaimPlugin.api.event;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClaimTransferEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final ClaimProfile profile;
    private final UUID oldOwnerId;
    private final UUID newOwnerId;

    public ClaimTransferEvent(@NotNull ClaimProfile profile, @NotNull UUID oldOwnerId, @NotNull UUID newOwnerId) {
        this.profile = profile;
        this.oldOwnerId = oldOwnerId;
        this.newOwnerId = newOwnerId;
    }

    @NotNull
    public ClaimProfile getProfile() {
        return profile;
    }

    @NotNull
    public UUID getOldOwnerId() {
        return oldOwnerId;
    }

    @NotNull
    public UUID getNewOwnerId() {
        return newOwnerId;
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
