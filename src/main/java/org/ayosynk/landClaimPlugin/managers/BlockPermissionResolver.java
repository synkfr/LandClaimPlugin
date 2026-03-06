package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.models.BlockPermission;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;

/**
 * Resolves a Minecraft Block into a specific BlockPermission for claim
 * protection.
 */
public class BlockPermissionResolver {

    /**
     * Resolves a block interaction into its required permission node.
     * 
     * @return the required BlockPermission, or null if freely interactable.
     */
    public static BlockPermission resolve(Block block) {
        if (block == null)
            return null;
        Material type = block.getType();

        // 1. Specific explicit overrides
        if (type == Material.LECTERN)
            return BlockPermission.LECTERNS;
        if (type == Material.BELL)
            return BlockPermission.BELLS;

        // 2. Containers (Catches Chests, Barrels, Crafters, Hoppers, Shulker Boxes,
        // Furnaces, etc.)
        // Must come AFTER Lectern since Lectern implements InventoryHolder but has its
        // own permission.
        if (block.getState() instanceof InventoryHolder) {
            return BlockPermission.CONTAINERS;
        }

        // 3. Tag-based matching
        if (Tag.DOORS.isTagged(type))
            return BlockPermission.DOORS;
        if (Tag.TRAPDOORS.isTagged(type))
            return BlockPermission.TRAPDOORS;
        if (Tag.FENCE_GATES.isTagged(type))
            return BlockPermission.FENCE_GATES;
        if (Tag.BEDS.isTagged(type))
            return BlockPermission.BEDS;

        if (Tag.BUTTONS.isTagged(type)
                || Tag.PRESSURE_PLATES.isTagged(type)
                || type == Material.LEVER
                || type == Material.TRIPWIRE_HOOK
                || type == Material.DAYLIGHT_DETECTOR) {
            return BlockPermission.REDSTONE;
        }

        // 4. Workstations (Explicit set to avoid catching unrelated blocks)
        switch (type) {
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case GRINDSTONE:
            case SMITHING_TABLE:
            case LOOM:
            case CARTOGRAPHY_TABLE:
            case FLETCHING_TABLE:
            case STONECUTTER:
            case COMPOSTER:
            case CRAFTING_TABLE:
            case ENCHANTING_TABLE:
                return BlockPermission.WORKSTATIONS;
            default:
                return null;
        }
    }
}
