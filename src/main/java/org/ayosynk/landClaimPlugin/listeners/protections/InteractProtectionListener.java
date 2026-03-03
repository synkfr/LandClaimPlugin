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
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InteractProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    private static final Set<Material> DOORS = new HashSet<>(Arrays.asList(
            Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR,
            Material.OAK_DOOR, Material.SPRUCE_DOOR, Material.CHERRY_DOOR, Material.PALE_OAK_DOOR,
            Material.BAMBOO_DOOR, Material.CRIMSON_DOOR, Material.WARPED_DOOR, Material.IRON_DOOR));

    private static final Set<Material> TRAPDOORS = new HashSet<>(Arrays.asList(
            Material.ACACIA_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR,
            Material.PALE_OAK_TRAPDOOR, Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR, Material.DARK_OAK_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR, Material.OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.IRON_TRAPDOOR));

    private static final Set<Material> FENCE_GATES = new HashSet<>(Arrays.asList(
            Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.CHERRY_FENCE_GATE,
            Material.PALE_OAK_FENCE_GATE,
            Material.BAMBOO_FENCE_GATE, Material.CRIMSON_FENCE_GATE, Material.WARPED_FENCE_GATE));

    private static final Set<Material> CONTAINERS = new HashSet<>(Arrays.asList(
            Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL,
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER,
            Material.DROPPER, Material.DISPENSER, Material.BREWING_STAND,
            Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX));

    private static final Set<Material> WORKSTATIONS = new HashSet<>(Arrays.asList(
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.GRINDSTONE,
            Material.SMITHING_TABLE, Material.LOOM, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
            Material.STONECUTTER, Material.COMPOSTER, Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE));

    private static final Set<Material> REDSTONE = new HashSet<>(Arrays.asList(
            Material.LEVER, Material.STONE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON, Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON, Material.CRIMSON_BUTTON,
            Material.WARPED_BUTTON, Material.BAMBOO_BUTTON, Material.CHERRY_BUTTON, Material.PALE_OAK_BUTTON,
            Material.STONE_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE, Material.PALE_OAK_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE));

    public InteractProtectionListener(LandClaimPlugin plugin, ClaimManager claimManager,
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        Material type = block.getType();

        if (DOORS.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_DOORS");
        } else if (TRAPDOORS.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_TRAPDOORS");
        } else if (FENCE_GATES.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_FENCE_GATES");
        } else if (CONTAINERS.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_CONTAINERS");
        } else if (WORKSTATIONS.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_WORKSTATIONS");
        } else if (REDSTONE.contains(type)) {
            checkPermission(event.getPlayer(), block, event, "USE_REDSTONE");
        } else if (type.name().endsWith("_BED")) {
            checkPermission(event.getPlayer(), block, event, "USE_BEDS");
        } else if (type == Material.LECTERN) {
            checkPermission(event.getPlayer(), block, event, "USE_LECTERNS");
        } else if (type == Material.BELL) {
            checkPermission(event.getPlayer(), block, event, "USE_BELLS");
        }
    }
}
