package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ClaimMapGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        ClaimMapConfig config = plugin.getConfigManager().getClaimMapConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui.Builder<?, ?> guiBuilder = Gui.builder()
                .setStructure(
                        "W F F F F F F F F",
                        "F F F F F F F F F",
                        "F F F F F F F F F",
                        "F F F F F F F F F",
                        "F F F F F F F F F",
                        "B B R B X B I B B")
                .addIngredient('W', buildConfigItem(config.wilderness))
                .addIngredient('F', buildConfigItem(config.mapFill))
                .addIngredient('B', buildConfigItem(config.bottomFill))
                .addIngredient('R', buildConfigItem(config.refresh))
                .addIngredient('X', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            MainMenuGUI.open(player, claim, plugin);
                        }).build())
                .addIngredient('I', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.info))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            ClaimMapInfoGUI.open(player, claim, plugin);
                        }).build());

        Gui gui = guiBuilder.build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(ClaimMapConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(ClaimMapConfig.ItemConfig itemConfig) {
        Material mat = Material.matchMaterial(itemConfig.material.toUpperCase());
        if (mat == null)
            mat = Material.STONE;

        ItemBuilder builder = new ItemBuilder(mat);
        builder.addModifier(item -> {
            item.editMeta(meta -> meta.addItemFlags(org.bukkit.inventory.ItemFlag.values()));
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
