package org.ayosynk.landClaimPlugin.gui.framework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Lazy-rendered content entry for paginated GUIs.
 * Items are only materialized when their page becomes visible,
 * enabling dynamic placeholders and permission checks without ticking.
 */
public interface GuiItem {

    /**
     * Render this entry into a displayable ItemStack.
     * Called each time the page containing this item becomes active.
     *
     * @param viewer the player viewing the GUI
     * @return the rendered item
     */
    ItemStack render(Player viewer);

    /**
     * Optional click handler for this content entry.
     *
     * @return click action, or {@code null} for non-interactive entries
     */
    default ClickAction clickAction() {
        return null;
    }
}
