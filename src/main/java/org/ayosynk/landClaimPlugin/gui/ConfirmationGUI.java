package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable confirmation dialog — 1-row GUI with Confirm (green) and Cancel
 * (red).
 */
public class ConfirmationGUI {

    public static void open(Player player, String title, Runnable onConfirm, Runnable onCancel) {
        String[] structure = {
                "F F F Y F N F F F"
        };

        Map<Character, SlotDefinition> ingredients = new HashMap<>();
        ingredients.put('F', GuiHelper.buildSlot("GRAY_STAINED_GLASS_PANE", " ", List.of()));
        ingredients.put('Y', GuiHelper.buildSlot("LIME_WOOL", "<green>Confirm",
                List.of("<gray>Click to confirm."), (p, e) -> {
                    p.closeInventory();
                    if (onConfirm != null)
                        onConfirm.run();
                }));
        ingredients.put('N', GuiHelper.buildSlot("RED_WOOL", "<red>Cancel",
                List.of("<gray>Click to cancel."), (p, e) -> {
                    p.closeInventory();
                    if (onCancel != null)
                        onCancel.run();
                }));

        Component guiTitle = GuiHelper.MM.deserialize(title);
        CustomGui gui = new CustomGui(guiTitle, 1);
        gui.fillFromStructure(structure, ingredients);
        gui.open(player);
    }
}
