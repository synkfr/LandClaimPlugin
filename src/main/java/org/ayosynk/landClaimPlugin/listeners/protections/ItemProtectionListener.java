package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ItemProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public ItemProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    private boolean checkPermission(Player player, ChunkPosition pos, org.bukkit.event.Cancellable event,
            String permission) {
        if (player.hasPermission("landclaim.admin"))
            return true;

        ClaimProfile profile = claimManager.getProfileAt(pos);
        if (profile != null) {
            if (PermissionResolver.hasPermission(profile, player.getUniqueId(), permission)) {
                return true;
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied"));
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(event.getItemDrop().getLocation());
        checkPermission(player, pos, event, "DROP_ITEMS");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ChunkPosition pos = new ChunkPosition(event.getItem().getLocation());
            checkPermission(player, pos, event, "PICKUP_ITEMS");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (event.getEntity() instanceof org.bukkit.entity.EnderPearl) {
                ChunkPosition pos = new ChunkPosition(player.getLocation());
                checkPermission(player, pos, event, "USE_ENDER_PEARLS");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.CHORUS_FRUIT) {
            ChunkPosition pos = new ChunkPosition(player.getLocation());
            checkPermission(player, pos, event, "USE_CHORUS_FRUIT");
        }
    }
}
