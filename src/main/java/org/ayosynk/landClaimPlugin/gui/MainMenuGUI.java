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
                boolean ecoEnabled = plugin.getConfigManager().getPluginConfig().economyEnabled;

                Gui.Builder<?, ?> guiBuilder = Gui.builder()
                                .setStructure(
                                                "# # # # # # # # #",
                                                "# . S . M . E . #",
                                                "# # # # B # # # #")
                                .addIngredient('#',
                                                Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                                                                .setLegacyName(" ")))
                                .addIngredient('S',
                                                Item.builder().setItemProvider(new ItemBuilder(Material.COMMAND_BLOCK)
                                                                .setLegacyName("§aClaim Settings")
                                                                .addLegacyLoreLines(
                                                                                "§7Manage flags, rename, or delete claim"))
                                                                .addClickHandler(click -> {
                                                                        ClaimSettingsGUI.open(player, claim, plugin);
                                                                }).build())
                                .addIngredient('M',
                                                Item.builder().setItemProvider(new ItemBuilder(Material.PLAYER_HEAD)
                                                                .setLegacyName("§eMembers & Roles")
                                                                .addLegacyLoreLines(
                                                                                "§7Manage trusted players and assign roles"))
                                                                .addClickHandler(click -> {
                                                                        MemberListGUI.open(player, claim, plugin);
                                                                }).build())
                                .addIngredient('E', ecoEnabled ? Item.builder().setItemProvider(new ItemBuilder(
                                                Material.GOLD_INGOT)
                                                .setLegacyName("§6Economy Options")
                                                .addLegacyLoreLines("§7Buy allowance, sell claim, pay taxes"))
                                                .addClickHandler(click -> {
                                                        EconomyGUI.open(player, claim, plugin);
                                                }).build()
                                                : Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                                                                .setLegacyName(" ")))
                                .addIngredient('B', Item.builder()
                                                .setItemProvider(new ItemBuilder(Material.BARRIER)
                                                                .setLegacyName("§cClose"))
                                                .addClickHandler(click -> click.player().closeInventory()).build());

                Gui gui = guiBuilder.build();

                Window.builder()
                                .setTitle("Claim: " + (claim.getName() != null ? claim.getName() : "Unknown"))
                                .setUpperGui(gui)
                                .open(player);
        }
}
