package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class RoleSetupGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        RoleSetupConfig config = plugin.getConfigManager().getRoleSetupConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui gui = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F F N F P F T F F",
                        "F F F F F F F F F",
                        "G G G < G S G G G")
                .addIngredient('F', buildConfigItem(config.frame))
                .addIngredient('G', buildConfigItem(config.navFill))
                .addIngredient('N', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.setName))
                        .addClickHandler(click -> {
                            // Reserved: Opens chat/anvil input for role name
                        }).build())
                .addIngredient('P', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.permissions))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            // RoleEditGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('T', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.setPriority))
                        .addClickHandler(click -> {
                            // Reserved: Adjust role hierarchy priority
                        }).build())
                .addIngredient('<', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            RoleManagementGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('S', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.saveExit))
                        .addClickHandler(click -> {
                            // Reserved: Create role and close setup
                            player.closeInventory();
                            RoleManagementGUI.open(player, claim, plugin);
                        }).build())
                .build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(RoleSetupConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(RoleSetupConfig.ItemConfig itemConfig) {
        Material mat = Material.matchMaterial(itemConfig.material.toUpperCase());
        if (mat == null)
            mat = Material.STONE;

        ItemBuilder builder = new ItemBuilder(mat);
        builder.addModifier(item -> {
            item.editMeta(meta -> {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.values());
                try {
                    meta.setAttributeModifiers(com.google.common.collect.LinkedListMultimap.create());
                } catch (Exception ignored) {
                }
            });
            return item;
        });
        MiniMessage mm = MiniMessage.miniMessage();

        if (itemConfig.name != null && !itemConfig.name.isEmpty()) {
            Component comp = mm.deserialize(itemConfig.name);
            builder.setCustomName(comp);
        }

        if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : itemConfig.lore) {
                Component comp = mm.deserialize(line);
                lore.add(comp);
            }
            builder.setLore(lore);
        }

        return builder;
    }
}
