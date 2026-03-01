package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.AllyControlPanelConfig;
import net.kyori.adventure.text.Component;

public class AllyControlPanelGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AllyControlPanelConfig config = plugin.getConfigManager().getAllyControlPanelConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F P F W F R F F",
                            "F F F F F F F F F",
                            "S S S S < S S S S")
                    .addIngredient('F',
                            GuiHelper.buildItem(config.frame.material, config.frame.name, config.frame.lore))
                    .addIngredient('S',
                            GuiHelper.buildItem(config.spacer.material, config.spacer.name, config.spacer.lore))
                    .addIngredient('P', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.allyPermissions.material,
                                    config.allyPermissions.name, config.allyPermissions.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                AllyPremissionsGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('W', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.allyWarps.material,
                                    config.allyWarps.name, config.allyWarps.lore))
                            .addClickHandler(click -> {
                                // No action (coming later)
                            }).build())
                    .addIngredient('R', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.removeAlly.material,
                                    config.removeAlly.name, config.removeAlly.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens confirmation GUI
                            }).build())
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                AllyManagementGUI.open(player, claim, plugin);
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
