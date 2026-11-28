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
                var cm = plugin.getConfigManager();
                String ownerName = profile.getProfileId() != null
                                ? profile.getColoredOwnerName()
                                : cm.getRawTemplate("gui.default-unknown-owner");
                if (ownerName == null)
                        ownerName = cm.getRawTemplate("gui.default-unknown-owner");
                String claimName = profile.getColoredName() != null
                                ? profile.getColoredName()
                                : cm.getRawTemplate("gui.default-unnamed-claim");

                ClaimSettingsConfig config = cm.getClaimSettingsConfig();

                String[] structure = {
                                "1 1 1 1 O 1 1 1 1",
                                "1 2 2 2 2 2 2 2 1",
                                "2 N C R W V T A 2",
                                "1 2 2 E 2 M 2 2 1",
                                "1 1 1 1 B 1 1 1 1"
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

                ingredients.put('N', canManage ? GuiHelper.buildSlot(config.rename.material, config.rename.name,
                                config.rename.lore, profile, player, ownerName, claimName,
                                (p, e) -> RenameClaimGUI.open(p, profile, plugin))
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('C', canManageSettings ? GuiHelper.buildSlot(config.color.material, config.color.name,
                                config.color.lore, profile, player, ownerName, claimName,
                                (p, e) -> ChangeClaimColorGUI.open(p, profile, plugin))
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('R', canManageRoles ? GuiHelper.buildSlot(config.roles.material, config.roles.name,
                                config.roles.lore, profile, player, ownerName, claimName,
                                (p, e) -> RoleManagementGUI.open(p, profile, plugin))
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                String enabledStr = cm.getRawTemplate("gui.status-enabled");
                String disabledStr = cm.getRawTemplate("gui.status-disabled");

                java.util.List<String> modifiedPvpLore = new java.util.ArrayList<>();
                for (String line : config.pvpToggle.lore) {
                        modifiedPvpLore.add(line.replace("<pvp_status>", profile.isPvpEnabled() ? enabledStr : disabledStr));
                }
                ingredients.put('W', canManageSettings ? GuiHelper.buildSlot(config.pvpToggle.material, config.pvpToggle.name,
                                modifiedPvpLore, profile, player, ownerName, claimName, (p, e) -> {
                                        boolean newState = !profile.isPvpEnabled();
                                        profile.setPvpEnabled(newState);
                                        profile.setPvpTimerEnd(0);
                                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);

                                        String messageKey = newState ? "pvp-enabled" : "pvp-disabled";
                                        Component rawMessage = cm.getMessage(messageKey);

                                        for (Player target : Bukkit.getOnlinePlayers()) {
                                                FoliaScheduler.runForPlayer(plugin, target, () -> {
                                                        org.ayosynk.landClaimPlugin.models.ChunkPosition pPos =
                                                                new org.ayosynk.landClaimPlugin.models.ChunkPosition(target.getLocation());
                                                        if (profile.ownsChunk(pPos)) {
                                                                target.sendMessage(rawMessage);
                                                        }
                                                });
                                        }

                                        ClaimSettingsGUI.open(p, profile, plugin);
                                }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('V', canManageSettings ? GuiHelper.buildSlot(config.visibility.material, config.visibility.name,
                                config.visibility.lore, profile, player, ownerName, claimName, (p, e) -> {
                                        String currentMode = profile.getVisualizationMode();
                                        String newMode = "DISPLAY_ENTITY".equals(currentMode) ? "PARTICLE" : "DISPLAY_ENTITY";
                                        profile.setVisualizationMode(newMode);
                                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                                        plugin.getVisualizationManager().invalidateCache(profile.getProfileId());
                                        if (!p.getUniqueId().equals(profile.getProfileId())) {
                                                plugin.getVisualizationManager().invalidateCache(p.getUniqueId());
                                        }
                                        p.sendMessage(cm.getMessage("visibility-mode-changed", "<mode>", newMode));
                                        ClaimSettingsGUI.open(p, profile, plugin);
                                }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('T', canManageSettings ? GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                                config.titleToggle.lore, profile, player, ownerName, claimName,
                                (p, e) -> TitleToggleGUI.open(p, profile, plugin))
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('E', canManageSettings ? buildToggleSlot(config.tntToggle, "<tnt_status>",
                                profile.isTntEnabled(), () -> profile.setTntEnabled(!profile.isTntEnabled()),
                                () -> profile.isTntEnabled(), "tnt-enabled", "tnt-disabled",
                                profile, player, ownerName, claimName, plugin)
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('M', canManageSettings ? buildToggleSlot(config.mobSpawningToggle, "<mob_spawning_status>",
                                profile.isMobSpawningEnabled(), () -> profile.setMobSpawningEnabled(!profile.isMobSpawningEnabled()),
                                () -> profile.isMobSpawningEnabled(), "mob-spawning-enabled", "mob-spawning-disabled",
                                profile, player, ownerName, claimName, plugin)
                                : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('A', canManage ? GuiHelper.buildSlot(config.abandonAll.material, config.abandonAll.name,
                                config.abandonAll.lore, profile, player, ownerName, claimName, (p, e) -> {
                                        ConfirmationGUI.open(p, cm.getRawTemplate("gui.confirm-abandon-all"), () -> {
                                                int chunksDeleted = plugin.getClaimManager().abandonProfile(profile.getProfileId());
                                                p.sendMessage(cm.getMessage("profile-abandoned", "<chunks>", String.valueOf(chunksDeleted)));
                                                plugin.getHookManager().refreshMapHooks();
                                        }, () -> ClaimSettingsGUI.open(p, profile, plugin));
                                }) : GuiHelper.buildSlot(config.filler2.material, config.filler2.name, config.filler2.lore));

                ingredients.put('B', GuiHelper.buildSlot(config.back.material, config.back.name,
                                config.back.lore, profile, player, ownerName, claimName,
                                (p, e) -> MainMenuGUI.open(p, profile, plugin)));

                String windowTitle = config.title.replace("{claim_name}", claimName);
                Component title = GuiHelper.MM.deserialize(windowTitle);

                CustomGui gui = new CustomGui(title, 5);
                gui.fillFromStructure(structure, ingredients);
                gui.open(player);
        }

        private static SlotDefinition buildToggleSlot(ClaimSettingsConfig.ItemConfig item, String placeholder,
                        boolean initialState, Runnable toggleAction, java.util.function.BooleanSupplier stateSupplier,
                        String enabledKey, String disabledKey, ClaimProfile profile, Player player,
                        String ownerName, String claimName, LandClaimPlugin plugin) {
                java.util.List<String> lore = new java.util.ArrayList<>(item.lore.size());
                String status = plugin.getConfigManager().getRawTemplate(initialState ? "gui.status-enabled" : "gui.status-disabled");
                for (String line : item.lore) {
                        lore.add(line.replace(placeholder, status));
                }
                return GuiHelper.buildSlot(item.material, item.name, lore, profile, player, ownerName, claimName, (p, e) -> {
                        toggleAction.run();
                        plugin.getClaimManager().saveAndSync(profile);
                        p.sendMessage(plugin.getConfigManager().getMessage(stateSupplier.getAsBoolean() ? enabledKey : disabledKey));
                        p.closeInventory();
                        ClaimSettingsGUI.open(p, profile, plugin);
                });
        }
}
