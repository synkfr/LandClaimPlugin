package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WarpControlPanelGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        WarpControlPanelConfig config = plugin.getConfigManager().getWarpControlPanelConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F L F I F D F F",
                                        "F F F F F F F F F",
                                        "S S S S < S S S S"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.spacer.material, config.spacer.name,
                                        config.spacer.lore));
                        ingredients.put('L',
                                        GuiHelper.buildSlot(config.changeLocation.material, config.changeLocation.name,
                                                        config.changeLocation.lore, (p, e) -> {
                                                                // Reserved: Update warp to current location
                                                        }));
                        ingredients.put('I', GuiHelper.buildSlot(config.changeIcon.material, config.changeIcon.name,
                                        config.changeIcon.lore, (p, e) -> {
                                                p.closeInventory();
                                                WarpChangeIconGUI.open(p, claim, plugin);
                                        }));
                        ingredients.put('D', GuiHelper.buildSlot(config.deleteWarp.material, config.deleteWarp.name,
                                        config.deleteWarp.lore, (p, e) -> {
                                                // Reserved: Opens confirmation GUI
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                WarpManagementGUI.open(p, claim, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
