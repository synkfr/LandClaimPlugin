package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.UUID;

public class PistonProtectionListener implements Listener {

    private final ClaimManager claimManager;

    public PistonProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    private boolean isCrossBorderProtected(ChunkPosition pos, UUID pistonOwner) {
        if (!claimManager.isChunkClaimed(pos)) {
            return false;
        }
        UUID targetOwner = claimManager.getChunkOwner(pos);
        return pistonOwner == null || !pistonOwner.equals(targetOwner);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        if (event.getBlocks().isEmpty()) {
            ChunkPosition headTarget = new ChunkPosition(event.getBlock().getRelative(event.getDirection()));
            if (isCrossBorderProtected(headTarget, pistonOwner)) {
                event.setCancelled(true);
            }
            return;
        }

        for (Block block : event.getBlocks()) {
            if (isCrossBorderProtected(new ChunkPosition(block), pistonOwner)
                    || isCrossBorderProtected(new ChunkPosition(block.getRelative(event.getDirection())), pistonOwner)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        for (Block block : event.getBlocks()) {
            if (isCrossBorderProtected(new ChunkPosition(block), pistonOwner)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
