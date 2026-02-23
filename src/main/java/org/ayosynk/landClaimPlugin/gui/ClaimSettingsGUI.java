package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

public class ClaimSettingsGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# . R . F . D . #",
                        "# # # # B # # # #")
                .addIngredient('#', Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setLegacyName(" ")))
                .addIngredient('R', Item.builder().setItemProvider(new ItemBuilder(Material.NAME_TAG)
                        .setLegacyName("§eRename Claim")
                        .addLegacyLoreLines("§7Click to rename your claim."))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            player.sendMessage("§ePlease type the new name in chat (coming soon).");
                            // TODO: Add chat input for renaming claim
                        }).build())
                .addIngredient('F', Item.builder().setItemProvider(new ItemBuilder(Material.OAK_DOOR)
                        .setLegacyName("§aClaim Flags")
                        .addLegacyLoreLines("§7Configure PvP, mob spawning, etc.", "§c(Coming Soon)"))
                        .addClickHandler(click -> {
                            player.sendMessage("§cClaim flags are coming soon!");
                        }).build())
                .addIngredient('D', Item.builder().setItemProvider(new ItemBuilder(Material.RED_WOOL)
                        .setLegacyName("§cDelete Claim")
                        .addLegacyLoreLines("§7Click to permanently delete", "§7this claim."))
                        .addClickHandler(click -> {
                            // TODO: Confirm deletion
                            player.closeInventory();
                            player.sendMessage("§cComing soon: Delete Claim Confirmation");
                        }).build())
                .addIngredient('B', Item.builder()
                        .setItemProvider(new ItemBuilder(Material.BARRIER)
                                .setLegacyName("§cBack"))
                        .addClickHandler(click -> MainMenuGUI.open(player, claim, plugin)).build())
                .build();

        Window.builder()
                .setTitle("Settings: " + (claim.getName() != null ? claim.getName() : "Unknown"))
                .setUpperGui(gui)
                .open(player);
    }
}
