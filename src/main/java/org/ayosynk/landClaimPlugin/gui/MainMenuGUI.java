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

public class MainMenuGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                String ownerName = claim.getOwnerId() != null ? Bukkit.getOfflinePlayer(claim.getOwnerId()).getName()
                                : "Unknown";
                if (ownerName == null)
                        ownerName = "Unknown";

                String claimName = claim.getName() != null ? claim.getName() : "Unnamed Claim";

                Gui.Builder<?, ?> guiBuilder = Gui.builder()
                                .setStructure(
                                                "F F F F F F F F F",
                                                ". I M E T V . . .",
                                                ". W S . C . . . .",
                                                ". . . . . . . . .",
                                                ". . . . . . . . .",
                                                "F F F F F F F F X")
                                .addIngredient('F',
                                                Item.simple(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                                                                .setLegacyName(" ")))
                                .addIngredient('I', Item.simple(new ItemBuilder(Material.WRITTEN_BOOK)
                                                .setLegacyName("§eClaim Information")
                                                .addLegacyLoreLines(
                                                                "§7Owner: §f" + ownerName,
                                                                "§7Size: §f" + claim.getChunks().size() + " chunks",
                                                                "§7Power usage: §f0", // Placeholder for when power is
                                                                                      // implemented
                                                                "§7Members count: §f" + claim.getPlayerRoles().size(),
                                                                "",
                                                                "§eClick to view details")))
                                .addIngredient('M', Item.simple(new ItemBuilder(Material.FILLED_MAP)
                                                .setLegacyName("§aClaim Map")
                                                .addLegacyLoreLines(
                                                                "§7Opens visual chunk overview",
                                                                "§7Shows nearby claims & highlights borders",
                                                                "",
                                                                "§eClick to open map (Soon)")))
                                .addIngredient('E', Item.simple(new ItemBuilder(Material.PLAYER_HEAD)
                                                .setLegacyName("§bMembers")
                                                .addLegacyLoreLines(
                                                                "§7Shows all claim members",
                                                                "",
                                                                "§eClick to manage")))
                                .addIngredient('T', Item.simple(new ItemBuilder(Material.TOTEM_OF_UNDYING)
                                                .setLegacyName("§6Trusted Players")
                                                .addLegacyLoreLines(
                                                                "§7Players who bypass protections",
                                                                "",
                                                                "§eClick to manage")))
                                .addIngredient('V', Item.simple(new ItemBuilder(Material.EMERALD)
                                                .setLegacyName("§aVisitor Settings")
                                                .addLegacyLoreLines(
                                                                "§7Configure what visitors can do",
                                                                "",
                                                                "§eClick to configure")))
                                .addIngredient('W', Item.simple(new ItemBuilder(Material.RECOVERY_COMPASS)
                                                .setLegacyName("§dClaim Warps")
                                                .addLegacyLoreLines(
                                                                "§7Create and teleport to warps",
                                                                "",
                                                                "§eClick to manage warps")))
                                .addIngredient('S', Item.simple(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                                                .setLegacyName("§eClaim Settings")
                                                .addLegacyLoreLines(
                                                                "§7Core configuration",
                                                                "§7Durable system settings",
                                                                "",
                                                                "§eClick to open settings")))
                                .addIngredient('C', Item.simple(new ItemBuilder(Material.GRASS_BLOCK)
                                                .setLegacyName("§a" + claimName)
                                                .addLegacyLoreLines(
                                                                "§7World: §f" + player.getWorld().getName(),
                                                                "§7Location: §f" + player.getLocation().getBlockX()
                                                                                + ", "
                                                                                + player.getLocation().getBlockZ(),
                                                                "§7Chunk size: §f" + claim.getChunks().size())))
                                .addIngredient('X', Item.builder()
                                                .setItemProvider(new ItemBuilder(Material.BARRIER)
                                                                .setLegacyName("§cClose"))
                                                .addClickHandler(click -> click.player().closeInventory()).build());

                Gui gui = guiBuilder.build();

                Window.builder()
                                .setTitle("Claim: " + claimName)
                                .setUpperGui(gui)
                                .open(player);
        }
}
