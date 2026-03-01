package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ChangeClaimColorConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ChangeClaimColorGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        ChangeClaimColorConfig config = plugin.getConfigManager().getChangeClaimColorConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui gui = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F 0 1 2 3 4 5 6 F",
                        "F 7 8 9 A B C D F",
                        "F F F E X Y F F F",
                        "F F F N < N F F F")
                .addIngredient('F', buildConfigItem(config.frameFill))
                .addIngredient('N', buildConfigItem(config.navFrame))
                .addIngredient('<', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            ClaimSettingsGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('X', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.customColor))
                        .addClickHandler(click -> {
                            // Reserved: Opens chat/anvil HEX input
                        }).build())
                .addIngredient('0', buildColorItem(config.colorBlack))
                .addIngredient('1', buildColorItem(config.colorBlue))
                .addIngredient('2', buildColorItem(config.colorBrown))
                .addIngredient('3', buildColorItem(config.colorCyan))
                .addIngredient('4', buildColorItem(config.colorGray))
                .addIngredient('5', buildColorItem(config.colorGreen))
                .addIngredient('6', buildColorItem(config.colorLightBlue))
                .addIngredient('7', buildColorItem(config.colorLime))
                .addIngredient('8', buildColorItem(config.colorLightGray))
                .addIngredient('9', buildColorItem(config.colorMagenta))
                .addIngredient('A', buildColorItem(config.colorOrange))
                .addIngredient('B', buildColorItem(config.colorPink))
                .addIngredient('C', buildColorItem(config.colorPurple))
                .addIngredient('D', buildColorItem(config.colorRed))
                .addIngredient('E', buildColorItem(config.colorWhite))
                .addIngredient('Y', buildColorItem(config.colorYellow))
                .build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildColorItem(ChangeClaimColorConfig.ItemConfig itemConfig) {
        return Item.builder()
                .setItemProvider(buildConfigItemBuilder(itemConfig))
                .addClickHandler(click -> {
                    // Reserved: Instantly selects color
                }).build();
    }

    private static Item buildConfigItem(ChangeClaimColorConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(ChangeClaimColorConfig.ItemConfig itemConfig) {
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
