package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

public class MainMenuGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {

                Gui.Builder<?, ?> guiBuilder = Gui.builder()
                                .setStructure(
                                                "# # # # # # # # #",
                                                "# . . . X . . . #",
                                                "# # # # B # # # #")
                                .addIngredient('#',
                                                Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                                                                .setLegacyName(" ")))
                                .addIngredient('X',
                                                Item.simple(new ItemBuilder(Material.BARRIER)
                                                                .setLegacyName("§cComing Soon")
                                                                .addLegacyLoreLines(
                                                                                "§7More features will be added here!")))
                                .addIngredient('B', Item.builder()
                                                .setItemProvider(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                                                                .setLegacyName("§cClose"))
                                                .addClickHandler(click -> click.player().closeInventory()).build());

                Gui gui = guiBuilder.build();

                Window.builder()
                                .setTitle("Claim: " + (claim.getName() != null ? claim.getName() : "Unknown"))
                                .setUpperGui(gui)
                                .open(player);
        }
}
