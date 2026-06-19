package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.PlayerControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerControlPanelGUI {

    public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, UUID targetPlayerId,
            String targetPlayerName) {
        FoliaScheduler.runAsync(plugin, () -> {
            PlayerControlPanelConfig config = plugin.getConfigManager().getPlayerControlPanelConfig();

            String[] structure = {
                    "F F F F F F F F F",
                    "F F C F T F K F F",
                    "F F F F F F F F F",
                    ". . . X B B B . ."
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', buildPlayerSlot(config.frame, targetPlayerName));
            ingredients.put('C', buildPlayerSlotWithAction(config.changeRole, targetPlayerName, (p, e) -> {
                p.closeInventory();
                RoleSelectionGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName);
            }));
            ingredients.put('T', buildPlayerSlotWithAction(config.transferOwnership, targetPlayerName, (p, e) -> {
                // Transfer Ownership — only the actual owner can transfer
                if (!profile.isOwner(p.getUniqueId())) {
                    p.sendMessage(GuiHelper.MM.deserialize("<red>Only the claim owner can transfer ownership."));
                    return;
                }
                FoliaScheduler.runTask(plugin, () -> {
                    ConfirmationGUI.open(p, "<red>Transfer to " + targetPlayerName + "?",
                            () -> {
                                plugin.getClaimManager().transferOwnership(profile.getProfileId(), targetPlayerId);
                                p.sendMessage(GuiHelper.MM
                                        .deserialize("<green>Ownership transferred to " + targetPlayerName + "."));
                            },
                            () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
                });
            }));
            ingredients.put('K', buildPlayerSlotWithAction(config.kickPlayer, targetPlayerName, (p, e) -> {
                // Kick Member
                FoliaScheduler.runTask(plugin, () -> {
                    ConfirmationGUI.open(p, "<red>Kick " + targetPlayerName + " from claim?",
                            () -> {
                                profile.removeMember(targetPlayerId);
                                plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                                plugin.getClaimManager().saveAndSync(profile);
                                p.sendMessage(GuiHelper.MM
                                        .deserialize("<yellow>Kicked " + targetPlayerName + " from the claim."));
                                MemberManagementGUI.open(p, profile, plugin);
                            },
                            () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
                });
            }));
            ingredients.put('X', buildPlayerSlotWithAction(config.banPlayer, targetPlayerName, (p, e) -> {
                // Ban Player — confirmed in a ConfirmationGUI, then handed to BanCommand so
                // the same Bedrock-form path is used and the player is ejected if online.
                if (profile.isBanned(targetPlayerId)) {
                    p.sendMessage(GuiHelper.MM.deserialize("<red>That player is already banned."));
                    return;
                }
                FoliaScheduler.runTask(plugin, () -> {
                    ConfirmationGUI.open(p, "<dark_red>Ban " + targetPlayerName + " from your claim?",
                            () -> {
                                org.ayosynk.landClaimPlugin.commands.BanCommand banCmd =
                                        new org.ayosynk.landClaimPlugin.commands.BanCommand(plugin,
                                                plugin.getClaimManager(), plugin.getConfigManager());
                                banCmd.applyBan(p, profile, targetPlayerId, targetPlayerName);
                            },
                            () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
                });
            }));
            ingredients.put('B', buildPlayerSlotWithAction(config.back, targetPlayerName, (p, e) -> {
                p.closeInventory();
                MemberManagementGUI.open(p, profile, plugin);
            }));

            // Handle the A slots that were removed in structure mapping by filling them
            // with F
            ingredients.put('.', buildPlayerSlot(config.frame, targetPlayerName));

            String windowTitle = config.title.replace("<Player>", targetPlayerName);
            Component title = GuiHelper.MM.deserialize(windowTitle);

            CustomGui gui = new CustomGui(title, 4);
            gui.fillFromStructure(structure, ingredients);
            gui.open(player);
        });
    }

    private static SlotDefinition buildPlayerSlot(PlayerControlPanelConfig.ItemConfig itemConfig, String targetName) {
        String name = itemConfig.name != null ? itemConfig.name.replace("<Player>", targetName) : null;
        List<String> lore = null;
        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            lore = new ArrayList<>(itemConfig.lore.size());
            for (String line : itemConfig.lore) {
                lore.add(line.replace("<Player>", targetName));
            }
        }
        return GuiHelper.buildSlot(itemConfig.material, name, lore);
    }

    private static SlotDefinition buildPlayerSlotWithAction(PlayerControlPanelConfig.ItemConfig itemConfig,
            String targetName, org.ayosynk.landClaimPlugin.gui.framework.ClickAction action) {
        String name = itemConfig.name != null ? itemConfig.name.replace("<Player>", targetName) : null;
        List<String> lore = null;
        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            lore = new ArrayList<>(itemConfig.lore.size());
            for (String line : itemConfig.lore) {
                lore.add(line.replace("<Player>", targetName));
            }
        }
        return GuiHelper.buildSlot(itemConfig.material, name, lore, action);
    }
}
