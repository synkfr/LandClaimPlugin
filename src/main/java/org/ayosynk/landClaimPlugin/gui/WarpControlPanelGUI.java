package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class WarpControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        WarpControlPanelConfig config = plugin.getConfigManager().getWarpControlPanelConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui gui = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F F L F I F D F F",
                        "F F F F F F F F F",
                        "S S S S < S S S S")
                .addIngredient('F', buildConfigItem(config.frame))
                .addIngredient('S', buildConfigItem(config.spacer))
                .addIngredient('L', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.changeLocation))
                        .addClickHandler(click -> {
                            // Reserved: Update warp to current location
                        }).build())
                .addIngredient('I', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.changeIcon))
                        .addClickHandler(click -> {
                            // Reserved: Select new warp icon
                        }).build())
                .addIngredient('D', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.deleteWarp))
                        .addClickHandler(click -> {
                            // Reserved: Opens confirmation GUI
                        }).build())
                .addIngredient('<', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            WarpManagementGUI.open(player, claim, plugin);
                        }).build())
                .build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(WarpControlPanelConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(WarpControlPanelConfig.ItemConfig itemConfig) {
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
