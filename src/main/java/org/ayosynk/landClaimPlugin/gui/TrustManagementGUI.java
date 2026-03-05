package org.ayosynk.landClaimPlugin.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.TrustManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class TrustManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        TrustManagementConfig config = plugin.getConfigManager().getTrustManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Build player head items for each trusted player
                        for (UUID trustedId : profile.getTrustedPlayerFlags().keySet()) {
                                String trustedName = Bukkit.getOfflinePlayer(trustedId).getName();
                                if (trustedName == null)
                                        trustedName = trustedId.toString();
                                final String displayName = trustedName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                                                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                                                if (meta != null) {
                                                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(trustedId));
                                                        meta.displayName(GuiHelper.MM
                                                                        .deserialize("<yellow>" + displayName));

                                                        List<Component> lore = new ArrayList<>();
                                                        lore.add(GuiHelper.MM.deserialize(
                                                                        "<gray>Left-click: Edit permissions"));
                                                        lore.add(GuiHelper.MM.deserialize("<gray>Right-click: Remove"));
                                                        meta.lore(lore);
                                                        skull.setItemMeta(meta);
                                                }
                                                return skull;
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.RIGHT) {
                                                                // Right-click → confirmation → remove
                                                                Bukkit.getScheduler().runTask(plugin,
                                                                                () -> ConfirmationGUI.open(p,
                                                                                                "<red>Remove " + displayName
                                                                                                                + "?",
                                                                                                () -> {
                                                                                                        profile.removeTrustedPlayer(
                                                                                                                        trustedId);
                                                                                                        plugin.getCacheManager()
                                                                                                                        .getProfileCache()
                                                                                                                        .put(profile.getOwnerId(),
                                                                                                                                        profile);
                                                                                                        plugin.getClaimManager()
                                                                                                                        .saveAndSync(profile);
                                                                                                        // Re-open trust
                                                                                                        // management
                                                                                                        TrustManagementGUI
                                                                                                                        .open(p, profile,
                                                                                                                                        plugin);
                                                                                                },
                                                                                                () -> TrustManagementGUI
                                                                                                                .open(p, profile,
                                                                                                                                plugin)));
                                                        } else {
                                                                // Left-click → edit permissions
                                                                p.closeInventory();
                                                                PlayerTrustPermissionGUI.open(p, profile, plugin,
                                                                                trustedId);
                                                        }
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P B B + < B B B N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('B', GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name,
                                        config.bottomFill.lore));
                        ingredients.put('+', GuiHelper.buildSlot(config.addPlayer.material, config.addPlayer.name,
                                        config.addPlayer.lore, (p, e) -> {
                                                // Add player via /claim trust add — display instruction message
                                                p.closeInventory();
                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                "<yellow>Use <white>/claim trust add <player> <yellow>to add a trusted player."));
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                MainMenuGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

                        gui.setPrevButton(27,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
