package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpChangeIconConfig;
import org.ayosynk.landClaimPlugin.gui.framework.*;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpChangeIconGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Warp warp) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        WarpChangeIconConfig config = plugin.getConfigManager().getWarpChangeIconConfig();

                        List<GuiItem> contentItems = new ArrayList<>();
                        List<Material> materials = List.of(
                                        Material.ENDER_PEARL, Material.COMPASS, Material.FILLED_MAP,
                                        Material.RECOVERY_COMPASS, Material.SPYGLASS, Material.WRITABLE_BOOK,
                                        Material.BOOK, Material.CLOCK, Material.FEATHER, Material.LANTERN,
                                        Material.CAMPFIRE, Material.CYAN_DYE, Material.CANDLE, Material.TORCH,
                                        Material.CHEST, Material.ENDER_CHEST, Material.SHULKER_BOX, Material.BARREL,
                                        Material.CRAFTING_TABLE, Material.FURNACE, Material.ENCHANTING_TABLE,
                                        Material.ANVIL, Material.LOOM, Material.SMITHING_TABLE,
                                        Material.SUSPICIOUS_SAND);

                        for (Material mat : materials) {
                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                return GuiHelper.buildItemStack(mat.name(), "<yellow>" + mat.name(),
                                                                List.of("<gray>Click to select this icon"));
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        p.closeInventory();
                                                        warp.setIcon(mat);
                                                        plugin.getWarpManager().setWarp(profile.getOwnerId(),
                                                                        warp.getName(),
                                                                        warp.getLocation(), warp.getIcon());
                                                        p.sendMessage(plugin.getConfigManager().getMessage(
                                                                        "warp-icon-updated",
                                                                        "<name>", warp.getName()));
                                                        WarpControlPanelGUI.open(p, profile, plugin, warp);
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P B B B < B B B N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                WarpControlPanelGUI.open(p, profile, plugin, warp);
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
