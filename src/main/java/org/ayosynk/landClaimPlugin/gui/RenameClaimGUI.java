package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.RenameClaimConfig;
import net.kyori.adventure.text.Component;

public class RenameClaimGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RenameClaimConfig config = plugin.getConfigManager().getRenameClaimConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "O O B B B B B O O",
                            "O B B C B R B B O",
                            "B B B B B B B B B",
                            "O S < S B B B B B")
                    .addIngredient('O', GuiHelper.buildItem(config.outerFrame.material, config.outerFrame.name,
                            config.outerFrame.lore))
                    .addIngredient('B', GuiHelper.buildItem(config.background.material, config.background.name,
                            config.background.lore))
                    .addIngredient('S', GuiHelper.buildItem(config.navSpacer.material, config.navSpacer.name,
                            config.navSpacer.lore))
                    .addIngredient('C', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.changeName.material,
                                    config.changeName.name, config.changeName.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens chat input or anvil rename UI
                            }).build())
                    .addIngredient('R', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.resetToDefault.material,
                                    config.resetToDefault.name, config.resetToDefault.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens confirmation GUI
                            }).build())
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                ClaimSettingsGUI.open(player, claim, plugin);
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
