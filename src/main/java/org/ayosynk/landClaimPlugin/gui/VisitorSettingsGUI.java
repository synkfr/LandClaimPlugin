package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitorSettingsGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (plugin.getConfigManager().isVisitorSettingsLocked()) {
                        player.sendMessage(plugin.getConfigManager().getMessage("visitor-settings-locked"));
                        return;
                }
                if (!GuiHelper.checkMenuPermission(player, "visitors", plugin)) {
                        return;
                }
                VisitorSettingsConfig config = plugin.getConfigManager().getVisitorSettingsConfig();

                List<GuiItem> contentItems = new ArrayList<>();

                for (Map.Entry<String, VisitorSettingsConfig.ItemConfig> entry : config.flags.entrySet()) {
                        String flagId = entry.getKey();
                        if (flagId.equals("CLAIM_LAND") || flagId.equals("ADMIN_MENU")
                                || flagId.equals("MANAGE_SETTINGS") || flagId.equals("MANAGE_MEMBERS")
                                || flagId.equals("MANAGE_ROLES")) continue;
                        VisitorSettingsConfig.ItemConfig flagConfig = entry.getValue();

                        contentItems.add(new GuiItem() {
                                @Override
                                public ItemStack render(Player viewer) {
                                        boolean hasFlag = profile.hasVisitorFlag(flagId);
                                        String statusText = hasFlag ? config.enabledStatus : config.disabledStatus;
                                        return GuiHelper.buildToggleFlagItem(flagConfig.material,
                                                        flagConfig.name, flagConfig.lore, statusText, hasFlag);
                                }

                                @Override
                                public org.ayosynk.landClaimPlugin.gui.framework.ClickAction clickAction() {
                                        return (p, e) -> {
                                                if (profile.hasVisitorFlag(flagId)) {
                                                        profile.removeVisitorFlag(flagId);
                                                } else {
                                                        profile.addVisitorFlag(flagId);
                                                }

                                                plugin.getDatabaseManager().getProfileDao().saveProfile(profile)
                                                                .thenRun(() -> {
                                                                        if (plugin.getRedisManager() != null) {
                                                                                plugin.getRedisManager()
                                                                                                .publishUpdate("INVALIDATE_PROFILE",
                                                                                                                profile.getProfileId());
                                                                        }
                                                                });

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
                                "P b b b B b b b N"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                ingredients.put('b', GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name, config.bottomFill.lore));
                ingredients.put('B', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> MainMenuGUI.open(p, profile, plugin)));

                Component title = GuiHelper.MM.deserialize(config.title);
                PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

                ItemStack navFiller = GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name, config.bottomFill.lore);
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
