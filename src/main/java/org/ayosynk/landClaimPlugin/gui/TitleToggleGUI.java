package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.TitleSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TitleToggleGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        TitleSettingsConfig config = plugin.getConfigManager().getTitleSettingsConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F T F E F O F F",
                                        "F F F F F F F F F",
                                        "F F F S < S F F F"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name,
                                        config.navSpacer.lore));
                        ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                        config.titleToggle.lore, (p, e) -> {
                                                // Reserved: Toggle on and off
                                        }));
                        ingredients.put('E', GuiHelper.buildSlot(config.onEntry.material, config.onEntry.name,
                                        config.onEntry.lore, (p, e) -> {
                                                // Reserved: Configure entry title behavior
                                        }));
                        ingredients.put('O', GuiHelper.buildSlot(config.onLeaveTitle.material, config.onLeaveTitle.name,
                                        config.onLeaveTitle.lore, (p, e) -> {
                                                // Reserved: Configure leave title behavior
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
