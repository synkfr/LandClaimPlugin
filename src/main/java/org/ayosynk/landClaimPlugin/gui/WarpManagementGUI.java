package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpManagementGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        WarpManagementConfig config = plugin.getConfigManager().getWarpManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P B B B < B B B N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                MainMenuGUI.open(p, claim, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

                        gui.setPrevButton(27,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
