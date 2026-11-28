package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleEditConfig;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleEditGUI {

    public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Role role, boolean isNew) {
        RoleEditConfig config = plugin.getConfigManager().getRoleEditConfig();
        VisitorSettingsConfig flagsConfig = plugin.getConfigManager().getVisitorSettingsConfig();

        List<GuiItem> contentItems = new ArrayList<>();

            for (Map.Entry<String, VisitorSettingsConfig.ItemConfig> entry : flagsConfig.flags.entrySet()) {
                String flagId = entry.getKey();
                VisitorSettingsConfig.ItemConfig flagConfig = entry.getValue();

                contentItems.add(new GuiItem() {
                    @Override
                    public ItemStack render(Player viewer) {
                        boolean hasFlag = role.hasFlag(flagId);
                        String statusText = hasFlag ? config.enabledStatus : config.disabledStatus;
                        return GuiHelper.buildToggleFlagItem(flagConfig.material, flagConfig.name,
                                flagConfig.lore, statusText, hasFlag);
                    }

                    @Override
                    public ClickAction clickAction() {
                        return (p, e) -> {
                            if (role.hasFlag(flagId)) {
                                role.removeFlag(flagId);
                            } else {
                                role.addFlag(flagId);
                            }

                            // If this isn't a new pending role, save it to db immediately
                            if (!isNew) {
                                plugin.getDatabaseManager().getProfileDao().saveProfile(profile)
                                        .thenRun(() -> {
                                            if (plugin.getRedisManager() != null) {
                                                plugin.getRedisManager().publishUpdate("INVALIDATE_PROFILE",
                                                        profile.getProfileId());
                                            }
                                        });
                            }

                            // Refresh page
                            if (e.getView().getTopInventory().getHolder() instanceof PaginatedGui currentGui) {
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
                    "P B B B < B B B N",
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
            ingredients.put('I', GuiHelper.buildSlot(config.info.material,
                    config.info.name.replace("<role>", role.getName()), config.info.lore));
            ingredients.put('B',
                    GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name, config.bottomFill.lore));
            ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                    (p, e) -> RoleSetupGUI.open(p, profile, plugin, role)));

            Component title = GuiHelper.MM.deserialize(config.title.replace("<role>", role.getName()));
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
