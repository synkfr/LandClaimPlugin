package org.ayosynk.landClaimPlugin.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;

import java.util.UUID;

/**
 * Called when a player leaves a claimed chunk.
 *
 * This event fires when a player moves from a claimed chunk to wilderness
 * or to another claim.
 *
 * Note: This is NOT called on every tick - only when the player crosses a chunk boundary.
 */
public class PlayerLeaveClaimEvent extends ClaimEvent {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final Player player;

    /**
     * The claim they're going TO (null if going to wilderness).
     */
    @Nullable
    private final ClaimProfile toClaim;

    public PlayerLeaveClaimEvent(@NotNull Player player, @NotNull ClaimProfile leftClaim,
                                  @NotNull ChunkPosition chunk, @Nullable ClaimProfile toClaim) {
        super(leftClaim, chunk, player.getUniqueId());
        this.player = player;
        this.toClaim = toClaim;
    }

    /**
     * Get the player who left the claim.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the player's UUID (convenience method).
     */
    @NotNull
    public UUID getPlayerId() {
        return player.getUniqueId();
    }

    /**
     * Get the claim the player is going TO (null if going to wilderness).
     */
    @Nullable
    public ClaimProfile getToClaim() {
        return toClaim;
    }

    /**
     * Check if the player is going to wilderness (no destination claim).
     */
    public boolean isToWilderness() {
        return toClaim == null;
    }

    /**
     * Check if the player is transitioning between two different claims.
     */
    public boolean isClaimToClaim() {
        return toClaim != null && !toClaim.getProfileId().equals(getProfile().getProfileId());
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