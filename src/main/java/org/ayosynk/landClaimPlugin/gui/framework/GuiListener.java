package org.ayosynk.landClaimPlugin.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Single Bukkit listener for all custom GUIs.
 * Registered once at plugin enable via
 * {@link org.ayosynk.landClaimPlugin.gui.GuiGlobalSetup}.
 * <p>
 * Handles:
 * <ul>
 * <li>Click dispatch with rawSlot filtering</li>
 * <li>Blocks shift-click, number-key, offhand-swap, double-click into GUI</li>
 * <li>Drag cancellation</li>
 * <li>Dispose on close (memory leak prevention)</li>
 * </ul>
 */
public final class GuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof CustomGui gui))
            return;

        // Cancel ALL interactions with custom GUI inventories
        event.setCancelled(true);

        // Block interactions that could move items in/out
        switch (event.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                return;
            } // shift-click
            case NUMBER_KEY -> {
                return;
            } // number key swap
            case SWAP_OFFHAND -> {
                return;
            } // F / offhand swap
            case DOUBLE_CLICK -> {
                return;
            } // collect-to-cursor
            case CREATIVE -> {
                return;
            } // creative middle-click
            default -> {
            }
        }

        // Use getRawSlot() — NOT getSlot() — to correctly distinguish
        // top inventory from player inventory
        int rawSlot = event.getRawSlot();

        // Ignore clicks outside the GUI (bottom inventory or outside window)
        if (rawSlot < 0 || rawSlot >= topInv.getSize())
            return;

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        gui.handleClick(rawSlot, player, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!(topInv.getHolder() instanceof CustomGui))
            return;

        // Cancel if any dragged slot is in the top (GUI) inventory
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 0 && rawSlot < topInv.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (topInv.getHolder() instanceof CustomGui gui) {
            gui.dispose();
        }
    }
}
