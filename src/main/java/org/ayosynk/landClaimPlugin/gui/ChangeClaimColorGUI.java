package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ChangeClaimColorConfig;
import net.kyori.adventure.text.Component;

public class ChangeClaimColorGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ChangeClaimColorConfig config = plugin.getConfigManager().getChangeClaimColorConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F 0 1 2 3 4 5 6 F",
                            "F 7 8 9 A B C D F",
                            "F F F E X Y F F F",
                            "F F F N < N F F F")
                    .addIngredient('F', GuiHelper.buildItem(config.frameFill.material, config.frameFill.name,
                            config.frameFill.lore))
                    .addIngredient('N', GuiHelper.buildItem(config.navFrame.material, config.navFrame.name,
                            config.navFrame.lore))
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                ClaimSettingsGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('X', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.customColor.material,
                                    config.customColor.name, config.customColor.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens chat/anvil HEX input
                            }).build())
                    .addIngredient('0', buildColorItem(config.colorBlack))
                    .addIngredient('1', buildColorItem(config.colorBlue))
                    .addIngredient('2', buildColorItem(config.colorBrown))
                    .addIngredient('3', buildColorItem(config.colorCyan))
                    .addIngredient('4', buildColorItem(config.colorGray))
                    .addIngredient('5', buildColorItem(config.colorGreen))
                    .addIngredient('6', buildColorItem(config.colorLightBlue))
                    .addIngredient('7', buildColorItem(config.colorLime))
                    .addIngredient('8', buildColorItem(config.colorLightGray))
                    .addIngredient('9', buildColorItem(config.colorMagenta))
                    .addIngredient('A', buildColorItem(config.colorOrange))
                    .addIngredient('B', buildColorItem(config.colorPink))
                    .addIngredient('C', buildColorItem(config.colorPurple))
                    .addIngredient('D', buildColorItem(config.colorRed))
                    .addIngredient('E', buildColorItem(config.colorWhite))
                    .addIngredient('Y', buildColorItem(config.colorYellow))
                    .build();

            Component title = GuiHelper.MM.deserialize(config.title);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Window.builder().setTitle(title).setUpperGui(gui).open(player);
                }
            });
        });
    }

    private static Item buildColorItem(ChangeClaimColorConfig.ItemConfig itemConfig) {
        return Item.builder()
                .setItemProvider(
                        GuiHelper.buildItemBuilder(itemConfig.material, itemConfig.name, itemConfig.lore))
                .addClickHandler(click -> {
                    // Reserved: Instantly selects color
                }).build();
    }
}
