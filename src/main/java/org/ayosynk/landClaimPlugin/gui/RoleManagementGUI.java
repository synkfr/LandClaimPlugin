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
import org.ayosynk.landClaimPlugin.config.menus.RoleManagementConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class RoleManagementGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        RoleManagementConfig config = plugin.getConfigManager().getRoleManagementConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        List<Item> contentItems = new ArrayList<>();
        // Backend logic for roles later:
        // When iterating over roles, build an Item for each role.
        // - Left Click -> RoleEditGUI.open(player, claim, role, plugin); (future)
        // - Right Click -> Delete (future)

        BoundItem backBtn = BoundItem.pagedBuilder()
                .setItemProvider((p, gui) -> {
                    if (gui.getPage() > 0) {
                        return buildConfigItemBuilder(config.previousPage);
                    } else {
                        return buildConfigItemBuilder(config.navFill);
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
                        return buildConfigItemBuilder(config.navFill);
                    }
                })
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() < gui.getPageCount() - 1)
                        gui.setPage(gui.getPage() + 1);
                })
                .build();

        Gui gui = PagedGui.itemsBuilder()
                .setStructure(
                        "F F F F F F F F F",
                        "F x x x x x x x F",
                        "F x x x x x x x F",
                        "P N N < C N N N V")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('F', buildConfigItem(config.frame))
                .addIngredient('N', buildConfigItem(config.navFill))
                .addIngredient('C', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.createRole))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            RoleSetupGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('<', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            ClaimSettingsGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('P', backBtn)
                .addIngredient('V', forwardBtn)
                .setContent(contentItems)
                .build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(RoleManagementConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(RoleManagementConfig.ItemConfig itemConfig) {
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
