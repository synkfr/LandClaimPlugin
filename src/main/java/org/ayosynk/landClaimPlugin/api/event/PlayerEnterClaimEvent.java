package org.ayosynk.landClaimPlugin.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;

import java.util.UUID;

/**
 * Called when a player enters a claimed chunk.
 *
 * This event fires when a player moves from wilderness into a claimed chunk,
 * or from one claim into another claim.
 *
 * Note: This is NOT called on every tick - only when the player crosses a chunk boundary.
 */
public class PlayerEnterClaimEvent extends ClaimEvent {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final Player player;

    /**
     * The claim they came FROM (null if coming from wilderness).
     */
    @Nullable
    private final ClaimProfile fromClaim;

    public PlayerEnterClaimEvent(@NotNull Player player, @NotNull ClaimProfile enteredClaim,
                                  @NotNull ChunkPosition chunk, @Nullable ClaimProfile fromClaim) {
        super(enteredClaim, chunk, player.getUniqueId());
        this.player = player;
        this.fromClaim = fromClaim;
    }

    /**
     * Get the player who entered the claim.
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
     * Get the claim the player came FROM (null if coming from wilderness).
     */
    @Nullable
    public ClaimProfile getFromClaim() {
        return fromClaim;
    }

    /**
     * Check if the player came from wilderness (no previous claim).
     */
    public boolean isFromWilderness() {
        return fromClaim == null;
    }

    /**
     * Check if the player is transitioning between two different claims.
     */
    public boolean isClaimToClaim() {
        return fromClaim != null && !fromClaim.getProfileId().equals(getProfile().getProfileId());
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