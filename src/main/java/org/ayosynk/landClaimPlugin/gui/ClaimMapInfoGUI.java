package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapInfoConfig;
import net.kyori.adventure.text.Component;

public class ClaimMapInfoGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ClaimMapInfoConfig config = plugin.getConfigManager().getClaimMapInfoConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F O A P E W F F",
                            "F F F F F F F F F",
                            "F F F 1 B 1 F F F")
                    .addIngredient('F',
                            GuiHelper.buildItem(config.filler.material, config.filler.name, config.filler.lore))
                    .addIngredient('1',
                            GuiHelper.buildItem(config.filler1.material, config.filler1.name, config.filler1.lore))
                    .addIngredient('O',
                            GuiHelper.buildItem(config.owned.material, config.owned.name, config.owned.lore))
                    .addIngredient('A',
                            GuiHelper.buildItem(config.ally.material, config.ally.name, config.ally.lore))
                    .addIngredient('P',
                            GuiHelper.buildItem(config.other.material, config.other.name, config.other.lore))
                    .addIngredient('E',
                            GuiHelper.buildItem(config.enemy.material, config.enemy.name, config.enemy.lore))
                    .addIngredient('W',
                            GuiHelper.buildItem(config.wilderness.material, config.wilderness.name,
                                    config.wilderness.lore))
                    .addIngredient('B', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                ClaimMapGUI.open(player, claim, plugin);
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
