package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.BlockPermissionResolver;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;
import org.ayosynk.landClaimPlugin.managers.WildernessProtection;

import org.ayosynk.landClaimPlugin.models.BlockPermission;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public InteractProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    private boolean checkPermission(Player player, Block block, org.bukkit.event.Cancellable event, String permission) {
        if (player.hasPermission("landclaim.admin"))
            return true;

        ChunkPosition pos = new ChunkPosition(block);
        ClaimProfile profile = claimManager.getProfileAt(pos);
        if (profile != null) {
            if (PermissionResolver.hasPermission(profile, player.getUniqueId(), permission)) {
                return true;
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied"));
            return false;
        }

        // No claim at this chunk — fall through to wilderness protection.
        if (WildernessProtection.isDenied(block.getWorld(), player, permission)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("wilderness-protected"));
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        // 1. Handle physical interaction (trampling crops)
        if (event.getAction() == org.bukkit.event.block.Action.PHYSICAL) {
            if (block != null && block.getType() == org.bukkit.Material.FARMLAND) {
                checkPermission(event.getPlayer(), block, event, "TRAMPLE_CROPS");
                return;
            }
        }

        if (block == null)
            return;

        // 2. Handle standard block interactions (doors, containers, workstations, etc.)
        BlockPermission requiredPermission = BlockPermissionResolver.resolve(block);
        if (requiredPermission != null) {
            checkPermission(event.getPlayer(), block, event, requiredPermission.getFlag());
        }
    }
}
