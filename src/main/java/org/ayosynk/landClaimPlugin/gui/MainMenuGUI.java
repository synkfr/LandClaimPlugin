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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
                                                "F F F F F F F F F",
                                                ". I M E T V . . .",
                                                ". W S . C . . . .",
                                                ". . . . . . . . .",
                                                ". . . . . . . . .",
                                                "F F F F F F F F X")
                                .addIngredient('F', buildConfigItem(config.filler, claim, player, ownerName, claimName))
                                .addIngredient('I',
                                                buildConfigItem(config.claimInfo, claim, player, ownerName, claimName))
                                .addIngredient('M',
                                                buildConfigItem(config.claimMap, claim, player, ownerName, claimName))
                                .addIngredient('E',
                                                buildConfigItem(config.members, claim, player, ownerName, claimName))
                                .addIngredient('T',
                                                buildConfigItem(config.trusted, claim, player, ownerName, claimName))
                                .addIngredient('V',
                                                buildConfigItem(config.visitors, claim, player, ownerName, claimName))
                                .addIngredient('W', buildConfigItem(config.warps, claim, player, ownerName, claimName))
                                .addIngredient('S',
                                                buildConfigItem(config.settings, claim, player, ownerName, claimName))
                                .addIngredient('C',
                                                buildConfigItem(config.claimAnchor, claim, player, ownerName,
                                                                claimName))
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
                MiniMessage mm = MiniMessage.miniMessage();
                LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

                if (itemConfig.name != null && !itemConfig.name.isEmpty()) {
                        Component comp = mm.deserialize(
                                        replacePlaceholders(itemConfig.name, claim, player, ownerName, claimName));
                        builder.setLegacyName(legacy.serialize(comp));
                }

                if (itemConfig.lore != null && !itemConfig.lore.isEmpty()) {
                        List<String> lore = new ArrayList<>();
                        for (String line : itemConfig.lore) {
                                Component comp = mm.deserialize(
                                                replacePlaceholders(line, claim, player, ownerName, claimName));
                                lore.add(legacy.serialize(comp));
                        }
                        builder.setLegacyLore(lore);
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
