package org.ayosynk.landClaimPlugin.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EventListener implements Listener {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final Map<UUID, ChunkPosition> lastChunkMap = new HashMap<>();
    private final Map<UUID, String> lastActionBarMap = new HashMap<>();
    private final Map<UUID, Boolean> lastClaimStatusMap = new HashMap<>();

    private static final Set<Material> CONTAINER_BLOCKS = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER,
            Material.DROPPER, Material.DISPENSER, Material.BREWING_STAND,
            Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX));

    private static final Set<Material> INTERACTABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR,
            Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.CHERRY_DOOR, Material.PALE_OAK_DOOR, Material.BAMBOO_DOOR,
            Material.CRIMSON_DOOR, Material.WARPED_DOOR,
            Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR,
            Material.PALE_OAK_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR, Material.BAMBOO_BUTTON, Material.CHERRY_BUTTON,
            Material.PALE_OAK_BUTTON,
            Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR,
            Material.IRON_DOOR, Material.IRON_TRAPDOOR, Material.LEVER, Material.STONE_BUTTON, Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.GRINDSTONE,
            Material.SMITHING_TABLE, Material.LOOM, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
            Material.STONECUTTER, Material.BELL, Material.COMPOSTER));

    public EventListener(LandClaimPlugin plugin, ClaimManager claimManager,
            TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;

        startActionBarTask();
    }

    private void startActionBarTask() {
        int interval = configManager.getActionBarUpdateInterval();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    private void updateActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        ChunkPosition currentPos = new ChunkPosition(player.getLocation().getChunk());
        ChunkPosition lastPos = lastChunkMap.get(playerId);

        boolean isClaimed = claimManager.isChunkClaimed(currentPos);
        boolean chunkChanged = lastPos == null || !currentPos.equals(lastPos);

        // If chunk changed, compute the new actionbar string and cache it
        if (chunkChanged) {
            lastChunkMap.put(playerId, currentPos);
            lastClaimStatusMap.put(playerId, isClaimed);

            String message;
            if (isClaimed) {
                UUID ownerId = claimManager.getChunkOwner(currentPos);
                String ownerName = plugin.getServer().getOfflinePlayer(ownerId).getName();
                if (ownerName == null)
                    ownerName = "Unknown";

                if (playerId.equals(ownerId)) {
                    message = configManager.getActionBarMessage("actionbar-owned-by-you");
                } else if (trustManager.hasPermission(claimManager.getClaimAt(currentPos), player.getUniqueId(),
                        "INTERACT")) {
                    message = configManager.getActionBarMessage("actionbar-trusted").replace("<owner>", ownerName);
                } else if (player.hasPermission("landclaim.admin")) {
                    message = configManager.getActionBarMessage("actionbar-admin").replace("<owner>", ownerName);
                } else {
                    message = configManager.getActionBarMessage("actionbar-owned-by-other").replace("<owner>",
                            ownerName);
                }
            } else {
                message = configManager.getActionBarMessage("actionbar-wilderness");
            }

            lastActionBarMap.put(playerId, message);
        }

        // Always send the cached message to ensure persistence across all claims and
        // wilderness
        String cachedMessage = lastActionBarMap.get(playerId);
        if (cachedMessage != null) {
            sendActionBar(player, cachedMessage);
        }
    }

    private void sendActionBar(Player player, String message) {
        player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null)
            return;

        Chunk fromChunk = from.getChunk();
        Chunk toChunk = to.getChunk();
        if (fromChunk.equals(toChunk))
            return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (plugin.getCommandHandler().isAutoClaimEnabled(playerId)) {
            ChunkPosition pos = new ChunkPosition(toChunk);
            if (!claimManager.isChunkClaimed(pos)) {
                if (claimManager.claimChunk(player, toChunk)) {
                    player.sendMessage(configManager.getMessage("chunk-claimed"));
                }
            }
        }

        if (plugin.getCommandHandler().isAutoUnclaimEnabled(playerId)) {
            ChunkPosition fromPos = new ChunkPosition(fromChunk);
            if (claimManager.isChunkClaimed(fromPos) && claimManager.getChunkOwner(fromPos).equals(playerId)) {
                if (!isConnectedToOtherClaims(fromPos, playerId)) {
                    claimManager.unclaimChunk(fromChunk);
                    player.sendMessage(configManager.getMessage("auto-unclaimed"));
                }
            }
        }
    }

    private boolean isConnectedToOtherClaims(ChunkPosition pos, UUID playerId) {
        if (!configManager.requireConnectedClaims())
            return false;

        Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = claimManager.getPlayerClaims(playerId);
        Set<ChunkPosition> claims = claimObjects.stream()
                .flatMap(claim -> claim.getChunks().stream())
                .collect(java.util.stream.Collectors.toSet());
        if (claims.isEmpty())
            return false;

        Set<ChunkPosition> neighbors = new HashSet<>(pos.getNeighbors(configManager.allowDiagonalConnections()));
        neighbors.retainAll(claims);
        neighbors.remove(pos);

        return !neighbors.isEmpty();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        checkBlockPermission(event.getPlayer(), event.getBlock(), event, "BUILD");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkBlockPermission(event.getPlayer(), event.getBlock(), event, "BUILD");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        Material blockType = block.getType();

        if (CONTAINER_BLOCKS.contains(blockType)) {
            checkInteractionPermission(event.getPlayer(), event, "CONTAINER");
            return;
        }

        if (INTERACTABLE_BLOCKS.contains(blockType)) {
            checkInteractionPermission(event.getPlayer(), event, "INTERACT");
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        Material bucket = event.getBucket();

        if (bucket != Material.WATER_BUCKET && bucket != Material.LAVA_BUCKET) {
            return;
        }

        if (shouldCancelBucketPlacement(player, block)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("bucket-denied"));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager ||
                event.getEntity() instanceof Animals ||
                isPet(event.getEntity())) {

            // Get the attacking player
            Player damager = null;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (damager == null)
                return;

            Location location = event.getEntity().getLocation();
            if (isInProtectedChunk(location)) {
                ChunkPosition pos = new ChunkPosition(location);
                if (claimManager.isChunkClaimed(pos)) {
                    Claim claim = claimManager.getClaimAt(pos);
                    if (damager.getUniqueId().equals(claim.getOwnerId()) ||
                            trustManager.hasPermission(claim, damager.getUniqueId(), "DAMAGE_ENTITIES")) {
                        return; // Owner or trusted can harm
                    }

                    event.setCancelled(true);
                    damager.sendMessage(configManager.getMessage("harm-entity-denied"));
                }
            }
            return;
        }

        if (!configManager.preventPvP())
            return;

        Player attacker = null;
        Player victim = null;

        // Handle direct player attacks
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            attacker = (Player) event.getDamager();
            victim = (Player) event.getEntity();
        }
        // Handle projectile attacks (arrows, etc.)
        else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                attacker = (Player) projectile.getShooter();
                victim = (Player) event.getEntity();
            }
        }

        if (attacker == null || victim == null)
            return;

        Location location = victim.getLocation();
        if (isInProtectedChunk(location)) {
            event.setCancelled(true);
            attacker.sendMessage(configManager.getMessage("pvp-denied"));
        }
    }

    private boolean isPet(Entity entity) {
        // Check if entity is a tamed animal
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            return tameable.isTamed() && tameable.getOwner() != null;
        }
        return false;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!configManager.preventExplosionDamage())
            return;

        Location explosionLoc = event.getLocation();
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

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!configManager.preventExplosionDamage())
            return;

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

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        for (Block block : event.getBlocks()) {
            Block pushedTo = block.getRelative(event.getDirection());
            ChunkPosition targetChunk = new ChunkPosition(pushedTo);

            if (claimManager.isChunkClaimed(targetChunk)) {
                UUID targetOwner = claimManager.getChunkOwner(targetChunk);
                if (pistonOwner == null || !pistonOwner.equals(targetOwner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        ChunkPosition pistonChunk = new ChunkPosition(event.getBlock());
        UUID pistonOwner = claimManager.isChunkClaimed(pistonChunk) ? claimManager.getChunkOwner(pistonChunk) : null;

        for (Block block : event.getBlocks()) {
            ChunkPosition targetChunk = new ChunkPosition(block);

            if (claimManager.isChunkClaimed(targetChunk)) {
                UUID targetOwner = claimManager.getChunkOwner(targetChunk);
                if (pistonOwner == null || !pistonOwner.equals(targetOwner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
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

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!configManager.preventMobGriefing())
            return;

        // Check for griefing mobs (endermen, ravagers, etc.)
        EntityType type = event.getEntityType();
        if (type == EntityType.ENDERMAN || type == EntityType.RAVAGER) {
            // Check if block change is in claimed chunk
            if (isInProtectedChunk(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInProtectedChunk(Location location) {
        ChunkPosition pos = new ChunkPosition(location);
        return claimManager.isChunkClaimed(pos);
    }

    private void checkBlockPermission(Player player, Block block, org.bukkit.event.Cancellable event,
            String permission) {
        if (player.hasPermission("landclaim.admin"))
            return;

        // Always check the block's chunk, not the player's chunk
        ChunkPosition pos = new ChunkPosition(block);

        if (claimManager.isChunkClaimed(pos)) {
            Claim claim = claimManager.getClaimAt(pos);
            if (player.getUniqueId().equals(claim.getOwnerId())) {
                return; // Owner can build
            }

            if (trustManager.hasPermission(claim, player.getUniqueId(), permission)) {
                return; // Has required role flag
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied"));
        }
    }

    private boolean shouldCancelBucketPlacement(Player player, Block block) {
        if (player.hasPermission("landclaim.admin")) {
            return false;
        }

        ChunkPosition pos = new ChunkPosition(block);

        if (claimManager.isChunkClaimed(pos)) {
            Claim claim = claimManager.getClaimAt(pos);
            UUID playerId = player.getUniqueId();

            // Allow owner
            if (playerId.equals(claim.getOwnerId()))
                return false;

            // Check for BUILD flag
            return !trustManager.hasPermission(claim, playerId, "BUILD");
        }

        return false;
    }

    private void checkInteractionPermission(Player player, PlayerInteractEvent event, String permission) {
        if (player.hasPermission("landclaim.admin"))
            return;

        Block block = event.getClickedBlock();
        ChunkPosition pos = new ChunkPosition(block);

        if (claimManager.isChunkClaimed(pos)) {
            Claim claim = claimManager.getClaimAt(pos);
            if (player.getUniqueId().equals(claim.getOwnerId())) {
                return; // Owner can always interact
            }

            if (trustManager.hasPermission(claim, player.getUniqueId(), permission)) {
                return; // Has required role flag
            }

            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("access-denied-interact"));
        }
    }

    // Removed vulnerable onPlayerChat handler - trust menu is now accessed via GUI
    // only

    /**
     * Clean up player data when they quit to prevent memory leaks
     */
    public void cleanupPlayer(UUID playerId) {
        lastChunkMap.remove(playerId);
        lastActionBarMap.remove(playerId);
        lastClaimStatusMap.remove(playerId);
    }
}