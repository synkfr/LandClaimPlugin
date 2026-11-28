package org.ayosynk.landClaimPlugin.listeners.protections;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;
import org.ayosynk.landClaimPlugin.managers.WildernessProtection;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.EnumSet;
import java.util.Set;

public class EntityProtectionListener implements Listener {

    private static final Set<CreatureSpawnEvent.SpawnReason> BLOCKED_SPAWN_REASONS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.NATURAL,
            CreatureSpawnEvent.SpawnReason.JOCKEY,
            CreatureSpawnEvent.SpawnReason.MOUNT,
            CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
            CreatureSpawnEvent.SpawnReason.NETHER_PORTAL,
            CreatureSpawnEvent.SpawnReason.LIGHTNING,
            CreatureSpawnEvent.SpawnReason.PATROL,
            CreatureSpawnEvent.SpawnReason.RAID,
            CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER,
            CreatureSpawnEvent.SpawnReason.TRAP,
            CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK);

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
        World world = Bukkit.getWorld(pos.world());
        if (WildernessProtection.isDenied(world, player, permission)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("wilderness-protected"));
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Player damager = getDamager(event.getCause(), event.getDamager());

        if (damager == null)
            return;
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof Animals || target instanceof Tameable || target instanceof Fish
                || target instanceof WaterMob) {
            checkPermission(damager, pos, event, "DAMAGE_ANIMALS");
        } else if (target instanceof Enemy || target instanceof Monster || target instanceof Slime || target instanceof Ghast || target instanceof Phantom) {
            checkPermission(damager, pos, event, "DAMAGE_MONSTERS");
        } else if (target instanceof ArmorStand) {
            checkPermission(damager, pos, event, "MODIFY_ARMOR_STANDS");
        } else if (target instanceof Hanging) { // Item frames, paintings, leashes
            checkPermission(damager, pos, event, "MODIFY_ITEM_FRAMES");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            ChunkPosition pos = new ChunkPosition(event.getEntity().getLocation());
            checkPermission(player, pos, event, "MODIFY_ITEM_FRAMES");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover != null) {
            Player damager = getDamager(null, remover);
            ChunkPosition pos = new ChunkPosition(event.getEntity().getLocation());
            if (damager != null) {
                checkPermission(damager, pos, event, "MODIFY_ITEM_FRAMES");
            } else {
                ClaimProfile profile = claimManager.getProfileAt(pos);
                boolean isTnt = remover.getType() == EntityType.TNT || remover.getType() == EntityType.TNT_MINECART;
                if (profile != null && !(isTnt && profile.isTntEnabled())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity target = event.getRightClicked();
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof Villager || target instanceof WanderingTrader) {
            checkPermission(player, pos, event, "TRADE_VILLAGERS");
        } else if (target instanceof Sheep && player.getInventory().getItemInMainHand().getType() == Material.SHEARS) {
            checkPermission(player, pos, event, "SHEAR_ENTITIES");
        } else if (target instanceof Hanging) {
            checkPermission(player, pos, event, "MODIFY_ITEM_FRAMES");
        } else if (target instanceof Animals animal) {
            // 1. Always allow if the player is the owner of a tameable animal
            if (animal instanceof Tameable tameable && player.getUniqueId().equals(tameable.getOwnerUniqueId())) {
                return;
            }

            // 2. Determine what action they are likely taking
            org.bukkit.inventory.ItemStack handItem = player.getInventory().getItemInMainHand();
            
            if (handItem.getType() == Material.LEAD) {
                return;
            }

            if ((animal instanceof Steerable || animal instanceof AbstractHorse)
                    && (handItem.getType().isAir() || handItem.getType() == Material.SADDLE)) {
                checkPermission(player, pos, event, "MOUNT_ANIMALS");
                return;
            }

            if (!handItem.getType().isAir()) {
                ClaimProfile profile = claimManager.getProfileAt(pos);
                if (profile != null) {
                    if (!player.hasPermission("landclaim.admin")
                            && !PermissionResolver.hasPermission(profile, player.getUniqueId(), "BREED_ANIMALS")
                            && !PermissionResolver.hasPermission(profile, player.getUniqueId(), "FEED_ANIMALS")) {
                        event.setCancelled(true);
                    }
                } else if (WildernessProtection.isDenied(Bukkit.getWorld(pos.world()), player, "BREED_ANIMALS")
                        && WildernessProtection.isDenied(Bukkit.getWorld(pos.world()), player, "FEED_ANIMALS")) {
                    event.setCancelled(true);
                    player.sendMessage(configManager.getMessage("wilderness-protected"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLeash(org.bukkit.event.entity.PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(event.getEntity().getLocation());
        checkPermission(player, pos, event, "LEASH_ENTITIES");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerUnleash(org.bukkit.event.player.PlayerUnleashEntityEvent event) {
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(event.getEntity().getLocation());
        checkPermission(player, pos, event, "LEASH_ENTITIES");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity target = event.getRightClicked();
        Player player = event.getPlayer();
        ChunkPosition pos = new ChunkPosition(target.getLocation());

        if (target instanceof ArmorStand) {
            checkPermission(player, pos, event, "MODIFY_ARMOR_STANDS");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!BLOCKED_SPAWN_REASONS.contains(event.getSpawnReason())) {
            return;
        }
        ClaimProfile profile = claimManager.getProfileAt(new ChunkPosition(event.getLocation()));
        if (profile != null && !profile.isMobSpawningEnabled()) {
            event.setCancelled(true);
        }
    }
}
