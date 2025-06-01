package org.ayosynk.landClaimPlugin.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.commands.CommandHandler;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EventListener implements Listener {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final CommandHandler commandHandler;
    private final Map<UUID, ChunkPosition> lastChunkMap = new HashMap<>();
    private final Map<UUID, String> lastActionBarMap = new HashMap<>();

    // List of interactable blocks
    private static final Set<Material> INTERACTABLE_BLOCKS = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER,
            Material.DROPPER, Material.DISPENSER, Material.BREWING_STAND,
            Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR,
            Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.CHERRY_DOOR, Material.PALE_OAK_DOOR, Material.BAMBOO_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR,
            Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR, Material.PALE_OAK_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR, Material.BAMBOO_BUTTON, Material.CHERRY_BUTTON, Material.PALE_OAK_BUTTON,
            Material.DARK_OAK_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR,
            Material.IRON_DOOR, Material.IRON_TRAPDOOR, Material.LEVER, Material.STONE_BUTTON, Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON, Material.WARPED_BUTTON,
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.GRINDSTONE,
            Material.SMITHING_TABLE, Material.LOOM, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
            Material.STONECUTTER, Material.BELL, Material.COMPOSTER,
            Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    ));

    public EventListener(LandClaimPlugin plugin, ClaimManager claimManager,
                         TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.commandHandler = plugin.getCommandHandler();

        startActionBarTask();
    }

    private void startActionBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0, 10); // Update every 1/2 second
    }

    private void updateActionBar(Player player) {
        ChunkPosition currentPos = new ChunkPosition(player.getLocation().getChunk());
        ChunkPosition lastPos = lastChunkMap.get(player.getUniqueId());

        // Only update if chunk changed
        if (lastPos == null || !currentPos.equals(lastPos)) {
            lastChunkMap.put(player.getUniqueId(), currentPos);

            if (claimManager.isChunkClaimed(currentPos)) {
                UUID ownerId = claimManager.getChunkOwner(currentPos);
                String ownerName = plugin.getServer().getOfflinePlayer(ownerId).getName();
                if (ownerName == null) ownerName = "Unknown";

                String message = configManager.getConfig().getString("messages.actionbar-owner", "&e{owner}'s claim")
                        .replace("{owner}", ownerName);

                sendActionBar(player, ChatUtils.colorize(message));
                lastActionBarMap.put(player.getUniqueId(), message);
            } else if (lastActionBarMap.containsKey(player.getUniqueId())) {
                // Clear action bar when not in claim
                sendActionBar(player, "");
                lastActionBarMap.remove(player.getUniqueId());
            }
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        Chunk fromChunk = from.getChunk();
        Chunk toChunk = to.getChunk();
        if (fromChunk.equals(toChunk)) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (commandHandler.isAutoClaimEnabled(playerId)) {
            ChunkPosition pos = new ChunkPosition(toChunk);
            if (!claimManager.isChunkClaimed(pos)) {
                if (claimManager.claimChunk(player, toChunk)) {
                    player.sendMessage(ChatUtils.colorize(configManager.getConfig().getString("messages.chunk-claimed")));
                }
            }
        }

        if (commandHandler.isAutoUnclaimEnabled(playerId)) {
            ChunkPosition fromPos = new ChunkPosition(fromChunk);
            if (claimManager.isChunkClaimed(fromPos) && claimManager.getChunkOwner(fromPos).equals(playerId)) {
                claimManager.unclaimChunk(fromChunk);
                player.sendMessage(ChatUtils.colorize(configManager.getConfig().getString("messages.auto-unclaimed")));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        checkBlockPermission(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        checkBlockPermission(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();

        if (!INTERACTABLE_BLOCKS.contains(block.getType())) return;

        // Check the block's location
        if (shouldCancelInteraction(event.getPlayer(), block)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatUtils.colorize(
                    configManager.getConfig().getString("messages.access-denied-interact")
            ));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!configManager.preventPvP()) return;

        // Check if it's PvP damage
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            Location location = victim.getLocation();

            // Check if in claimed chunk
            if (isInProtectedChunk(location)) {
                event.setCancelled(true);
                attacker.sendMessage(ChatUtils.colorize(
                        configManager.getConfig().getString("messages.pvp-denied")
                ));
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!configManager.preventExplosionDamage()) return;

        // Get explosion location
        Location explosionLoc = event.getLocation();
        ChunkPosition explosionChunk = new ChunkPosition(explosionLoc);

        // Check if explosion is near any claims
        boolean nearClaim = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPosition checkPos = new ChunkPosition(
                        explosionLoc.getWorld().getName(),
                        explosionChunk.getX() + dx,
                        explosionChunk.getZ() + dz
                );
                if (claimManager.isChunkClaimed(checkPos)) {
                    nearClaim = true;
                    break;
                }
            }
            if (nearClaim) break;
        }

        // If not near any claim, do nothing
        if (!nearClaim) return;

        // Remove any blocks that are in claimed chunks from the explosion list
        event.blockList().removeIf(block -> {
            ChunkPosition blockChunk = new ChunkPosition(block);
            return claimManager.isChunkClaimed(blockChunk);
        });
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!configManager.preventMobGriefing()) return;

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

    private void checkBlockPermission(Player player, Block block, org.bukkit.event.Cancellable event) {
        if (player.hasPermission("landclaim.admin")) return;

        // Always check the block's chunk, not the player's chunk
        ChunkPosition pos = new ChunkPosition(block);

        if (claimManager.isChunkClaimed(pos)) {
            UUID owner = claimManager.getChunkOwner(pos);
            if (player.getUniqueId().equals(owner)) {
                return; // Owner can build
            }

            // Check global trust for this owner
            if (trustManager.isTrusted(owner, player)) {
                return; // Trusted player can build
            }

            event.setCancelled(true);
            player.sendMessage(ChatUtils.colorize(
                    configManager.getConfig().getString("messages.access-denied")
            ));
        }
    }

    private boolean shouldCancelInteraction(Player player, Block block) {
        if (player.hasPermission("landclaim.admin")) return false;

        ChunkPosition pos = new ChunkPosition(block);

        if (claimManager.isChunkClaimed(pos)) {
            UUID owner = claimManager.getChunkOwner(pos);
            UUID playerId = player.getUniqueId();

            return !playerId.equals(owner) &&
                    !trustManager.isTrusted(owner, player);
        }

        return false;
    }
}