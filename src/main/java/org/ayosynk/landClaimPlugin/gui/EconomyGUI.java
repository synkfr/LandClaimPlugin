package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

public class EconomyGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        if (!plugin.getConfigManager().getPluginConfig().economyEnabled) {
            player.sendMessage("§cEconomy features are currently disabled.");
            return;
        }

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# . B . S . U . #",
                        "# # # # X # # # #")
                .addIngredient('#', Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setLegacyName(" ")))
                .addIngredient('B', Item.builder().setItemProvider(new ItemBuilder(Material.GOLD_INGOT)
                        .setLegacyName("§6Buy More Allowance")
                        .addLegacyLoreLines("§7Purchase more claiming allowance", "§7using your balance."))
                        .addClickHandler(click -> {
                            // TODO: Add logic for buying allowance
                            player.closeInventory();
                            player.sendMessage("§eComing soon: Buy Allowance");
                        }).build())
                .addIngredient('S', Item.builder().setItemProvider(new ItemBuilder(Material.PAPER)
                        .setLegacyName("§aSell Claim")
                        .addLegacyLoreLines("§7Put this claim up for sale", "§7so others can buy it."))
                        .addClickHandler(click -> {
                            // TODO: Add logic for selling claim
                            player.closeInventory();
                            player.sendMessage("§eComing soon: Sell Claim");
                        }).build())
                .addIngredient('U', Item.builder().setItemProvider(new ItemBuilder(Material.EMERALD)
                        .setLegacyName("§2Pay Taxes / Upkeep")
                        .addLegacyLoreLines("§7Pay your claim's upkeep", "§7to prevent it from expiring."))
                        .addClickHandler(click -> {
                            // TODO: Add logic for taxes
                            player.closeInventory();
                            player.sendMessage("§eComing soon: Pay Taxes");
                        }).build())
                .addIngredient('X', Item.builder().setItemProvider(new ItemBuilder(Material.BARRIER)
                        .setLegacyName("§cBack to Main Menu"))
                        .addClickHandler(click -> MainMenuGUI.open(player, claim, plugin)).build())
                .build();

        Window.builder()
                .setTitle("Economy: " + (claim.getName() != null ? claim.getName() : "Unknown"))
                .setUpperGui(gui)
                .open(player);
    }
}
