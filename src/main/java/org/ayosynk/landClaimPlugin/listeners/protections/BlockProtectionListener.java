package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public BlockProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    private boolean checkPermission(Player player, Block block, org.bukkit.event.Cancellable event, String permission) {
        if (player.hasPermission("landclaim.admin"))
            return true;

        ChunkPosition pos = new ChunkPosition(block);
        if (claimManager.isChunkClaimed(pos)) {
            Claim claim = claimManager.getClaimAt(pos);
            if (player.getUniqueId().equals(claim.getOwnerId()))
                return true; // Owner

            if (claim.hasVisitorFlag(permission)) {
                return true; // Visitor flag is allowed for this claim
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied"));
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        checkPermission(event.getPlayer(), event.getBlock(), event, "BLOCK_BREAK");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        checkPermission(event.getPlayer(), event.getBlock(), event, "BLOCK_PLACE");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null)
            return;
        checkPermission(event.getPlayer(), event.getBlock(), event, "BLOCK_IGNITE");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        if (!checkPermission(event.getPlayer(), block, event, "USE_BUCKETS")) {
            event.getPlayer().sendMessage(configManager.getMessage("bucket-denied"));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();
        if (!checkPermission(event.getPlayer(), block, event, "USE_BUCKETS")) {
            event.getPlayer().sendMessage(configManager.getMessage("bucket-denied"));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        checkPermission(event.getPlayer(), event.getBlock(), event, "MODIFY_SIGNS");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractForFertilizer(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getItem() == null)
            return;

        if (event.getItem().getType() == Material.BONE_MEAL) {
            checkPermission(event.getPlayer(), event.getClickedBlock(), event, "USE_FERTILIZER");
        }
    }
}
