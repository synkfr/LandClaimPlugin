package org.ayosynk.landClaimPlugin.gui.framework;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Paginated GUI runtime with diff-based page updates.
 * <p>
 * Content is stored as {@link GuiItem} instances (lazy rendering).
 * Page transitions only update the content slots that actually changed,
 * avoiding full inventory clears.
 */
public class PaginatedGui extends CustomGui {

    private final int[] contentSlots;
    private List<GuiItem> content = Collections.emptyList();
    private int currentPage = 0;

    // Previous / Next button state
    private int prevSlot = -1;
    private ItemStack prevActiveItem;
    private ItemStack prevInactiveItem;
    private Boolean prevWasActive;
    private int nextSlot = -1;
    private ItemStack nextActiveItem;
    private ItemStack nextInactiveItem;
    private Boolean nextWasActive;

    /**
     * Creates a paginated GUI.
     *
     * @param title           window title
     * @param rows            total rows (including navigation row)
     * @param structure       structure strings (use 'x' for content slots)
     * @param ingredients     ingredient map (excluding 'x' and 'P'/'N' navigation markers)
     * @param contentSlotChar character used to mark content slots in structure
     */
    public PaginatedGui(Component title, int rows, String[] structure,
            Map<Character, SlotDefinition> ingredients, char contentSlotChar) {
        super(title, rows);

        List<Integer> contentSlotList = new ArrayList<>();
        parseStructure(structure, ingredients, (slot, c) -> {
            if (c == contentSlotChar) {
                contentSlotList.add(slot);
            } else if (c == 'P' && this.prevSlot < 0) {
                this.prevSlot = slot;
            } else if (c == 'N' && this.nextSlot < 0) {
                this.nextSlot = slot;
            }
        });
        this.contentSlots = new int[contentSlotList.size()];
        for (int i = 0; i < contentSlotList.size(); i++) {
            this.contentSlots[i] = contentSlotList.get(i);
        }
    }

    /**
     * Sets the content list and renders page 0.
     */
    public void setContent(List<GuiItem> items, Player viewer) {
        this.content = items != null ? items : Collections.emptyList();
        this.prevWasActive = null;
        this.nextWasActive = null;
        setPage(0, viewer);
    }

    /**
     * @return the current page index (0-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Navigate to a specific page. Only updates changed slots (diff-based).
     */
    public void setPage(int page, Player viewer) {
        if (page < 0 || page >= getPageCount())
            return;
        this.currentPage = page;
        renderPage(viewer);
    }

    /**
     * Renders the current page content into the content slots.
     */
    private void renderPage(Player viewer) {
        int startIndex = currentPage * contentSlots.length;

        for (int i = 0; i < contentSlots.length; i++) {
            int dataIndex = startIndex + i;
            int invSlot = contentSlots[i];

            if (dataIndex < content.size()) {
                GuiItem guiItem = content.get(dataIndex);
                ItemStack rendered = guiItem.render(viewer);
                ClickAction action = guiItem.clickAction();
                setItem(invSlot, rendered, action);
            } else {
                // Clear unused content slots
                setItem(invSlot, null, null);
            }
        }

        // Update navigation buttons
        updateNavButtons();
    }

    // ---- Navigation buttons ----

    /**
     * Configure the previous-page button at the parsed {@code 'P'} slot.
     */
    public void setPrevButton(ItemStack activeItem, ItemStack inactiveItem) {
        if (this.prevSlot >= 0) {
            setPrevButton(this.prevSlot, activeItem, inactiveItem);
        }
    }

    /**
     * Configure the previous-page button.
     *
     * @param slot         inventory slot for the button
     * @param activeItem   item shown when previous page exists
     * @param inactiveItem item shown when on first page (filler)
     */
    public void setPrevButton(int slot, ItemStack activeItem, ItemStack inactiveItem) {
        this.prevSlot = slot;
        this.prevActiveItem = activeItem;
        this.prevInactiveItem = inactiveItem;
        this.prevWasActive = null;
        setItem(slot, inactiveItem, null);
    }

    /**
     * Configure the next-page button at the parsed {@code 'N'} slot.
     */
    public void setNextButton(ItemStack activeItem, ItemStack inactiveItem) {
        if (this.nextSlot >= 0) {
            setNextButton(this.nextSlot, activeItem, inactiveItem);
        }
    }

    /**
     * Configure the next-page button.
     *
     * @param slot         inventory slot for the button
     * @param activeItem   item shown when next page exists
     * @param inactiveItem item shown when on last page (filler)
     */
    public void setNextButton(int slot, ItemStack activeItem, ItemStack inactiveItem) {
        this.nextSlot = slot;
        this.nextActiveItem = activeItem;
        this.nextInactiveItem = inactiveItem;
        this.nextWasActive = null;
        setItem(slot, inactiveItem, null);
    }

    private void updateNavButtons() {
        if (prevSlot >= 0) {
            boolean hasPrev = currentPage > 0;
            if (prevWasActive == null || prevWasActive != hasPrev) {
                prevWasActive = hasPrev;
                setItem(prevSlot, hasPrev ? prevActiveItem : prevInactiveItem,
                        hasPrev ? (player, event) -> setPage(currentPage - 1, player) : null);
            }
        }
        if (nextSlot >= 0) {
            boolean hasNext = currentPage < getPageCount() - 1;
            if (nextWasActive == null || nextWasActive != hasNext) {
                nextWasActive = hasNext;
                setItem(nextSlot, hasNext ? nextActiveItem : nextInactiveItem,
                        hasNext ? (player, event) -> setPage(currentPage + 1, player) : null);
            }
        }
    }

    @Override
    public void dispose() {
        content = Collections.emptyList();
        prevActiveItem = null;
        prevInactiveItem = null;
        nextActiveItem = null;
        nextInactiveItem = null;
        super.dispose();
    }

    // ---- Query ----

    public int getPage() {
        return currentPage;
    }

    public int getPageCount() {
        if (content.isEmpty() || contentSlots.length == 0)
            return 1;
        return (int) Math.ceil((double) content.size() / contentSlots.length);
    }

    public int getContentSlotCount() {
        return contentSlots.length;
    }
}
