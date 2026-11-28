package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleSelectionConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoleSelectionGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, UUID targetPlayerId,
                        String targetPlayerName) {
                var cm = plugin.getConfigManager();
                RoleSelectionConfig config = cm.getRoleSelectionConfig();

                List<GuiItem> contentItems = new ArrayList<>();

                for (Role role : profile.getRoles().values()) {
                        final String roleName = role.getName();
                        final boolean isDefault = roleName.equalsIgnoreCase("Member")
                                        || roleName.equalsIgnoreCase("CoOwner");

                        contentItems.add(new GuiItem() {
                                @Override
                                public ItemStack render(Player viewer) {
                                        Material mat = isDefault ? Material.NAME_TAG : Material.PAPER;
                                        Component displayName = cm.getRawComponent(
                                                        isDefault ? "gui.role-default-name" : "gui.role-custom-name",
                                                        "<role>", roleName);
                                        List<Component> lore = new ArrayList<>();
                                        if (isDefault) {
                                                lore.add(cm.getRawComponent("gui.default-role-badge"));
                                        }
                                        lore.add(Component.empty());
                                        lore.add(cm.getRawComponent("gui.click-assign-role", "<player>", targetPlayerName));
                                        return GuiHelper.buildItem(mat, displayName, lore);
                                }

                                @Override
                                public ClickAction clickAction() {
                                        return (p, e) -> {
                                                profile.setMemberRole(targetPlayerId, roleName);

                                                plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                                                plugin.getClaimManager().saveAndSync(profile);

                                                p.sendMessage(cm.getMessage("gui.role-assigned-player",
                                                                "<player>", targetPlayerName,
                                                                "<role>", roleName));
                                                PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName);
                                        };
                                }
                        });
                }

                String[] structure = {
                                "F F F F F F F F F",
                                "F x x x x x x x F",
                                "F x x x x x x x F",
                                "P B B B < B B B N"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
                ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> PlayerControlPanelGUI.open(p, profile, plugin, targetPlayerId, targetPlayerName)));

                Component title = GuiHelper.MM.deserialize(config.title.replace("<Player>", targetPlayerName));
                PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

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
