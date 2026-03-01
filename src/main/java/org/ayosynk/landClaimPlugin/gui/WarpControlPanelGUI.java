package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.WarpControlPanelConfig;
import net.kyori.adventure.text.Component;

public class WarpControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            WarpControlPanelConfig config = plugin.getConfigManager().getWarpControlPanelConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F L F I F D F F",
                            "F F F F F F F F F",
                            "S S S S < S S S S")
                    .addIngredient('F',
                            GuiHelper.buildItem(config.frame.material, config.frame.name, config.frame.lore))
                    .addIngredient('S',
                            GuiHelper.buildItem(config.spacer.material, config.spacer.name, config.spacer.lore))
                    .addIngredient('L', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.changeLocation.material,
                                    config.changeLocation.name, config.changeLocation.lore))
                            .addClickHandler(click -> {
                                // Reserved: Update warp to current location
                            }).build())
                    .addIngredient('I', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.changeIcon.material,
                                    config.changeIcon.name, config.changeIcon.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                WarpChangeIconGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('D', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.deleteWarp.material,
                                    config.deleteWarp.name, config.deleteWarp.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens confirmation GUI
                            }).build())
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                WarpManagementGUI.open(player, claim, plugin);
                            }).build())
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
