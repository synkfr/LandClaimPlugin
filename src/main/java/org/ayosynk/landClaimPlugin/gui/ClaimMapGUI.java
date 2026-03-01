package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapConfig;
import net.kyori.adventure.text.Component;

public class ClaimMapGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ClaimMapConfig config = plugin.getConfigManager().getClaimMapConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "W F F F F F F F F",
                            "F F F F F F F F F",
                            "F F F F F F F F F",
                            "F F F F F F F F F",
                            "F F F F F F F F F",
                            "B B R B X B I B B")
                    .addIngredient('W', GuiHelper.buildItem(config.wilderness.material, config.wilderness.name,
                            config.wilderness.lore))
                    .addIngredient('F',
                            GuiHelper.buildItem(config.mapFill.material, config.mapFill.name, config.mapFill.lore))
                    .addIngredient('B', GuiHelper.buildItem(config.bottomFill.material, config.bottomFill.name,
                            config.bottomFill.lore))
                    .addIngredient('R',
                            GuiHelper.buildItem(config.refresh.material, config.refresh.name, config.refresh.lore))
                    .addIngredient('X', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                MainMenuGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('I', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.info.material, config.info.name,
                                    config.info.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                ClaimMapInfoGUI.open(player, claim, plugin);
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
