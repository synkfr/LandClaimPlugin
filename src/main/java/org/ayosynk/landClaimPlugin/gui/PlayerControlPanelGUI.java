package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.PlayerControlPanelConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin, UUID targetPlayerId,
            String targetPlayerName) {
        PlayerControlPanelConfig config = plugin.getConfigManager().getPlayerControlPanelConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui gui = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F F C F T F K F F",
                        "F F F F F F F F F",
                        ". . . A B A . . .")
                .addIngredient('F', buildConfigItem(config.frame, targetPlayerName))
                .addIngredient('A', buildConfigItem(config.accent, targetPlayerName))
                .addIngredient('C', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.changeRole, targetPlayerName))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            RoleSelectionGUI.open(player, claim, plugin, targetPlayerId, targetPlayerName);
                        }).build())
                .addIngredient('T', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.transferOwnership, targetPlayerName))
                        .addClickHandler(click -> {
                            // Backend logic for ownership transfer to follow
                        }).build())
                .addIngredient('K', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.kickPlayer, targetPlayerName))
                        .addClickHandler(click -> {
                            // Opens later confirmation to remove member
                        }).build())
                .addIngredient('B', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back, targetPlayerName))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            MemberManagementGUI.open(player, claim, plugin);
                        }).build())
                .build();

        String windowTitle = config.title.replace("<Player>", targetPlayerName);

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(PlayerControlPanelConfig.ItemConfig itemConfig, String targetName) {
        return Item.simple(buildConfigItemBuilder(itemConfig, targetName));
    }

    private static ItemBuilder buildConfigItemBuilder(PlayerControlPanelConfig.ItemConfig itemConfig,
            String targetName) {
        Material mat = Material.matchMaterial(itemConfig.material.toUpperCase());
        if (mat == null)
            mat = Material.STONE;

        ItemBuilder builder = new ItemBuilder(mat);
        builder.addModifier(item -> {
            item.editMeta(meta -> {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.values());
                try {
                    meta.setAttributeModifiers(com.google.common.collect.LinkedListMultimap.create());
                } catch (Exception ignored) {
                }
            });
            return item;
        });
        MiniMessage mm = MiniMessage.miniMessage();

        if (itemConfig.name != null && !itemConfig.name.isEmpty()) {
            String updatedName = itemConfig.name.replace("<Player>", targetName);
            Component comp = mm.deserialize(updatedName);
            builder.setCustomName(comp);
        }

        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : itemConfig.lore) {
                String updatedLine = line.replace("<Player>", targetName);
                Component comp = mm.deserialize(updatedLine);
                lore.add(comp);
            }
            builder.setLore(lore);
        }

        return builder;
    }
}
