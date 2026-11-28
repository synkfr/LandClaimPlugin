package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.PublicWarpsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
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
 */
public class PublicWarpsGUI {

        public static void open(Player player, LandClaimPlugin plugin) {
                FoliaScheduler.runAsync(plugin, () -> {
                        var cm = plugin.getConfigManager();
                        PublicWarpsConfig config = cm.getPublicWarpsConfig();

                        Map<UUID, Map<String, Warp>> publicWarps = plugin.getWarpManager().getAllPublicWarps();

                        List<Map.Entry<UUID, Warp>> entries = new ArrayList<>();
                        for (Map.Entry<UUID, Map<String, Warp>> ownerEntry : publicWarps.entrySet()) {
                                for (Warp warp : ownerEntry.getValue().values()) {
                                        entries.add(new java.util.AbstractMap.SimpleEntry<>(ownerEntry.getKey(), warp));
                                }
                        }

                        if (entries.isEmpty()) {
                                FoliaScheduler.runForPlayer(plugin, player,
                                                () -> player.sendMessage(cm.getMessage("publicwarps-no-warps")));
                                return;
                        }

                        List<GuiItem> contentItems = new ArrayList<>(entries.size());
                        for (Map.Entry<UUID, Warp> entry : entries) {
                                UUID ownerId = entry.getKey();
                                Warp warp = entry.getValue();
                                final Warp finalWarp = warp;
                                ClaimProfile ownerProfile = plugin.getClaimManager().getProfile(ownerId);
                                String ownerName = ownerProfile != null
                                                ? ownerProfile.getDisplayOwnerName()
                                                : Bukkit.getOfflinePlayer(ownerId).getName();
                                if (ownerName == null)
                                        ownerName = ownerId.toString();
                                final String finalOwnerName = ownerName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                return GuiHelper.buildItem(
                                                                finalWarp.getIcon(),
                                                                cm.getRawComponent("gui.warp-item-name", "<name>", finalWarp.getName()),
                                                                List.of(
                                                                                cm.getRawComponent("gui.warp-owner-line", "<owner>", finalOwnerName),
                                                                                cm.getRawComponent("gui.warp-teleport-hint")));
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.LEFT) {
                                                                p.closeInventory();
                                                                if (plugin.getCombatManager().isInCombat(p)) {
                                                                        p.sendMessage(cm.getMessage("in-combat"));
                                                                        return;
                                                                }
                                                                p.teleportAsync(finalWarp.getLocation())
                                                                                .thenAccept(success -> {
                                                                                        if (success) {
                                                                                                p.sendMessage(cm.getMessage(
                                                                                                                "publicwarps-teleported",
                                                                                                                "<owner>", finalOwnerName,
                                                                                                                "<name>", finalWarp.getName()));
                                                                                        }
                                                                                });
                                                        }
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "n P n n < n n N n"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                        ingredients.put('n', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
                        ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                        (p, e) -> {
                                                ClaimProfile active = plugin.getClaimManager().getActiveProfile(p);
                                                if (active != null) {
                                                        MainMenuGUI.open(p, active, plugin);
                                                } else {
                                                        p.closeInventory();
                                                }
                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

                        ItemStack navFiller = GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore);
                        gui.setPrevButton(
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name, config.previousPage.lore),
                                        navFiller);
                        gui.setNextButton(
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                                        navFiller);

                        FoliaScheduler.runForPlayer(plugin, player, () -> {
                                gui.setContent(contentItems, player);
                                gui.open(player);
                        });
                });
        }
}
