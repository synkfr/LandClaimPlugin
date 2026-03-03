package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public EntityProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    private boolean checkPermission(Player player, ChunkPosition pos, org.bukkit.event.Cancellable event,
            String permission) {
        if (player.hasPermission("landclaim.admin"))
            return true;

        if (claimManager.isChunkClaimed(pos)) {
            Claim claim = claimManager.getClaimAt(pos);
            if (player.getUniqueId().equals(claim.getOwnerId()))
                return true;

            if (claim.hasVisitorFlag(permission)) {
                return true;
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied"));
            return false;
        }

        return true;
    }

    private Player getDamager(org.bukkit.event.entity.EntityDamageEvent.DamageCause cause, Entity damagerEntity) {
        if (damagerEntity instanceof Player) {
            return (Player) damagerEntity;
        } else if (damagerEntity instanceof Projectile) {
            Projectile projectile = (Projectile) damagerEntity;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Player damager = getDamager(event.getCause(), event.getDamager());

        if (damager == null)
            return;
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof Animals || target instanceof Tameable || target instanceof Fish
                || target instanceof WaterMob) {
            checkPermission(damager, pos, event, "DAMAGE_ANIMALS");
        } else if (target instanceof Monster || target instanceof Slime || target instanceof Flying) {
            checkPermission(damager, pos, event, "DAMAGE_MONSTERS");
        } else if (target instanceof ArmorStand) {
            checkPermission(damager, pos, event, "MODIFY_ARMOR_STANDS");
        } else if (target instanceof Hanging) { // Item frames, paintings, leashes
            checkPermission(damager, pos, event, "MODIFY_ITEM_FRAMES");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity target = event.getRightClicked();
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof Villager || target instanceof WanderingTrader) {
            checkPermission(player, pos, event, "TRADE_VILLAGERS");
        } else if (target instanceof Sheep && player.getInventory().getItemInMainHand().getType() == Material.SHEARS) {
            checkPermission(player, pos, event, "SHEAR_ENTITIES");
        } else if (target instanceof Animals) {
            // Very simplified approach: checking item in hand for breeding/feeding is
            // complex across 1.21 mobs,
            // so we classify general "interaction" with animals as
            // FEED_ANIMALS/BREED_ANIMALS contextually.
            if (!checkPermission(player, pos, event, "BREED_ANIMALS")) {
                if (!checkPermission(player, pos, event, "FEED_ANIMALS")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity target = event.getRightClicked();
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof ArmorStand) {
            checkPermission(player, pos, event, "MODIFY_ARMOR_STANDS");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMobGriefing(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman || event.getEntity() instanceof Ravager
                || event.getEntity() instanceof Wither || event.getEntity() instanceof Silverfish) {

            ChunkPosition pos = new ChunkPosition(event.getBlock());
            // Global mob griefing within claims
            if (claimManager.isChunkClaimed(pos)) {
                // We default to cancelling non-player entity changes in claims
                event.setCancelled(true);
            }
        }
    }
}
