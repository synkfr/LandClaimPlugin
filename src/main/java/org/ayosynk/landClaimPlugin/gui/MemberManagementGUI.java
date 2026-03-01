package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.MemberManagementConfig;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class MemberManagementGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MemberManagementConfig config = plugin.getConfigManager().getMemberManagementConfig();

            List<Item> contentItems = new ArrayList<>();

            BoundItem backBtn = BoundItem.pagedBuilder()
                    .setItemProvider((p, gui) -> {
                        if (gui.getPage() > 0) {
                            return GuiHelper.buildItemBuilder(config.previousPage.material,
                                    config.previousPage.name, config.previousPage.lore);
                        } else {
                            return GuiHelper.buildItemBuilder(config.bottomFill.material, config.bottomFill.name,
                                    config.bottomFill.lore);
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
                            return GuiHelper.buildItemBuilder(config.nextPage.material, config.nextPage.name,
                                    config.nextPage.lore);
                        } else {
                            return GuiHelper.buildItemBuilder(config.bottomFill.material, config.bottomFill.name,
                                    config.bottomFill.lore);
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
                        .addIngredient('F', GuiHelper.buildItem(config.frame.material, config.frame.name,
                                config.frame.lore))
                        .addIngredient('E', GuiHelper.buildItem(config.emptyIndicator.material,
                                config.emptyIndicator.name, config.emptyIndicator.lore))
                        .addIngredient('B', GuiHelper.buildItem(config.bottomFill.material, config.bottomFill.name,
                                config.bottomFill.lore))
                        .addIngredient('<', Item.builder()
                                .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                        config.back.lore))
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
                        .addIngredient('B', GuiHelper.buildItem(config.bottomFill.material, config.bottomFill.name,
                                config.bottomFill.lore))
                        .addIngredient('<', Item.builder()
                                .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                        config.back.lore))
                                .addClickHandler(click -> {
                                    player.closeInventory();
                                    MainMenuGUI.open(player, claim, plugin);
                                }).build())
                        .addIngredient('P', backBtn)
                        .addIngredient('N', forwardBtn)
                        .setContent(contentItems)
                        .build();
            }

            Component title = GuiHelper.MM.deserialize(config.title);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Window.builder().setTitle(title).setUpperGui(gui).open(player);
                }
            });
        });
    }
}
