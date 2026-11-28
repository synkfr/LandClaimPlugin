package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable confirmation dialog — 1-row GUI with Confirm and Cancel actions.
 */
public class ConfirmationGUI {

    public static void open(Player player, String title, Runnable onConfirm, Runnable onCancel) {
        ConfigManager cm = LandClaimPlugin.getInstance().getConfigManager();
        String yesName = cm.getRawTemplate("gui.confirm-yes-name");
        String yesLore = cm.getRawTemplate("gui.confirm-yes-lore");
        String noName = cm.getRawTemplate("gui.confirm-no-name");
        String noLore = cm.getRawTemplate("gui.confirm-no-lore");

        String[] structure = {
                "F F F Y F N F F F"
        };

        Map<Character, SlotDefinition> ingredients = new HashMap<>();
        ingredients.put('F', GuiHelper.buildSlot("BLACK_STAINED_GLASS_PANE", " ", List.of()));
        ingredients.put('Y', GuiHelper.buildSlot("LIME_CONCRETE", yesName,
                List.of(yesLore), (p, e) -> {
                    p.closeInventory();
                    if (onConfirm != null)
                        onConfirm.run();
                }));
        ingredients.put('N', GuiHelper.buildSlot("RED_CONCRETE", noName,
                List.of(noLore), (p, e) -> {
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
