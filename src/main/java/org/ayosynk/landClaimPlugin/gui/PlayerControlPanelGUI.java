package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.PlayerControlPanelConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin, UUID targetPlayerId,
            String targetPlayerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerControlPanelConfig config = plugin.getConfigManager().getPlayerControlPanelConfig();

            String[] structure = {
                    "F F F F F F F F F",
                    "F F C F T F K F F",
                    "F F F F F F F F F",
                    ". . . A B A . . ."
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', buildPlayerSlot(config.frame, targetPlayerName));
            ingredients.put('A', buildPlayerSlot(config.accent, targetPlayerName));
            ingredients.put('C', buildPlayerSlotWithAction(config.changeRole, targetPlayerName, (p, e) -> {
                p.closeInventory();
                RoleSelectionGUI.open(p, claim, plugin, targetPlayerId, targetPlayerName);
            }));
            ingredients.put('T', buildPlayerSlotWithAction(config.transferOwnership, targetPlayerName, (p, e) -> {
                // Backend logic for ownership transfer to follow
            }));
            ingredients.put('K', buildPlayerSlotWithAction(config.kickPlayer, targetPlayerName, (p, e) -> {
                // Opens later confirmation to remove member
            }));
            ingredients.put('B', buildPlayerSlotWithAction(config.back, targetPlayerName, (p, e) -> {
                p.closeInventory();
                MemberManagementGUI.open(p, claim, plugin);
            }));

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
