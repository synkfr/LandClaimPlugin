package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RenameClaimConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RenameClaimGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        RenameClaimConfig config = plugin.getConfigManager().getRenameClaimConfig();

                        String[] structure = {
                                        "O O B B B B B O O",
                                        "O B B C B R B B O",
                                        "B B B B B B B B B",
                                        "O S < S B B B B B"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('O', GuiHelper.buildSlot(config.outerFrame.material, config.outerFrame.name,
                                        config.outerFrame.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.background.material, config.background.name,
                                        config.background.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name,
                                        config.navSpacer.lore));
                        ingredients.put('C', GuiHelper.buildSlot(config.changeName.material, config.changeName.name,
                                        config.changeName.lore, (p, e) -> {
                                                // Reserved: Opens chat input or anvil rename UI
                                        }));
                        ingredients.put('R',
                                        GuiHelper.buildSlot(config.resetToDefault.material, config.resetToDefault.name,
                                                        config.resetToDefault.lore, (p, e) -> {
                                                                // Reserved: Opens confirmation GUI
                                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
