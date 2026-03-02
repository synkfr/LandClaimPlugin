package org.ayosynk.landClaimPlugin.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Functional interface for GUI click handlers.
 * Implementations receive the clicking player and the raw Bukkit event
 * (already cancelled by the framework).
 */
@FunctionalInterface
public interface ClickAction {
    void onClick(Player player, InventoryClickEvent event);
}
