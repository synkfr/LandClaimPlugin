package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
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
                FoliaScheduler.runAsync(plugin, () -> {
                        AllyPremissionsConfig config = plugin.getConfigManager().getAllyPremissionsConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        for (Map.Entry<String, AllyPremissionsConfig.ItemConfig> entry : config.flags.entrySet()) {
                                String flagId = entry.getKey();
                                if (flagId.equals("CLAIM_LAND")) continue;
                                AllyPremissionsConfig.ItemConfig flagConfig = entry.getValue();

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                Set<String> allyFlags = profile
                                                                .getAllyFlags(targetAllyProfile.getProfileId());
                                                boolean hasFlag = allyFlags != null && allyFlags.contains(flagId);
                                                String statusText = hasFlag ? config.enabledStatus
                                                                : config.disabledStatus;
                                                return GuiHelper.buildToggleFlagItem(flagConfig.material,
                                                                flagConfig.name, flagConfig.lore, statusText, hasFlag);
                                        }

                                        @Override
                                        public org.ayosynk.landClaimPlugin.gui.framework.ClickAction clickAction() {
                                                return (p, e) -> {
                                                        Set<String> allyFlags = profile
                                                                        .getAllyFlags(targetAllyProfile.getProfileId());
                                                        if (allyFlags == null) {
                                                                allyFlags = new HashSet<>();
                                                                profile.setAllyFlags(targetAllyProfile.getProfileId(),
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
                                        "P B B B < B B B N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('I',
                                        GuiHelper.buildSlot(config.info.material, config.info.name, config.info.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> AllyControlPanelGUI.open(p, profile, plugin, targetAllyProfile)));

                        Component title = GuiHelper.MM
                                        .deserialize(config.title.replace("<name>", targetAllyProfile.getName()));
                        PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

                        ItemStack navFiller = GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                        config.navFill.lore);
                        gui.setPrevButton(
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        navFiller);
                        gui.setNextButton(
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        navFiller);

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
