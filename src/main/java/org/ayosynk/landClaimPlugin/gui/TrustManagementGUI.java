package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.TrustManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class TrustManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "trusted", plugin)) {
                        return;
                }
                FoliaScheduler.runAsync(plugin, () -> {
                        TrustManagementConfig config = plugin.getConfigManager().getTrustManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Build player head items for each trusted player
                        for (UUID trustedId : profile.getTrustedPlayerFlags().keySet()) {
                                String trustedName = Bukkit.getOfflinePlayer(trustedId).getName();
                                if (trustedName == null)
                                        trustedName = trustedId.toString();
                                final String displayName = trustedName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                var cm = plugin.getConfigManager();
                                                List<Component> lore = List.of(
                                                                cm.getRawComponent("gui.click-edit-permissions"),
                                                                cm.getRawComponent("gui.click-remove"));
                                                return GuiHelper.buildPlayerHead(
                                                                trustedId,
                                                                displayName,
                                                                cm.getRawComponent("gui.skull-item-name", "<name>", displayName),
                                                                lore);
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.RIGHT) {
                                                                ConfirmationGUI.open(p,
                                                                                plugin.getConfigManager().getRawTemplate("gui.confirm-remove-trust", "<player>", displayName),
                                                                                () -> {
                                                                                        profile.removeTrustedPlayer(trustedId);
                                                                                        plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                                                                                        plugin.getClaimManager().saveAndSync(profile);
                                                                                        TrustManagementGUI.open(p, profile, plugin);
                                                                                },
                                                                                () -> TrustManagementGUI.open(p, profile, plugin));
                                                        } else {
                                                                PlayerTrustPermissionGUI.open(p, profile, plugin, trustedId);
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
                                        "B P B + < B B N B"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name,
                                        config.bottomFill.lore));
                        ingredients.put('+', GuiHelper.buildSlot(config.addPlayer.material, config.addPlayer.name,
                                        config.addPlayer.lore, (p, e) -> {
                                                OnlinePlayerSelectorGUI.open(p, plugin, target -> {
                                                        // Callback: target selected
                                                        if (profile.isOwner(target.getUniqueId())) {
                                                                p.sendMessage(plugin.getConfigManager()
                                                                                .getMessage("cannot-trust-self"));
                                                                return;
                                                        }
                                                        if (profile.isTrusted(target.getUniqueId())) {
                                                                p.sendMessage(plugin.getConfigManager()
                                                                                .getMessage("already-trusted"));
                                                                return;
                                                        }

                                                        plugin.getClaimManager().sendTrustInvite(p, target, profile);
                                                        TrustManagementGUI.open(p, profile, plugin);
                                                }, () -> TrustManagementGUI.open(p, profile, plugin));
                                        }));
                        ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                        (p, e) -> MainMenuGUI.open(p, profile, plugin)));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

                        ItemStack navFiller = GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                        config.bottomFill.lore);
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
