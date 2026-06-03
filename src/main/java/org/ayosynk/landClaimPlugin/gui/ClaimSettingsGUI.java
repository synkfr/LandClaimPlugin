package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ClaimSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimSettingsGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "settings", plugin)) {
                        return;
                }
                FoliaScheduler.runAsync(plugin, () -> {
                        String ownerName = profile.getProfileId() != null
                                        ? profile.getColoredOwnerName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = profile.getColoredName() != null ? profile.getColoredName() : "Unnamed Claim";

                        ClaimSettingsConfig config = plugin.getConfigManager().getClaimSettingsConfig();

                        String[] structure = {
                                        "1 1 2 2 O 2 2 1 1",
                                        "1 2 2 2 2 2 2 2 1",
                                        "2 N C R W V T A 2",
                                        "1 2 2 2 2 2 2 2 1",
                                        "1 1 2 2 B 2 2 1 1"
                        };

                        boolean canManage = profile.canManage(player);
                        boolean canManageSettings = canManage || org.ayosynk.landClaimPlugin.managers.PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_SETTINGS");
                        boolean canManageRoles = canManage || org.ayosynk.landClaimPlugin.managers.PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_ROLES");

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('1', GuiHelper.buildSlot(config.filler1.material, config.filler1.name,
                                        config.filler1.lore, profile, player, ownerName, claimName));
                        ingredients.put('2', GuiHelper.buildSlot(config.filler2.material, config.filler2.name,
                                        config.filler2.lore, profile, player, ownerName, claimName));
                        ingredients.put('O', GuiHelper.buildSlot(config.overview.material, config.overview.name,
                                        config.overview.lore, profile, player, ownerName, claimName));
                        
                        // Rename: OWNER or ADMIN BYPASS
                        ingredients.put('N', canManage ? GuiHelper.buildSlot(config.rename.material, config.rename.name,
                                        config.rename.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RenameClaimGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        ingredients.put('C', canManageSettings ? GuiHelper.buildSlot(config.color.material, config.color.name,
                                        config.color.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ChangeClaimColorGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        ingredients.put('R', canManageRoles ? GuiHelper.buildSlot(config.roles.material, config.roles.name,
                                        config.roles.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                RoleManagementGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        java.util.List<String> modifiedPvpLore = new java.util.ArrayList<>();
                        for (String line : config.pvpToggle.lore) {
                                modifiedPvpLore.add(line.replace("<pvp_status>", profile.isPvpEnabled() ? "<green>Enabled" : "<red>Disabled"));
                        }
                        ingredients.put('W', canManageSettings ? GuiHelper.buildSlot(config.pvpToggle.material, config.pvpToggle.name,
                                        modifiedPvpLore, profile, player, ownerName, claimName, (p, e) -> {
                                                boolean newState = !profile.isPvpEnabled();
                                                profile.setPvpEnabled(newState);
                                                profile.setPvpTimerEnd(0); // Permanent via GUI
                                                plugin.getDatabaseManager().getProfileDao().saveProfile(profile);

                                                String messageKey = newState ? "pvp-enabled" : "pvp-disabled";
                                                String rawMessage = plugin.getConfigManager().getMessage(messageKey);

                                                // Folia: player.getLocation() requires the player's region thread.
                                                // Dispatch per-player so the location read is safe.
                                                for (Player p2 : Bukkit.getOnlinePlayers()) {
                                                        final Player target = p2;
                                                        FoliaScheduler.runForPlayer(plugin, target, () -> {
                                                                org.ayosynk.landClaimPlugin.models.ChunkPosition pPos =
                                                                        new org.ayosynk.landClaimPlugin.models.ChunkPosition(target.getLocation());
                                                                if (profile.ownsChunk(pPos)) {
                                                                        target.sendMessage(rawMessage);
                                                                }
                                                        });
                                                }

                                                p.closeInventory();
                                                ClaimSettingsGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        ingredients.put('V', canManageSettings ? GuiHelper.buildSlot(config.visibility.material, config.visibility.name,
                                        config.visibility.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                String currentMode = profile.getVisualizationMode();
                                                String newMode = "DISPLAY_ENTITY".equals(currentMode) ? "PARTICLE"
                                                                 : "DISPLAY_ENTITY";
                                                profile.setVisualizationMode(newMode);
                                                plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                                                plugin.getVisualizationManager().invalidateCache(profile.getProfileId());
                                                p.sendMessage(plugin.getConfigManager().getMessage(
                                                                 "visibility-mode-changed", "<mode>", newMode));
                                                p.closeInventory();
                                                ClaimSettingsGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        ingredients.put('T', canManageSettings ? GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                        config.titleToggle.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                TitleToggleGUI.open(p, profile, plugin);
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        // Abandon: OWNER or ADMIN BYPASS
                        ingredients.put('A', canManage ? GuiHelper.buildSlot(config.abandonAll.material, config.abandonAll.name,
                                        config.abandonAll.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                ConfirmationGUI.open(p, "<red>Abandon ALL claims?", () -> {
                                                        plugin.getClaimManager().abandonProfile(profile.getProfileId());
                                                        p.sendMessage(plugin.getConfigManager()
                                                                        .getMessage("profile-abandoned"));
                                                        plugin.getHookManager().refreshMapHooks();
                                                }, () -> ClaimSettingsGUI.open(p, profile, plugin));
                                        }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                        ingredients.put('B', GuiHelper.buildSlot(config.back.material, config.back.name,
                                        config.back.lore, profile, player, ownerName, claimName, (p, e) -> {
                                                p.closeInventory();
                                                MainMenuGUI.open(p, profile, plugin);
                                        }));

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        CustomGui gui = new CustomGui(title, 5);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }
}
