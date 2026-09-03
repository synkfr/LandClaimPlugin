package org.ayosynk.landClaimPlugin.api.event;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpPrivacyChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ClaimProfile profile;
    private final Warp warp;
    private final Player player;
    private final boolean newIsPublic;
    private boolean cancelled = false;

    public WarpPrivacyChangeEvent(@NotNull ClaimProfile profile, @NotNull Warp warp, @Nullable Player player, boolean newIsPublic) {
        this.profile = profile;
        this.warp = warp;
        this.player = player;
        this.newIsPublic = newIsPublic;
    }

    @NotNull
    public ClaimProfile getProfile() {
        return profile;
    }

    @NotNull
    public Warp getWarp() {
        return warp;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public boolean isNewIsPublic() {
        return newIsPublic;
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
