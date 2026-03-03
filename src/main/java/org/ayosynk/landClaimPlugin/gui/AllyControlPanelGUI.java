package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.AllyControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AllyControlPanelGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        AllyControlPanelConfig config = plugin.getConfigManager().getAllyControlPanelConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F P F W F R F F",
                                        "F F F F F F F F F",
                                        "S S S S < S S S S"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.spacer.material, config.spacer.name,
                                        config.spacer.lore));
                        ingredients.put('P',
                                        GuiHelper.buildSlot(config.allyPermissions.material,
                                                        config.allyPermissions.name,
                                                        config.allyPermissions.lore, (p, e) -> {
                                                                p.closeInventory();
                                                                AllyPremissionsGUI.open(p, profile, plugin);
                                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.allyWarps.material, config.allyWarps.name,
                                        config.allyWarps.lore, (p, e) -> {
                                                // No action (coming later)
                                        }));
                        ingredients.put('R', GuiHelper.buildSlot(config.removeAlly.material, config.removeAlly.name,
                                        config.removeAlly.lore, (p, e) -> {
                                                // Reserved: Opens confirmation GUI
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                AllyManagementGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
