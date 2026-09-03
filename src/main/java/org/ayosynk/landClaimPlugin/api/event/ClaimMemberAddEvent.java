package org.ayosynk.landClaimPlugin.api.event;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimMemberAddEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ClaimProfile profile;
    private final UUID memberId;
    private final Player inviter;
    private boolean cancelled = false;

    public ClaimMemberAddEvent(@NotNull ClaimProfile profile, @NotNull UUID memberId, @Nullable Player inviter) {
        this.profile = profile;
        this.memberId = memberId;
        this.inviter = inviter;
    }

    @NotNull
    public ClaimProfile getProfile() {
        return profile;
    }

    @NotNull
    public UUID getMemberId() {
        return memberId;
    }

    @Nullable
    public Player getInviter() {
        return inviter;
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
