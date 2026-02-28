package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class VisitorSettingsGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        VisitorSettingsConfig config = plugin.getConfigManager().getVisitorSettingsConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        // The empty area for future permission flags
        List<Item> contentItems = new ArrayList<>();

        BoundItem backBtn = BoundItem.pagedBuilder()
                .setItemProvider((p, gui) -> {
                    if (gui.getPage() > 0) {
                        return buildConfigItemBuilder(config.previousPage);
                    } else {
                        return buildConfigItemBuilder(config.bottomFill);
                    }
                })
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1))
                .build();

        BoundItem forwardBtn = BoundItem.pagedBuilder()
                .setItemProvider((p, gui) -> {
                    if (gui.getPage() < gui.getPageCount() - 1) {
                        return buildConfigItemBuilder(config.nextPage);
                    } else {
                        return buildConfigItemBuilder(config.bottomFill);
                    }
                })
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1))
                .build();

        var guiBuilder = PagedGui.itemsBuilder()
                .setStructure(
                        "F F F F I F F F F",
                        "F x x x x x x x F",
                        "F x x x x x x x F",
                        "F x x x x x x x F",
                        "F x x x x x x x F",
                        "P B B B X B B B N")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('F', buildConfigItem(config.frame))
                .addIngredient('B', buildConfigItem(config.bottomFill))
                .addIngredient('I', buildConfigItem(config.info))
                .addIngredient('P', backBtn)
                .addIngredient('N', forwardBtn)
                .addIngredient('X', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            MainMenuGUI.open(player, claim, plugin);
                        }).build())
                .setContent(contentItems);

        Gui gui = guiBuilder.build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(VisitorSettingsConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(VisitorSettingsConfig.ItemConfig itemConfig) {
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
