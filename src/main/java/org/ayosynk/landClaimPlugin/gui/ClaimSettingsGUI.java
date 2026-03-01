package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ClaimSettingsConfig;
import net.kyori.adventure.text.Component;

public class ClaimSettingsGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String ownerName = claim.getOwnerId() != null
                    ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                    : "Unknown";
            if (ownerName == null)
                ownerName = "Unknown";
            String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

            ClaimSettingsConfig config = plugin.getConfigManager().getClaimSettingsConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "1 1 2 2 O 2 2 1 1",
                            "1 2 2 2 2 2 2 2 1",
                            "2 N C R W V T A 2",
                            "1 2 2 2 2 2 2 2 1",
                            "1 1 2 2 B 2 2 1 1")
                    .addIngredient('1',
                            GuiHelper.buildItem(config.filler1.material, config.filler1.name, config.filler1.lore,
                                    claim, player, ownerName, claimName))
                    .addIngredient('2',
                            GuiHelper.buildItem(config.filler2.material, config.filler2.name, config.filler2.lore,
                                    claim, player, ownerName, claimName))
                    .addIngredient('O',
                            GuiHelper.buildItem(config.overview.material, config.overview.name, config.overview.lore,
                                    claim, player, ownerName, claimName))
                    .addIngredient('N', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.rename.material, config.rename.name,
                                    config.rename.lore, claim, player, ownerName, claimName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                RenameClaimGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('C', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.color.material, config.color.name,
                                    config.color.lore, claim, player, ownerName, claimName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                ChangeClaimColorGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('R', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.roles.material, config.roles.name,
                                    config.roles.lore, claim, player, ownerName, claimName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                RoleManagementGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('W',
                            GuiHelper.buildItem(config.warps.material, config.warps.name, config.warps.lore,
                                    claim, player, ownerName, claimName))
                    .addIngredient('V',
                            GuiHelper.buildItem(config.visibility.material, config.visibility.name,
                                    config.visibility.lore, claim, player, ownerName, claimName))
                    .addIngredient('T', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.titleToggle.material,
                                    config.titleToggle.name, config.titleToggle.lore, claim, player, ownerName,
                                    claimName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                TitleToggleGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('A',
                            GuiHelper.buildItem(config.abandonAll.material, config.abandonAll.name,
                                    config.abandonAll.lore, claim, player, ownerName, claimName))
                    .addIngredient('B', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore, claim, player, ownerName, claimName))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                MainMenuGUI.open(player, claim, plugin);
                            }).build())
                    .build();

            String windowTitle = config.title.replace("{claim_name}", claimName);
            Component title = GuiHelper.MM.deserialize(windowTitle);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    Window.builder().setTitle(title).setUpperGui(gui).open(player);
                }
            });
        });
    }
}
