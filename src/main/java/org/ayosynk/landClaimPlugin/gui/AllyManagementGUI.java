package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.AllyManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllyManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "allies", plugin)) {
                        return;
                }
                FoliaScheduler.runAsync(plugin, () -> {
                        AllyManagementConfig config = plugin.getConfigManager().getAllyManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        for (UUID allyOwnerId : profile.getAllyFlags().keySet()) {
                                ClaimProfile allyProfile = plugin.getClaimManager().getProfile(allyOwnerId);
                                if (allyProfile == null)
                                        continue;

                                String allyName = allyProfile.getColoredName();
                                String ownerName = allyProfile.getColoredOwnerName();
                                final String finalOwnerName = ownerName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                var cm = plugin.getConfigManager();
                                                List<Component> lore = List.of(
                                                                cm.getRawComponent("gui.ally-owner-line", "<owner>", finalOwnerName),
                                                                Component.empty(),
                                                                cm.getRawComponent("gui.click-manage"));
                                                return GuiHelper.buildPlayerHead(
                                                                allyOwnerId,
                                                                finalOwnerName,
                                                                cm.getRawComponent("gui.skull-item-name", "<name>", allyName),
                                                                lore);
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> AllyControlPanelGUI.open(p, profile, plugin, allyProfile);
                                        }
                                });
                        }

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "F x x x x x x x F",
                                        "B P B + < B B N B"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('+', GuiHelper.buildSlot(config.addAlly.material, config.addAlly.name,
                                        config.addAlly.lore, (p, e) -> {
                                                OnlinePlayerSelectorGUI.open(p, plugin, target -> {
                                                        ClaimProfile targetProfile = plugin.getClaimManager().getActiveProfile(target);
                                                        
                                                        if (targetProfile == null) {
                                                                p.sendMessage(plugin.getConfigManager()
                                                                                .getMessage("claim-not-found"));
                                                                AllyManagementGUI.open(p, profile, plugin);
                                                                return;
                                                        }

                                                        if (profile.getProfileId().equals(targetProfile.getProfileId())) {
                                                                p.sendMessage(plugin.getConfigManager()
                                                                                .getMessage("cannot-ally-self"));
                                                                AllyManagementGUI.open(p, profile, plugin);
                                                                return;
                                                        }

                                                        if (profile.getAllyFlags().containsKey(targetProfile.getProfileId())) {
                                                                p.sendMessage(plugin.getConfigManager()
                                                                                .getMessage("already-allied"));
                                                                AllyManagementGUI.open(p, profile, plugin);
                                                                return;
                                                        }

                                                        plugin.getClaimManager().sendAllyInvite(p, targetProfile);
                                                        AllyManagementGUI.open(p, profile, plugin);
                                                }, () -> AllyManagementGUI.open(p, profile, plugin));
                                        }));
                        ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                        (p, e) -> MainMenuGUI.open(p, profile, plugin)));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

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

                        FoliaScheduler.runForPlayer(plugin, player, () -> {
                                gui.setContent(contentItems, player);
                                if (contentItems.isEmpty()) {
                                        gui.setItem(22, GuiHelper.buildItemStack(config.emptyIndicator.material,
                                                        config.emptyIndicator.name, config.emptyIndicator.lore));
                                }
                                gui.open(player);
                        });
                });
        }
}
