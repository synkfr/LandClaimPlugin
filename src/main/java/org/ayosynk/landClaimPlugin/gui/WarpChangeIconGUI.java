package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.WarpChangeIconConfig;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class WarpChangeIconGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            WarpChangeIconConfig config = plugin.getConfigManager().getWarpChangeIconConfig();

            List<Item> contentItems = new ArrayList<>();

            BoundItem backBtn = BoundItem.pagedBuilder()
                    .setItemProvider((p, gui) -> {
                        if (gui.getPage() > 0) {
                            return GuiHelper.buildItemBuilder(config.previousPage.material,
                                    config.previousPage.name, config.previousPage.lore);
                        } else {
                            return GuiHelper.buildItemBuilder(config.navFill.material, config.navFill.name,
                                    config.navFill.lore);
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
                            return GuiHelper.buildItemBuilder(config.navFill.material, config.navFill.name,
                                    config.navFill.lore);
                        }
                    })
                    .addClickHandler((item, gui, click) -> {
                        if (gui.getPage() < gui.getPageCount() - 1)
                            gui.setPage(gui.getPage() + 1);
                    })
                    .build();

            Gui gui = PagedGui.itemsBuilder()
                    .setStructure(
                            "x x x x x x x x x",
                            "x x x x x x x x x",
                            "x x x x x x x x x",
                            "P F F S < S F F N")
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('F',
                            GuiHelper.buildItem(config.navFill.material, config.navFill.name, config.navFill.lore))
                    .addIngredient('S',
                            GuiHelper.buildItem(config.spacer.material, config.spacer.name, config.spacer.lore))
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                WarpControlPanelGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('P', backBtn)
                    .addIngredient('N', forwardBtn)
                    .setContent(contentItems)
                    .build();

            Component title = GuiHelper.MM.deserialize(config.title);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Window.builder().setTitle(title).setUpperGui(gui).open(player);
                }
            });
        });
    }
}
