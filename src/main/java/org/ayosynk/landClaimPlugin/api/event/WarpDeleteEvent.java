package org.ayosynk.landClaimPlugin.api.event;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpDeleteEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final ClaimProfile profile;
    private final Warp warp;
    private final Player player;

    public WarpDeleteEvent(@NotNull ClaimProfile profile, @NotNull Warp warp, @Nullable Player player) {
        this.profile = profile;
        this.warp = warp;
        this.player = player;
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
