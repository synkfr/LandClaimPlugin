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

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin,
                        ClaimProfile targetAllyProfile) {
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
                                                        config.allyPermissions.name.replace("<name>",
                                                                        targetAllyProfile.getName()),
                                                        config.allyPermissions.lore, (p, e) -> {
                                                                p.closeInventory();
                                                                AllyPremissionsGUI.open(p, profile, plugin,
                                                                                targetAllyProfile);
                                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.allyWarps.material, config.allyWarps.name,
                                        config.allyWarps.lore, (p, e) -> {
                                                // No action (coming later)
                                        }));
                        ingredients.put('R', GuiHelper.buildSlot(config.removeAlly.material,
                                        config.removeAlly.name.replace("<name>", targetAllyProfile.getName()),
                                        config.removeAlly.lore, (p, e) -> {
                                                p.closeInventory();
                                                ConfirmationGUI.open(p, "<red>Remove Alliance?", () -> {
                                                        // Confirm
                                                        profile.removeAlly(targetAllyProfile.getOwnerId());
                                                        targetAllyProfile.removeAlly(profile.getOwnerId());
                                                        plugin.getClaimManager().saveAndSync(profile);
                                                        plugin.getClaimManager().saveAndSync(targetAllyProfile);
                                                        p.sendMessage(plugin.getConfigManager().getMessage(
                                                                        "ally-removed", "<name>",
                                                                        targetAllyProfile.getName()));
                                                        AllyManagementGUI.open(p, profile, plugin);
                                                }, () -> {
                                                        // Cancel
                                                        AllyControlPanelGUI.open(p, profile, plugin, targetAllyProfile);
                                                });
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                AllyManagementGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM
                                        .deserialize(config.title.replace("<name>", targetAllyProfile.getName()));
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
