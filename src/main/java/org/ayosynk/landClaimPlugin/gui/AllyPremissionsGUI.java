package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.AllyPremissionsConfig;
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

public class AllyPremissionsGUI {

    public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AllyPremissionsConfig config = plugin.getConfigManager().getAllyPremissionsConfig();

            List<GuiItem> contentItems = new ArrayList<>();

            String[] structure = {
                    "F F F F I F F F F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "P N N N < N N N V"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
            ingredients.put('N', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                    config.navFill.lore));
            ingredients.put('I', GuiHelper.buildSlot(config.info.material, config.info.name, config.info.lore));
            ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                    (p, e) -> {
                        p.closeInventory();
                        AllyControlPanelGUI.open(p, profile, plugin);
                    }));

            Component title = GuiHelper.MM.deserialize(config.title);
            PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

            gui.setPrevButton(45,
                    GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                            config.previousPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));
            gui.setNextButton(53,
                    GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));

            gui.setContent(contentItems, player);
            gui.open(player);
        });
    }
}
