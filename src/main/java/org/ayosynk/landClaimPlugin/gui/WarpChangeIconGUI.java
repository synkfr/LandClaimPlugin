package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpChangeIconConfig;
import org.ayosynk.landClaimPlugin.gui.framework.*;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpChangeIconGUI {

        private static final List<Material> ICON_MATERIALS = List.of(
                        Material.ENDER_PEARL, Material.COMPASS, Material.FILLED_MAP,
                        Material.RECOVERY_COMPASS, Material.SPYGLASS, Material.WRITABLE_BOOK,
                        Material.BOOK, Material.CLOCK, Material.FEATHER, Material.LANTERN,
                        Material.CAMPFIRE, Material.CYAN_DYE, Material.CANDLE, Material.TORCH,
                        Material.CHEST, Material.ENDER_CHEST, Material.SHULKER_BOX, Material.BARREL,
                        Material.CRAFTING_TABLE, Material.FURNACE, Material.ENCHANTING_TABLE,
                        Material.ANVIL, Material.LOOM, Material.SMITHING_TABLE,
                        Material.SUSPICIOUS_SAND);

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Warp warp) {
                var cm = plugin.getConfigManager();
                WarpChangeIconConfig config = cm.getWarpChangeIconConfig();

                List<GuiItem> contentItems = new ArrayList<>(ICON_MATERIALS.size());
                for (Material mat : ICON_MATERIALS) {
                        contentItems.add(new GuiItem() {
                                @Override
                                public ItemStack render(Player viewer) {
                                        return GuiHelper.buildItem(
                                                        mat,
                                                        cm.getRawComponent("gui.warp-icon-name", "<icon>", mat.name()),
                                                        List.of(cm.getRawComponent("gui.warp-icon-hint")));
                                }

                                @Override
                                public ClickAction clickAction() {
                                        return (p, e) -> {
                                                warp.setIcon(mat);
                                                plugin.getWarpManager().setWarp(profile.getProfileId(),
                                                                warp.getName(), warp.getLocation(), warp.getIcon());
                                                p.sendMessage(cm.getMessage("warp-icon-updated", "<name>", warp.getName()));
                                                WarpControlPanelGUI.open(p, profile, plugin, warp);
                                        };
                                }
                        });
                }

                String[] structure = {
                                "F F F F F F F F F",
                                "F x x x x x x x F",
                                "F x x x x x x x F",
                                "F x x x x x x x F",
                                "B P B B < B B N B"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('F', GuiHelper.buildSlot(config.spacer.material, config.spacer.name, config.spacer.lore));
                ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
                ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> WarpControlPanelGUI.open(p, profile, plugin, warp)));

                Component title = GuiHelper.MM.deserialize(config.title);
                PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

                ItemStack navFiller = GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore);
                gui.setPrevButton(
                                GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name, config.previousPage.lore),
                                navFiller);
                gui.setNextButton(
                                GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                                navFiller);

                gui.setContent(contentItems, player);
                gui.open(player);
        }
}
