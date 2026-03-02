package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitorSettingsGUI {

        public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        VisitorSettingsConfig config = plugin.getConfigManager().getVisitorSettingsConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        for (Map.Entry<String, VisitorSettingsConfig.ItemConfig> entry : config.flags.entrySet()) {
                                String flagId = entry.getKey();
                                VisitorSettingsConfig.ItemConfig flagConfig = entry.getValue();

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                boolean hasFlag = claim.hasVisitorFlag(flagId);
                                                ItemStack item = GuiHelper.buildItemStack(flagConfig.material,
                                                                flagConfig.name, flagConfig.lore);
                                                ItemMeta meta = item.getItemMeta();
                                                if (meta != null) {
                                                        List<Component> lore = meta.lore();
                                                        if (lore == null)
                                                                lore = new ArrayList<>();

                                                        lore.add(Component.empty());
                                                        String statusText = hasFlag ? config.enabledStatus
                                                                        : config.disabledStatus;
                                                        lore.add(GuiHelper.MM.deserialize(statusText));
                                                        meta.lore(lore);

                                                        // Optionally add a glow if enabled
                                                        if (hasFlag) {
                                                                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING,
                                                                                1, true);
                                                                meta.addItemFlags(
                                                                                org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                                                        }
                                                        item.setItemMeta(meta);
                                                }
                                                return item;
                                        }

                                        @Override
                                        public org.ayosynk.landClaimPlugin.gui.framework.ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (claim.hasVisitorFlag(flagId)) {
                                                                claim.removeVisitorFlag(flagId);
                                                        } else {
                                                                claim.addVisitorFlag(flagId);
                                                        }

                                                        // Save asynchronously and invalidate cache
                                                        plugin.getDatabaseManager().getClaimDao().saveClaim(claim)
                                                                        .thenRun(() -> {
                                                                                if (plugin.getRedisManager() != null) {
                                                                                        plugin.getRedisManager()
                                                                                                        .publishUpdate("INVALIDATE_CLAIM",
                                                                                                                        claim.getId());
                                                                                }
                                                                        });

                                                        // Refresh the current GUI page to show updated status
                                                        if (e.getView().getTopInventory()
                                                                        .getHolder() instanceof PaginatedGui currentGui) {
                                                                currentGui.setPage(currentGui.getCurrentPage(), p);
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
                                        "F x x x x x x x F",
                                        "F B F F P F N F F",
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', new SlotDefinition(
                                        GuiHelper.buildItemStack(config.frame.material, config.frame.name,
                                                        config.frame.lore)));
                        ingredients.put('B', new SlotDefinition(
                                        GuiHelper.buildItemStack(config.back.material, config.back.name,
                                                        config.back.lore),
                                        (p, e) -> MainMenuGUI.open(p, claim, plugin)));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

                        gui.setPrevButton(45,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));
                        gui.setNextButton(53,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
