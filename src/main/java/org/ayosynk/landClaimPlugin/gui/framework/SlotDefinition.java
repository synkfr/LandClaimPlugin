package org.ayosynk.landClaimPlugin.gui.framework;

import org.bukkit.inventory.ItemStack;

/**
 * Pairs an {@link ItemStack} with an optional {@link ClickAction} for use
 * in structure ingredient maps.
 *
 * @param item   the visual item for this slot
 * @param action click handler, or {@code null} for decorative-only slots
 */
public record SlotDefinition(ItemStack item, ClickAction action) {

    /** Decorative-only slot — no click handler. */
    public SlotDefinition(ItemStack item) {
        this(item, null);
    }
}
