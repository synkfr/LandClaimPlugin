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
import org.ayosynk.landClaimPlugin.config.menus.MemberManagementConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class MemberManagementGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        MemberManagementConfig config = plugin.getConfigManager().getMemberManagementConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        List<Item> contentItems = new ArrayList<>();
        // Backend logic for members later:
        // When iterating over members, build an Item for each PLAYER_HEAD.
        // - Left Click -> Opens future MemberActionGUI
        // - Right Click -> PlayerControlPanelGUI.open(player, claim, plugin,
        // memberUuid, memberName);

        BoundItem backBtn = BoundItem.pagedBuilder()
                .setItemProvider((p, gui) -> {
                    if (gui.getPage() > 0) {
                        return buildConfigItemBuilder(config.previousPage);
                    } else {
                        return buildConfigItemBuilder(config.bottomFill);
                    }
                })
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() > 0)
                        gui.setPage(gui.getPage() - 1);
                })
                .build();

        BoundItem forwardBtn = BoundItem.pagedBuilder()
                .setItemProvider((p, gui) -> {
                    if (gui.getPage() < gui.getPageCount() - 1) {
                        return buildConfigItemBuilder(config.nextPage);
                    } else {
                        return buildConfigItemBuilder(config.bottomFill);
                    }
                })
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() < gui.getPageCount() - 1)
                        gui.setPage(gui.getPage() + 1);
                })
                .build();

        Gui gui;
        if (contentItems.isEmpty()) {
            gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F F F E F F F F",
                            "F F F F F F F F F",
                            "B B B B < B B B B")
                    .addIngredient('F', buildConfigItem(config.frame))
                    .addIngredient('E', buildConfigItem(config.emptyIndicator))
                    .addIngredient('B', buildConfigItem(config.bottomFill))
                    .addIngredient('<', Item.builder()
                            .setItemProvider(buildConfigItemBuilder(config.back))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                MainMenuGUI.open(player, claim, plugin);
                            }).build())
                    .build();
        } else {
            gui = PagedGui.itemsBuilder()
                    .setStructure(
                            "x x x x x x x x x",
                            "x x x x x x x x x",
                            "x x x x x x x x x",
                            "P B B B < B B B N")
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('B', buildConfigItem(config.bottomFill))
                    .addIngredient('<', Item.builder()
                            .setItemProvider(buildConfigItemBuilder(config.back))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                MainMenuGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('P', backBtn)
                    .addIngredient('N', forwardBtn)
                    .setContent(contentItems)
                    .build();
        }

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(MemberManagementConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(MemberManagementConfig.ItemConfig itemConfig) {
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
