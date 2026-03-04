package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ayosynk.landClaimPlugin.gui.framework.*;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class WarpManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        WarpManagementConfig config = plugin.getConfigManager().getWarpManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();
                        for (Warp warp : profile.getWarps().values()) {
                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                return GuiHelper.buildItemStack(warp.getIcon().name(),
                                                                "<yellow>" + warp.getName(),
                                                                List.of("<gray>Left-Click to teleport",
                                                                                "<gray>Right-Click to manage"));
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.RIGHT) {
                                                                p.closeInventory();
                                                                WarpControlPanelGUI.open(p, profile, plugin, warp);
                                                        } else {
                                                                p.closeInventory();
                                                                p.teleportAsync(warp.getLocation())
                                                                                .thenAccept(success -> {
                                                                                        if (success) {
                                                                                                p.sendMessage(plugin
                                                                                                                .getConfigManager()
                                                                                                                .getMessage(
                                                                                                                                "warp-teleport",
                                                                                                                                "<name>",
                                                                                                                                warp.getName()));
                                                                                        }
                                                                                });
                                                        }
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "f f f f f f f f f",
                                        "f f f f f f f f f",
                                        "f f f f f f f f f",
                                        "P n n n < n n n N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('f', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('n', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
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
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
