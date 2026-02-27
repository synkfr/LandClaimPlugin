package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.ClaimMapInfoConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class ClaimMapInfoGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        ClaimMapInfoConfig config = plugin.getConfigManager().getClaimMapInfoConfig();
        MiniMessage mm = MiniMessage.miniMessage();

        Gui.Builder<?, ?> guiBuilder = Gui.builder()
                .setStructure(
                        "F F F F F F F F F",
                        "F F O A P E W F F",
                        "F F F F F F F F F",
                        "F F F 1 B 1 F F F")
                .addIngredient('F', buildConfigItem(config.filler))
                .addIngredient('1', buildConfigItem(config.filler1))
                .addIngredient('O', buildConfigItem(config.owned))
                .addIngredient('A', buildConfigItem(config.ally))
                .addIngredient('P', buildConfigItem(config.other))
                .addIngredient('E', buildConfigItem(config.enemy))
                .addIngredient('W', buildConfigItem(config.wilderness))
                .addIngredient('B', Item.builder()
                        .setItemProvider(buildConfigItemBuilder(config.back))
                        .addClickHandler(click -> {
                            player.closeInventory();
                            ClaimMapGUI.open(player, claim, plugin);
                        }).build());

        Gui gui = guiBuilder.build();

        String windowTitle = config.title;

        Window.builder()
                .setTitle(mm.deserialize(windowTitle))
                .setUpperGui(gui)
                .open(player);
    }

    private static Item buildConfigItem(ClaimMapInfoConfig.ItemConfig itemConfig) {
        return Item.simple(buildConfigItemBuilder(itemConfig));
    }

    private static ItemBuilder buildConfigItemBuilder(ClaimMapInfoConfig.ItemConfig itemConfig) {
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
