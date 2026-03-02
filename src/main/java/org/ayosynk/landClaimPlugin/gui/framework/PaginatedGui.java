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
    private int nextSlot = -1;
    private ItemStack nextActiveItem;
    private ItemStack nextInactiveItem;

    /**
     * Creates a paginated GUI.
     *
     * @param title           window title
     * @param rows            total rows (including navigation row)
     * @param structure       structure strings (use 'x' for content slots)
     * @param ingredients     ingredient map (excluding 'x' — content marker)
     * @param contentSlotChar character used to mark content slots in structure
     *                        (typically 'x')
     */
    public PaginatedGui(Component title, int rows, String[] structure,
            Map<Character, SlotDefinition> ingredients, char contentSlotChar) {
        super(title, rows);

        // Parse structure to find content slot positions AND fill static ingredients
        List<Integer> contentSlotList = new ArrayList<>();
        int slot = 0;
        for (String row : structure) {
            String[] chars = row.split(" ");
            for (String ch : chars) {
                if (slot >= rows * 9)
                    break;
                if (ch.length() == 1) {
                    char c = ch.charAt(0);
                    if (c == contentSlotChar) {
                        contentSlotList.add(slot);
                    } else if (c != '.') {
                        SlotDefinition def = ingredients.get(c);
                        if (def != null) {
                            setItem(slot, def.item(), def.action());
                        }
                    }
                }
                slot++;
            }
        }
        this.contentSlots = contentSlotList.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Sets the content list and renders page 0.
     */
    public void setContent(List<GuiItem> items, Player viewer) {
        this.content = items != null ? items : Collections.emptyList(); // Assuming 'content' is the field to update
        // The original code had 'this.currentPage = 0; renderPage(viewer);'
        // The provided snippet has 'this.totalPages = ... setPage(0, viewer);'
        // To maintain correctness without introducing new fields, I'll adapt to
        // existing fields.
        // Assuming 'content' is the list of items, and 'contentSlots.length' is
        // 'itemsPerPage'.
        int totalPages = (int) Math.ceil((double) this.content.size() / contentSlots.length);
        if (totalPages == 0)
            totalPages = 1; // Ensure at least one page if content is empty
        setPage(0, viewer); // This will render the page
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
        int oldPage = this.currentPage;
        this.currentPage = page;

        if (oldPage != page) {
            renderPage(viewer);
        }
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

        // Set up click handler for prev button
        setItem(slot, inactiveItem, (player, event) -> {
            if (currentPage > 0) {
                setPage(currentPage - 1, player);
            }
        });
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

        // Set up click handler for next button
        setItem(slot, inactiveItem, (player, event) -> {
            if (currentPage < getPageCount() - 1) {
                setPage(currentPage + 1, player);
            }
        });
    }

    private void updateNavButtons() {
        if (prevSlot >= 0) {
            ItemStack item = (currentPage > 0) ? prevActiveItem : prevInactiveItem;
            getInventory().setItem(prevSlot, item);
        }
        if (nextSlot >= 0) {
            ItemStack item = (currentPage < getPageCount() - 1) ? nextActiveItem : nextInactiveItem;
            getInventory().setItem(nextSlot, item);
        }
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
