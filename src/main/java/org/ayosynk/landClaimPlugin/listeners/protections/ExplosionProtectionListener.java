package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionProtectionListener implements Listener {

    private final ClaimManager claimManager;

    public ExplosionProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Location explosionLoc = event.getLocation();
        ChunkPosition explosionChunk = new ChunkPosition(explosionLoc);

        // Quick bounding box check: if the explosion chunk or any adjacent chunk is
        // claimed
        boolean nearClaim = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPosition checkPos = new ChunkPosition(
                        explosionLoc.getWorld().getName(),
                        explosionChunk.x() + dx,
                        explosionChunk.z() + dz);
                if (claimManager.isChunkClaimed(checkPos)) {
                    nearClaim = true;
                    break;
                }
            }
            if (nearClaim)
                break;
        }

        if (!nearClaim)
            return;

        // If near a claim, strictly filter the block destruction list
        event.blockList().removeIf(b -> claimManager.isChunkClaimed(new ChunkPosition(b)));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Location explosionLoc = event.getBlock().getLocation();
        ChunkPosition explosionChunk = new ChunkPosition(explosionLoc);

        boolean nearClaim = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPosition checkPos = new ChunkPosition(
                        explosionLoc.getWorld().getName(),
                        explosionChunk.x() + dx,
                        explosionChunk.z() + dz);
                if (claimManager.isChunkClaimed(checkPos)) {
                    nearClaim = true;
                    break;
                }
            }
            if (nearClaim)
                break;
        }

        if (!nearClaim)
            return;

        event.blockList().removeIf(b -> claimManager.isChunkClaimed(new ChunkPosition(b)));
    }
}
