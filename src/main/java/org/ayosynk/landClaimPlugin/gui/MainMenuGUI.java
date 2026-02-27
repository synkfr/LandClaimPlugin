package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;
import org.ayosynk.landClaimPlugin.config.menus.MainMenuConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.ArrayList;
import java.util.List;

public class MainMenuGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                String ownerName = claim.getOwnerId() != null ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                                : "Unknown";
                if (ownerName == null)
                        ownerName = "Unknown";

                String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

                MainMenuConfig config = plugin.getConfigManager().getMainMenuConfig();
                MiniMessage mm = MiniMessage.miniMessage();

                Gui.Builder<?, ?> guiBuilder = Gui.builder()
                                .setStructure(
                                                "1 1 2 2 2 2 2 1 1",
                                                "1 M W A S T E V 1",
                                                "2 2 2 2 2 2 2 2 2",
                                                "2 2 2 1 X 1 2 2 2")
                                .addIngredient('1',
                                                buildConfigItem(config.filler1, claim, player, ownerName, claimName))
                                .addIngredient('2',
                                                buildConfigItem(config.filler2, claim, player, ownerName, claimName))
                                .addIngredient('M', Item.builder()
                                                .setItemProvider(buildConfigItemBuilder(config.claimMap, claim, player,
                                                                ownerName, claimName))
                                                .addClickHandler(click -> {
                                                        player.closeInventory();
                                                        ClaimMapGUI.open(player, claim, plugin);
                                                }).build())
                                .addIngredient('W', buildConfigItem(config.warps, claim, player, ownerName, claimName))
                                .addIngredient('A', buildConfigItem(config.allies, claim, player, ownerName, claimName))
                                .addIngredient('S', Item.builder()
                                                .setItemProvider(buildConfigItemBuilder(config.settings, claim, player,
                                                                ownerName, claimName))
                                                .addClickHandler(click -> {
                                                        player.closeInventory();
                                                        ClaimSettingsGUI.open(player, claim, plugin);
                                                }).build())
                                .addIngredient('T',
                                                buildConfigItem(config.trusted, claim, player, ownerName, claimName))
                                .addIngredient('E',
                                                buildConfigItem(config.members, claim, player, ownerName, claimName))
                                .addIngredient('V', Item.builder()
                                                .setItemProvider(buildConfigItemBuilder(config.visitors, claim, player,
                                                                ownerName, claimName))
                                                .addClickHandler(click -> {
                                                        player.closeInventory();
                                                        VisitorSettingsGUI.open(player, claim, plugin);
                                                }).build())
                                .addIngredient('X', Item.builder()
                                                .setItemProvider(buildConfigItemBuilder(config.close, claim, player,
                                                                ownerName, claimName))
                                                .addClickHandler(click -> click.player().closeInventory()).build());

                Gui gui = guiBuilder.build();

                String windowTitle = config.title.replace("{claim_name}", claimName);

                Window.builder()
                                .setTitle(mm.deserialize(windowTitle))
                                .setUpperGui(gui)
                                .open(player);
        }

        private static Item buildConfigItem(MainMenuConfig.ItemConfig itemConfig, Claim claim, Player player,
                        String ownerName, String claimName) {
                return Item.simple(buildConfigItemBuilder(itemConfig, claim, player, ownerName, claimName));
        }

        private static ItemBuilder buildConfigItemBuilder(MainMenuConfig.ItemConfig itemConfig, Claim claim,
                        Player player, String ownerName, String claimName) {
                Material mat = Material.matchMaterial(itemConfig.material.toUpperCase());
                if (mat == null)
                        mat = Material.STONE;

                ItemBuilder builder = new ItemBuilder(mat);
                builder.addModifier(item -> {
                        item.editMeta(meta -> {
                                meta.addItemFlags(org.bukkit.inventory.ItemFlag.values());
                                try {
                                        meta.setAttributeModifiers(
                                                        com.google.common.collect.LinkedListMultimap.create());
                                } catch (Exception ignored) {
                                }
                        });
                        return item;
                });
                MiniMessage mm = MiniMessage.miniMessage();

                if (itemConfig.name != null && !itemConfig.name.isEmpty()) {
                        Component comp = mm.deserialize(
                                        replacePlaceholders(itemConfig.name, claim, player, ownerName, claimName));
                        builder.setCustomName(comp);
                }

                if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
                        List<Component> lore = new ArrayList<>();
                        for (String line : itemConfig.lore) {
                                Component comp = mm.deserialize(
                                                replacePlaceholders(line, claim, player, ownerName, claimName));
                                lore.add(comp);
                        }
                        builder.setLore(lore);
                }

                return builder;
        }

        private static String replacePlaceholders(String text, Claim claim, Player player, String ownerName,
                        String claimName) {
                return text.replace("{claim_name}", claimName)
                                .replace("{owner}", ownerName)
                                .replace("{size}", String.valueOf(claim.getChunks().size()))
                                .replace("{power}", "0")
                                .replace("{members}", String.valueOf(claim.getPlayerRoles().size()))
                                .replace("{world}", player.getWorld().getName())
                                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
        }
}
