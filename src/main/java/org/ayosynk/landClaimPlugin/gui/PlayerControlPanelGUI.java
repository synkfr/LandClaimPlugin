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
        PlayerControlPanelConfig config = plugin.getConfigManager().getPlayerControlPanelConfig();

        String[] structure = {
                "F F F F F F F F F",
                "F F C F T F K F F",
                "F F F F F F F F F",
                "A A A X A B A A A"
        };

        Map<Character, SlotDefinition> ingredients = new HashMap<>();
        ingredients.put('F', buildPlayerSlot(config.frame, targetPlayerName));
        ingredients.put('A', buildPlayerSlot(config.accent, targetPlayerName));
        ingredients.put('C', buildPlayerSlotWithAction(config.changeRole, targetPlayerName,
                (p, e) -> RoleSelectionGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName)));
        ingredients.put('T', buildPlayerSlotWithAction(config.transferOwnership, targetPlayerName, (p, e) -> {
            var cm = plugin.getConfigManager();
            if (!profile.isOwner(p.getUniqueId())) {
                p.sendMessage(cm.getMessage("gui.only-owner-transfer"));
                return;
            }
            ConfirmationGUI.open(p, cm.getRawTemplate("gui.confirm-transfer", "<player>", targetPlayerName),
                    () -> {
                        plugin.getClaimManager().transferOwnership(profile.getProfileId(), targetPlayerId);
                        p.sendMessage(cm.getMessage("gui.ownership-transferred", "<player>", targetPlayerName));
                    },
                    () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
        }));
        ingredients.put('K', buildPlayerSlotWithAction(config.kickPlayer, targetPlayerName, (p, e) -> {
            var cm = plugin.getConfigManager();
            ConfirmationGUI.open(p, cm.getRawTemplate("gui.confirm-kick", "<player>", targetPlayerName),
                    () -> {
                        profile.removeMember(targetPlayerId);
                        plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                        plugin.getClaimManager().saveAndSync(profile);
                        p.sendMessage(cm.getMessage("members.member-kicked", "<player>", targetPlayerName));
                        MemberManagementGUI.open(p, profile, plugin);
                    },
                    () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
        }));
        ingredients.put('X', buildPlayerSlotWithAction(config.banPlayer, targetPlayerName, (p, e) -> {
            var cm = plugin.getConfigManager();
            if (profile.isBanned(targetPlayerId)) {
                p.sendMessage(cm.getMessage("bans.already-banned", "<player>", targetPlayerName));
                return;
            }
            ConfirmationGUI.open(p, cm.getRawTemplate("gui.confirm-ban", "<player>", targetPlayerName),
                    () -> {
                        org.ayosynk.landClaimPlugin.commands.BanCommand banCmd =
                                new org.ayosynk.landClaimPlugin.commands.BanCommand(plugin,
                                        plugin.getClaimManager(), cm);
                        banCmd.applyBan(p, profile, targetPlayerId, targetPlayerName);
                    },
                    () -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName));
        }));
        ingredients.put('B', buildPlayerSlotWithAction(config.back, targetPlayerName,
                (p, e) -> MemberManagementGUI.open(p, profile, plugin)));

        String windowTitle = config.title.replace("<Player>", targetPlayerName);
        Component title = GuiHelper.MM.deserialize(windowTitle);

        CustomGui gui = new CustomGui(title, 4);
        gui.fillFromStructure(structure, ingredients);
        gui.open(player);
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
