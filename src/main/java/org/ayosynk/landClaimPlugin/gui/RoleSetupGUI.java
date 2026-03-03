package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RoleSetupGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        RoleSetupConfig config = plugin.getConfigManager().getRoleSetupConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F N F P F T F F",
                                        "F F F F F F F F F",
                                        "G G G < G S G G G"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('G', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('N', GuiHelper.buildSlot(config.setName.material, config.setName.name,
                                        config.setName.lore, (p, e) -> {
                                                // Reserved: Opens chat/anvil input for role name
                                        }));
                        ingredients.put('P', GuiHelper.buildSlot(config.permissions.material, config.permissions.name,
                                        config.permissions.lore, (p, e) -> {
                                                p.closeInventory();
                                                // RoleEditGUI.open(player, profile, plugin);
                                        }));
                        ingredients.put('T', GuiHelper.buildSlot(config.setPriority.material, config.setPriority.name,
                                        config.setPriority.lore, (p, e) -> {
                                                // Reserved: Adjust role hierarchy priority
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                RoleManagementGUI.open(p, profile, plugin);
                                                        }));
                        ingredients.put('S', GuiHelper.buildSlot(config.saveExit.material, config.saveExit.name,
                                        config.saveExit.lore, (p, e) -> {
                                                // Reserved: Create role and close setup
                                                p.closeInventory();
                                                RoleManagementGUI.open(p, profile, plugin);
                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
