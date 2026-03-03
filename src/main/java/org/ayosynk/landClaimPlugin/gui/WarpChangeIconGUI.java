package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpChangeIconConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpChangeIconGUI {

    public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            WarpChangeIconConfig config = plugin.getConfigManager().getWarpChangeIconConfig();

            List<GuiItem> contentItems = new ArrayList<>();

            String[] structure = {
                    "x x x x x x x x x",
                    "x x x x x x x x x",
                    "x x x x x x x x x",
                    "P F F S < S F F N"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                    config.navFill.lore));
            ingredients.put('S', GuiHelper.buildSlot(config.spacer.material, config.spacer.name,
                    config.spacer.lore));
            ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                    (p, e) -> {
                        p.closeInventory();
                        WarpControlPanelGUI.open(p, profile, plugin);
                    }));

            Component title = GuiHelper.MM.deserialize(config.title);
            PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

            gui.setPrevButton(27,
                    GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                            config.previousPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));
            gui.setNextButton(35,
                    GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));

            gui.setContent(contentItems, player);
            gui.open(player);
        });
    }
}
