package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class ExplosionProtectionListener implements Listener {

    private final ClaimManager claimManager;

    public ExplosionProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        boolean isTnt = type == EntityType.TNT || type == EntityType.TNT_MINECART;
        filterProtectedBlocks(event.getLocation(), event.blockList(), isTnt);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        filterProtectedBlocks(event.getBlock().getLocation(), event.blockList(), false);
    }

    private void filterProtectedBlocks(Location explosionLoc, List<Block> blockList, boolean allowTnt) {
        if (explosionLoc.getWorld() == null || blockList.isEmpty()) {
            return;
        }
        ChunkPosition explosionChunk = new ChunkPosition(explosionLoc);
        String worldName = explosionLoc.getWorld().getName();

        boolean nearClaim = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (claimManager.isChunkClaimed(new ChunkPosition(worldName, explosionChunk.x() + dx, explosionChunk.z() + dz))) {
                    nearClaim = true;
                    break;
                }
            }
            if (nearClaim) {
                break;
            }
        }

        if (!nearClaim) {
            return;
        }

        blockList.removeIf(b -> {
            ClaimProfile profile = claimManager.getProfileAt(new ChunkPosition(b));
            return profile != null && !(allowTnt && profile.isTntEnabled());
        });
    }
}
