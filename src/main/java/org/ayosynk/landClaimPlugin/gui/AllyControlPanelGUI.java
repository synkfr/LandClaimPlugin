package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.AllyControlPanelConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class AllyControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        AllyControlPanelConfig config = plugin.getConfigManager().getAllyControlPanelConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui gui = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F F P F W F R F F",
                        "F F F F F F F F F",
                        "S S S S < S S S S")
                .addIngredient('F', buildConfigItem(config.frame))
                .addIngredient('S', buildConfigItem(config.spacer))
                .addIngredient('P', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.allyPermissions))
                        .addClickHandler(click -> {
                            // Opens AllyPermissionsGUI (future)
                        }).build())
                .addIngredient('W', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.allyWarps))
                        .addClickHandler(click -> {
                            // No action (coming later)
                        }).build())
                .addIngredient('R', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.removeAlly))
                        .addClickHandler(click -> {
                            // Reserved: Opens confirmation GUI
                        }).build())
                .addIngredient('<', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            AllyManagementGUI.open(player, claim, plugin);
                        }).build())
                .build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(AllyControlPanelConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(AllyControlPanelConfig.ItemConfig itemConfig) {
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
            Component comp = mm.deserialize(itemConfig.name);
            builder.setCustomName(comp);
        }

        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : itemConfig.lore) {
                Component comp = mm.deserialize(line);
                lore.add(comp);
            }
            builder.setLore(lore);
        }

        return builder;
    }
}
