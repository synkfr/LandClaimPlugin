package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import net.kyori.adventure.text.Component;

public class RoleSetupGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RoleSetupConfig config = plugin.getConfigManager().getRoleSetupConfig();

            Gui gui = Gui.builder()
                    .setStructure(
                            "F F F F F F F F F",
                            "F F N F P F T F F",
                            "F F F F F F F F F",
                            "G G G < G S G G G")
                    .addIngredient('F',
                            GuiHelper.buildItem(config.frame.material, config.frame.name, config.frame.lore))
                    .addIngredient('G',
                            GuiHelper.buildItem(config.navFill.material, config.navFill.name, config.navFill.lore))
                    .addIngredient('N', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.setName.material, config.setName.name,
                                    config.setName.lore))
                            .addClickHandler(click -> {
                                // Reserved: Opens chat/anvil input for role name
                            }).build())
                    .addIngredient('P', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.permissions.material,
                                    config.permissions.name, config.permissions.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                // RoleEditGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('T', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.setPriority.material,
                                    config.setPriority.name, config.setPriority.lore))
                            .addClickHandler(click -> {
                                // Reserved: Adjust role hierarchy priority
                            }).build())
                    .addIngredient('<', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.back.material, config.back.name,
                                    config.back.lore))
                            .addClickHandler(click -> {
                                player.closeInventory();
                                RoleManagementGUI.open(player, claim, plugin);
                            }).build())
                    .addIngredient('S', Item.builder()
                            .setItemProvider(GuiHelper.buildItemBuilder(config.saveExit.material,
                                    config.saveExit.name, config.saveExit.lore))
                            .addClickHandler(click -> {
                                // Reserved: Create role and close setup
                                player.closeInventory();
                                RoleManagementGUI.open(player, claim, plugin);
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
