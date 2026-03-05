package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.AllyPremissionsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class AllyPremissionsGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin,
                        ClaimProfile targetAllyProfile) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        AllyPremissionsConfig config = plugin.getConfigManager().getAllyPremissionsConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        for (Map.Entry<String, AllyPremissionsConfig.ItemConfig> entry : config.flags.entrySet()) {
                                String flagId = entry.getKey();
                                AllyPremissionsConfig.ItemConfig flagConfig = entry.getValue();

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                Set<String> allyFlags = profile
                                                                .getAllyFlags(targetAllyProfile.getOwnerId());
                                                boolean hasFlag = allyFlags != null && allyFlags.contains(flagId);

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
                                                        Set<String> allyFlags = profile
                                                                        .getAllyFlags(targetAllyProfile.getOwnerId());
                                                        if (allyFlags == null) {
                                                                allyFlags = new HashSet<>();
                                                                profile.setAllyFlags(targetAllyProfile.getOwnerId(),
                                                                                allyFlags);
                                                        }

                                                        if (allyFlags.contains(flagId)) {
                                                                allyFlags.remove(flagId);
                                                        } else {
                                                                allyFlags.add(flagId);
                                                        }

                                                        plugin.getClaimManager().saveAndSync(profile);

                                                        if (e.getView().getTopInventory()
                                                                        .getHolder() instanceof PaginatedGui currentGui) {
                                                                currentGui.setPage(currentGui.getCurrentPage(), p);
                                                        }
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "F F F F I F F F F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "P N N N < N N N V"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('N', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('I',
                                        GuiHelper.buildSlot(config.info.material, config.info.name, config.info.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                AllyControlPanelGUI.open(p, profile, plugin,
                                                                                targetAllyProfile);
                                                        }));

                        Component title = GuiHelper.MM
                                        .deserialize(config.title.replace("<name>", targetAllyProfile.getName()));
                        PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

                        gui.setPrevButton(45,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(53,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
