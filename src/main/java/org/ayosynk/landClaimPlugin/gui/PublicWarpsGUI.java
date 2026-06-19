package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.PublicWarpsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-wide public warps browser. Any player with {@code landclaim.warp}
 * can open this GUI and teleport to any warp an owner has marked public.
 * Players are not required to be a member of the claim — the whole point
 * of the public flag is sharing the warp with the rest of the server.
 */
public class PublicWarpsGUI {

        public static void open(Player player, LandClaimPlugin plugin) {
                FoliaScheduler.runAsync(plugin, () -> {
                        PublicWarpsConfig config = plugin.getConfigManager().getPublicWarpsConfig();

                        Map<UUID, Map<String, Warp>> publicWarps = plugin.getWarpManager().getAllPublicWarps();

                        // Flatten into a list of records (ownerId, warp) for stable pagination.
                        List<Map.Entry<UUID, Warp>> entries = new ArrayList<>();
                        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : publicWarps.entrySet()) {
                                for (Warp warp : ownerEntry.getValue().values()) {
                                        entries.add(new java.util.AbstractMap.SimpleEntry<>(ownerEntry.getKey(), warp));
                                }
                        }

                        if (entries.isEmpty()) {
                                player.sendMessage(plugin.getConfigManager().getMessage("publicwarps-no-warps"));
                                return;
                        }

                        List<GuiItem> contentItems = new ArrayList<>();
                        for (Map.Entry<UUID, Warp> entry : entries) {
                                UUID ownerId = entry.getKey();
                                Warp warp = entry.getValue();
                                final UUID finalOwnerId = ownerId;
                                final Warp finalWarp = warp;
                                String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
                                if (ownerName == null)
                                        ownerName = ownerId.toString();
                                final String finalOwnerName = ownerName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                return GuiHelper.buildItemStack(finalWarp.getIcon().name(),
                                                                "<yellow>" + finalWarp.getName(),
                                                                List.of(
                                                                                "<gray>Owner: <gold>" + finalOwnerName,
                                                                                "<gray>Left-Click to teleport"));
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.LEFT) {
                                                                p.closeInventory();
                                                                p.teleportAsync(finalWarp.getLocation())
                                                                                .thenAccept(success -> {
                                                                                        if (success) {
                                                                                                p.sendMessage(plugin
                                                                                                                .getConfigManager()
                                                                                                                .getMessage(
                                                                                                                                "publicwarps-teleported",
                                                                                                                                "<owner>",
                                                                                                                                finalOwnerName,
                                                                                                                                "<name>",
                                                                                                                                finalWarp.getName()));
                                                                                        }
                                                                                });
                                                        }
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P n n n < n n n N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('n', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                org.ayosynk.landClaimPlugin.gui.MainMenuGUI
                                                                                .open(p, plugin.getClaimManager()
                                                                                                .getActiveProfile(p),
                                                                                                plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

                        gui.setPrevButton(27,
                                        GuiHelper.buildItemStack(config.previousPage.material,
                                                        config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        FoliaScheduler.runTask(plugin, () -> {
                                gui.setContent(contentItems, player);
                                gui.open(player);
                        });
                });
        }
}
