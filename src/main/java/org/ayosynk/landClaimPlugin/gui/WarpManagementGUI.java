package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.WarpManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarpManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "warps", plugin)) {
                        return;
                }
                ConfigManager cm = plugin.getConfigManager();
                WarpManagementConfig config = cm.getWarpManagementConfig();

                List<GuiItem> contentItems = new ArrayList<>();
                for (Warp warp : profile.getWarps().values()) {
                        contentItems.add(new GuiItem() {
                                @Override
                                public ItemStack render(Player viewer) {
                                        return GuiHelper.buildItem(
                                                        warp.getIcon(),
                                                        cm.getRawComponent("gui.warp-item-name", "<name>", warp.getName()),
                                                        List.of(
                                                                        cm.getRawComponent("gui.warp-teleport-hint"),
                                                                        cm.getRawComponent("gui.warp-manage-hint")));
                                }

                                @Override
                                public ClickAction clickAction() {
                                        return (p, e) -> {
                                                if (e.getClick() == ClickType.RIGHT) {
                                                        WarpControlPanelGUI.open(p, profile, plugin, warp);
                                                } else {
                                                        p.closeInventory();
                                                        p.teleportAsync(warp.getLocation()).thenAccept(success -> {
                                                                if (success) {
                                                                        p.sendMessage(cm.getMessage("warp-teleport",
                                                                                        "<name>", warp.getName()));
                                                                }
                                                        });
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
                                "n P n n < n n N n"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                ingredients.put('n', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
                ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> MainMenuGUI.open(p, profile, plugin)));

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
                if (contentItems.isEmpty()) {
                        gui.setItem(22, GuiHelper.buildItemStack(config.emptyIndicator.material, config.emptyIndicator.name, config.emptyIndicator.lore));
                }
                gui.open(player);
        }
}
