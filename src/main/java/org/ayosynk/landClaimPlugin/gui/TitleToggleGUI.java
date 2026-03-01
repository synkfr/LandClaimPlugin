package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.TitleSettingsConfig;
import net.kyori.adventure.text.Component;

public class TitleToggleGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TitleSettingsConfig config = plugin.getConfigManager().getTitleSettingsConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F T F E F O F F",
                            "F F F F F F F F F",
                            "F F F S < S F F F")
                    .addIngredient('F',
                            GuiHelper.buildItem(config.frame.material, config.frame.name, config.frame.lore))
                    .addIngredient('S',
                            GuiHelper.buildItem(config.navSpacer.material, config.navSpacer.name,
                                    config.navSpacer.lore))
                    .addIngredient('T', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.titleToggle.material,
                                    config.titleToggle.name, config.titleToggle.lore))
                            .addClickHandler(click -> {
                                // Reserved: Toggle on and off
                            }).build())
                    .addIngredient('E', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.onEntry.material,
                                    config.onEntry.name, config.onEntry.lore))
                            .addClickHandler(click -> {
                                // Reserved: Configure entry title behavior
                            }).build())
                    .addIngredient('O', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.onLeaveTitle.material,
                                    config.onLeaveTitle.name, config.onLeaveTitle.lore))
                            .addClickHandler(click -> {
                                // Reserved: Configure leave title behavior
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
