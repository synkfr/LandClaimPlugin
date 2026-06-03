package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoleSetupGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Role role) {
                FoliaScheduler.runAsync(plugin, () -> {
                        RoleSetupConfig config = plugin.getConfigManager().getRoleSetupConfig();

                        // Setup a temporary role representation if creating new
                        final Role workingRole;
                        final boolean isNew;
                        if (role == null) {
                                isNew = true;
                                workingRole = new Role(UUID.randomUUID(), profile.getProfileId(), "NewRole", 0);
                        } else {
                                isNew = false;
                                workingRole = role; // Edit existing by reference
                        }

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

                        ingredients.put('N', GuiHelper.buildSlot(config.setName.material,
                                        config.setName.name.replace("<name>", workingRole.getName()),
                                        config.setName.lore, (p, e) -> {
                                                p.closeInventory();
                                                AnvilInputGUI.open(plugin, p, "Role Name", workingRole.getName(), input -> {
                                                        FoliaScheduler.runTask(plugin, () -> {
                                                                if (input == null) {
                                                                        p.sendMessage(GuiHelper.MM.deserialize("<red>Operation cancelled."));
                                                                } else {
                                                                        String cleanName = input.replaceAll("[^a-zA-Z0-9_-]", "");
                                                                        if (!cleanName.isEmpty()) {
                                                                                workingRole.setName(cleanName);
                                                                                p.sendMessage(GuiHelper.MM.deserialize("<green>Role name updated."));
                                                                        } else {
                                                                                p.sendMessage(GuiHelper.MM.deserialize("<red>Invalid name."));
                                                                        }
                                                                }
                                                                RoleSetupGUI.open(p, profile, plugin, workingRole);
                                                        });
                                                });
                                        }));

                        ingredients.put('P', GuiHelper.buildSlot(config.permissions.material, config.permissions.name,
                                        config.permissions.lore, (p, e) -> {
                                                p.closeInventory();
                                                RoleEditGUI.open(p, profile, plugin, workingRole, isNew);
                                        }));

                        ingredients.put('T', GuiHelper.buildSlot(config.setPriority.material, config.setPriority.name,
                                        config.setPriority.lore, (p, e) -> {
                                                p.closeInventory();
                                                AnvilInputGUI.open(plugin, p, "Priority", String.valueOf(workingRole.getPriority()), input -> {
                                                        FoliaScheduler.runTask(plugin, () -> {
                                                                if (input == null) {
                                                                        p.sendMessage(GuiHelper.MM.deserialize("<red>Operation cancelled."));
                                                                } else {
                                                                        try {
                                                                                int prio = Integer.parseInt(input);
                                                                                workingRole.setPriority(prio);
                                                                                p.sendMessage(GuiHelper.MM.deserialize("<green>Role priority updated."));
                                                                        } catch (NumberFormatException ex) {
                                                                                p.sendMessage(GuiHelper.MM.deserialize("<red>Priority must be a number."));
                                                                        }
                                                                }
                                                                RoleSetupGUI.open(p, profile, plugin, workingRole);
                                                        });
                                                });
                                        }));

                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                RoleManagementGUI.open(p, profile, plugin);
                                                        }));

                        ingredients.put('S', GuiHelper.buildSlot(config.saveExit.material, config.saveExit.name,
                                        config.saveExit.lore, (p, e) -> {
                                                if (isNew) {
                                                        profile.addRole(workingRole);
                                                }

                                                plugin.getCacheManager().getProfileCache().put(profile.getProfileId(),
                                                                profile);
                                                plugin.getClaimManager().saveAndSync(profile);

                                                p.sendMessage(GuiHelper.MM
                                                                .deserialize("<green>Role saved successfully!"));
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
