package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.MainMenuConfig;
import net.kyori.adventure.text.Component;

public class MainMenuGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                // Phase 1+2: Async Construction & Binding — offload heavy ItemBuilder +
                // MiniMessage allocation
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        String ownerName = claim.getOwnerId() != null
                                        ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                                        : "Unknown";
                        if (ownerName == null)
                                ownerName = "Unknown";
                        String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

                        MainMenuConfig config = plugin.getConfigManager().getMainMenuConfig();

                        Gui gui = Gui.builder()
                                        .setStructure(
                                                        "1 1 2 2 2 2 2 1 1",
                                                        "1 M W A S T E V 1",
                                                        "2 2 2 2 2 2 2 2 2",
                                                        "2 2 2 1 X 1 2 2 2")
                                        .addIngredient('1',
                                                        GuiHelper.buildItem(config.filler1.material,
                                                                        config.filler1.name, config.filler1.lore,
                                                                        claim, player, ownerName, claimName))
                                        .addIngredient('2',
                                                        GuiHelper.buildItem(config.filler2.material,
                                                                        config.filler2.name, config.filler2.lore,
                                                                        claim, player, ownerName, claimName))
                                        .addIngredient('M', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.claimMap.material, config.claimMap.name,
                                                                        config.claimMap.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                ClaimMapGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('W', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.warps.material, config.warps.name,
                                                                        config.warps.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                WarpManagementGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('A', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.allies.material, config.allies.name,
                                                                        config.allies.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                AllyManagementGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('S', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.settings.material,
                                                                        config.settings.name,
                                                                        config.settings.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                ClaimSettingsGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('T', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.trusted.material, config.trusted.name,
                                                                        config.trusted.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                TrustManagementGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('E', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.members.material, config.members.name,
                                                                        config.members.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                MemberManagementGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('V', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.visitors.material,
                                                                        config.visitors.name,
                                                                        config.visitors.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(click -> {
                                                                player.closeInventory();
                                                                VisitorSettingsGUI.open(player, claim, plugin);
                                                        }).build())
                                        .addIngredient('X', Item.builder()
                                                        .setItemProvider(GuiHelper.buildItemBuilder(
                                                                        config.close.material, config.close.name,
                                                                        config.close.lore,
                                                                        claim, player, ownerName, claimName))
                                                        .addClickHandler(
                                                                        click -> click.player().closeInventory())
                                                        .build())
                                        .build();

                        String windowTitle = config.title.replace("{claim_name}", claimName);
                        Component title = GuiHelper.MM.deserialize(windowTitle);

                        // Phase 3: Sync Delivery — only window.open() on main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                                if (player.isOnline()) {
                                        Window.builder()
                                                        .setTitle(title)
                                                        .setUpperGui(gui)
                                                        .open(player);
                                }
                        });
                });
        }
}
