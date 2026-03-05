package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.VisitorSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Per-player permission overrides for a trusted player.
 * Uses the same flag catalog as VisitorSettingsGUI but toggles on
 * profile.getTrustedFlags(targetPlayerId) instead of visitor flags.
 */
public class PlayerTrustPermissionGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, UUID targetPlayerId) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        // Reuse the same flag catalog as visitor settings
                        VisitorSettingsConfig config = plugin.getConfigManager().getVisitorSettingsConfig();

                        String targetName = Bukkit.getOfflinePlayer(targetPlayerId).getName();
                        if (targetName == null)
                                targetName = targetPlayerId.toString();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Ensure the trusted player has a flag set
                        Set<String> playerFlags = profile.getTrustedFlags(targetPlayerId);
                        if (playerFlags == null) {
                                playerFlags = new HashSet<>();
                                profile.setTrustedFlags(targetPlayerId, playerFlags);
                        }
                        final Set<String> flags = playerFlags;

                        for (Map.Entry<String, VisitorSettingsConfig.ItemConfig> entry : config.flags.entrySet()) {
                                String flagId = entry.getKey();
                                VisitorSettingsConfig.ItemConfig flagConfig = entry.getValue();

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                boolean hasFlag = flags.contains(flagId.toUpperCase());
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
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        String upper = flagId.toUpperCase();
                                                        if (flags.contains(upper)) {
                                                                flags.remove(upper);
                                                        } else {
                                                                flags.add(upper);
                                                        }

                                                        // Save asynchronously
                                                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile)
                                                                        .thenRun(() -> {
                                                                                if (plugin.getRedisManager() != null) {
                                                                                        plugin.getRedisManager()
                                                                                                        .publishUpdate("INVALIDATE_CLAIM",
                                                                                                                        profile.getOwnerId());
                                                                                }
                                                                        });

                                                        // Refresh page
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

                        // Reuse the PlayerTrustPermissionConfig for frame/info/nav elements
                        var ptConfig = plugin.getConfigManager().getPlayerTrustPermissionConfig();

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(ptConfig.frame.material, ptConfig.frame.name,
                                        ptConfig.frame.lore));
                        ingredients.put('I', GuiHelper.buildSlot(ptConfig.info.material, ptConfig.info.name,
                                        ptConfig.info.lore));
                        ingredients.put('B', GuiHelper.buildSlot(ptConfig.bottomFill.material, ptConfig.bottomFill.name,
                                        ptConfig.bottomFill.lore));
                        ingredients.put('<', GuiHelper.buildSlot(ptConfig.back.material, ptConfig.back.name,
                                        ptConfig.back.lore, (p, e) -> {
                                                p.closeInventory();
                                                TrustManagementGUI.open(p, profile, plugin);
                                        }));

                        Component title = GuiHelper.MM.deserialize(ptConfig.title);
                        PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

                        gui.setPrevButton(45,
                                        GuiHelper.buildItemStack(ptConfig.previousPage.material,
                                                        ptConfig.previousPage.name,
                                                        ptConfig.previousPage.lore),
                                        GuiHelper.buildItemStack(ptConfig.bottomFill.material, ptConfig.bottomFill.name,
                                                        ptConfig.bottomFill.lore));
                        gui.setNextButton(53,
                                        GuiHelper.buildItemStack(ptConfig.nextPage.material, ptConfig.nextPage.name,
                                                        ptConfig.nextPage.lore),
                                        GuiHelper.buildItemStack(ptConfig.bottomFill.material, ptConfig.bottomFill.name,
                                                        ptConfig.bottomFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
