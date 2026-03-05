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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        for (Block block : event.getBlocks()) {
            Block pushedTo = block.getRelative(event.getDirection());
            ChunkPosition targetChunk = new ChunkPosition(pushedTo);

            // If pushing into a claim
            if (claimManager.isChunkClaimed(targetChunk)) {
                UUID targetOwner = claimManager.getChunkOwner(targetChunk);
                // Allow only if pushed from the SAME claim
                if (pistonOwner == null || !pistonOwner.equals(targetOwner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        for (Block block : event.getBlocks()) {
            ChunkPosition targetChunk = new ChunkPosition(block);

            // If pulling from a claim
            if (claimManager.isChunkClaimed(targetChunk)) {
                UUID targetOwner = claimManager.getChunkOwner(targetChunk);
                // Allow only if pulled from the SAME claim
                if (pistonOwner == null || !pistonOwner.equals(targetOwner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
