package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.MainMenuConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MainMenuGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "main", plugin)) {
                        return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String ownerName = profile.getProfileId() != null
                                        ? profile.getDisplayOwnerName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = profile.getName() != null ? profile.getName() : "Unnamed Claim";

                        MainMenuConfig config = plugin.getConfigManager().getMainMenuConfig();

                        String[] structure = {
                                        "1 1 2 2 2 2 2 1 1",
                                        "1 M W A S T E V 1",
                                        "2 2 2 2 2 2 2 2 2",
                                        "2 2 2 1 X 1 2 2 2"
                        };

                        boolean canManage = profile.canManage(player);
                        boolean canManageSettings = canManage || org.ayosynk.landClaimPlugin.managers.PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_SETTINGS");
                        boolean canManageRoles = canManage || org.ayosynk.landClaimPlugin.managers.PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_ROLES");
                        boolean canManageMembers = canManage || org.ayosynk.landClaimPlugin.managers.PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_MEMBERS");

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('1', GuiHelper.buildSlot(config.filler1.material, config.filler1.name,
                                        config.filler1.lore, profile, player, ownerName, claimName));
                        ingredients.put('2', GuiHelper.buildSlot(config.filler2.material, config.filler2.name,
                                        config.filler2.lore, profile, player, ownerName, claimName));
                        ingredients.put('M', GuiHelper.buildSlot(config.claimMap.material, config.claimMap.name,
                                        config.claimMap.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ClaimMapGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('W', GuiHelper.buildSlot(config.warps.material, config.warps.name,
                                        config.warps.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                WarpManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('A', GuiHelper.buildSlot(config.allies.material, config.allies.name,
                                        config.allies.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                if (!canManageSettings) {
                                                        p.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                                                        return;
                                                }
                                                p.closeInventory();
                                                AllyManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('S', GuiHelper.buildSlot(config.settings.material, config.settings.name,
                                        config.settings.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                // Settings menu itself handles granular permissions, but need MANAGE_SETTINGS or ADMIN_MENU to enter
                                                // Actually ADMIN_MENU is what got them here, so we allow entry.
                                                p.closeInventory();
                                                ClaimSettingsGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('T', GuiHelper.buildSlot(config.trusted.material, config.trusted.name,
                                        config.trusted.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                if (!canManageMembers) {
                                                        p.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                                                        return;
                                                }
                                                p.closeInventory();
                                                TrustManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('E', GuiHelper.buildSlot(config.members.material, config.members.name,
                                        config.members.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                if (!canManageMembers) {
                                                        p.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                                                        return;
                                                }
                                                p.closeInventory();
                                                MemberManagementGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('V', GuiHelper.buildSlot(config.visitors.material, config.visitors.name,
                                        config.visitors.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                if (!canManageSettings) {
                                                        p.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                                                        return;
                                                }
                                                p.closeInventory();
                                                VisitorSettingsGUI.open(p, profile, plugin);
                                        }));
                        ingredients.put('X', GuiHelper.buildSlot(config.close.material, config.close.name,
                                        config.close.lore, profile, player, ownerName, claimName,
                                        (p, e) -> p.closeInventory()));

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
