package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.PlayerControlPanelConfig;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin, UUID targetPlayerId,
            String targetPlayerName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerControlPanelConfig config = plugin.getConfigManager().getPlayerControlPanelConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F C F T F K F F",
                            "F F F F F F F F F",
                            ". . . A B A . . .")
                    .addIngredient('F', buildPlayerItem(config.frame, targetPlayerName))
                    .addIngredient('A', buildPlayerItem(config.accent, targetPlayerName))
                    .addIngredient('C', Item.builder()
                            .setItemProvider(buildPlayerItemBuilder(config.changeRole, targetPlayerName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                RoleSelectionGUI.open(player, claim, plugin, targetPlayerId, targetPlayerName);
                            }).build())
                    .addIngredient('T', Item.builder()
                            .setItemProvider(buildPlayerItemBuilder(config.transferOwnership, targetPlayerName))
                            .addClickHandler(click -> {
                                // Backend logic for ownership transfer to follow
                            }).build())
                    .addIngredient('K', Item.builder()
                            .setItemProvider(buildPlayerItemBuilder(config.kickPlayer, targetPlayerName))
                            .addClickHandler(click -> {
                                // Opens later confirmation to remove member
                            }).build())
                    .addIngredient('B', Item.builder()
                            .setItemProvider(buildPlayerItemBuilder(config.back, targetPlayerName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                MemberManagementGUI.open(player, claim, plugin);
                            }).build())
                    .build();

            String windowTitle = config.title.replace("<Player>", targetPlayerName);
            Component title = GuiHelper.MM.deserialize(windowTitle);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Window.builder().setTitle(title).setUpperGui(gui).open(player);
                }
            });
        });
    }

    private static Item buildPlayerItem(PlayerControlPanelConfig.ItemConfig itemConfig, String targetName) {
        String name = itemConfig.name != null ? itemConfig.name.replace("<Player>", targetName) : null;
        List<String> lore = null;
        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            lore = new ArrayList<>(itemConfig.lore.size());
            for (String line : itemConfig.lore) {
                lore.add(line.replace("<Player>", targetName));
            }
        }
        return GuiHelper.buildItem(itemConfig.material, name, lore);
    }

    private static xyz.xenondevs.invui.item.ItemBuilder buildPlayerItemBuilder(
            PlayerControlPanelConfig.ItemConfig itemConfig, String targetName) {
        String name = itemConfig.name != null ? itemConfig.name.replace("<Player>", targetName) : null;
        List<String> lore = null;
        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            lore = new ArrayList<>(itemConfig.lore.size());
            for (String line : itemConfig.lore) {
                lore.add(line.replace("<Player>", targetName));
            }
        }
        return GuiHelper.buildItemBuilder(itemConfig.material, name, lore);
    }
}
