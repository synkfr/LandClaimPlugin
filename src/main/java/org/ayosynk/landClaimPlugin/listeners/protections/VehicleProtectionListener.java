package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.UUID;

public class VehicleProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public VehicleProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
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
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            ChunkPosition pos = new ChunkPosition(event.getVehicle().getLocation());
            checkPermission(player, pos, event, "DESTROY_VEHICLES");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            ChunkPosition pos = new ChunkPosition(event.getVehicle().getLocation());
            checkPermission(player, pos, event, "DESTROY_VEHICLES");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerEnterVehicle(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Vehicle) {
            Player player = event.getPlayer();
            ChunkPosition pos = new ChunkPosition(event.getRightClicked().getLocation());
            checkPermission(player, pos, event, "RIDE_VEHICLES");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        // Prevent hopper minecarts from entering claims they didn't originate from
        if (event.getVehicle() instanceof HopperMinecart) {
            ChunkPosition fromChunk = new ChunkPosition(event.getFrom());
            ChunkPosition toChunk = new ChunkPosition(event.getTo());

            if (!fromChunk.equals(toChunk) && claimManager.isChunkClaimed(toChunk)) {
                UUID toOwner = claimManager.getChunkOwner(toChunk);
                UUID fromOwner = claimManager.isChunkClaimed(fromChunk) ? claimManager.getChunkOwner(fromChunk) : null;

                if (fromOwner == null || !fromOwner.equals(toOwner)) {
                    event.getVehicle().setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    event.getVehicle().teleport(event.getFrom());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPlaceVehicle(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null)
            return;

        String materialName = event.getItem().getType().name();
        if (materialName.endsWith("_BOAT") || materialName.endsWith("_MINECART") || materialName.equals("MINECART")) {
            Player player = event.getPlayer();
            // Check the block adjacent to the clicked block face (where the vehicle would
            // spawn)
            org.bukkit.block.Block placedBlock = event.getClickedBlock().getRelative(event.getBlockFace());
            ChunkPosition pos = new ChunkPosition(placedBlock.getLocation());
            checkPermission(player, pos, event, "PLACE_VEHICLES");
        }
    }
}
