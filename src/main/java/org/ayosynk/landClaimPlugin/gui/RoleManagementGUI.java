package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "roles", plugin)) {
                        return;
                }
                RoleManagementConfig config = plugin.getConfigManager().getRoleManagementConfig();

                List<GuiItem> contentItems = new ArrayList<>();

                List<Role> sortedRoles = new ArrayList<>(profile.getRoles().values());
                sortedRoles.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
                var cm = plugin.getConfigManager();
                for (Role role : sortedRoles) {
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
                                        lore.add(cm.getRawComponent("gui.click-edit-role"));
                                        lore.add(cm.getRawComponent(
                                                        isDefault ? "gui.click-delete-role-disabled" : "gui.click-delete-role"));
                                        return GuiHelper.buildItem(mat, displayName, lore);
                                }

                                @Override
                                public ClickAction clickAction() {
                                        return (p, e) -> {
                                                if (e.getClick() == ClickType.RIGHT) {
                                                        if (isDefault) {
                                                                p.sendMessage(cm.getMessage("gui.cannot-delete-default-role"));
                                                                return;
                                                        }

                                                        ConfirmationGUI.open(p,
                                                                        cm.getRawTemplate("gui.confirm-delete-role", "<role>", roleName),
                                                                        () -> {
                                                                                profile.removeRole(roleName);
                                                                                for (Map.Entry<java.util.UUID, String> memberEntry : profile
                                                                                                .getMemberRoles()
                                                                                                .entrySet()) {
                                                                                        if (memberEntry.getValue().equalsIgnoreCase(roleName)) {
                                                                                                profile.setMemberRole(memberEntry.getKey(), "Member");
                                                                                        }
                                                                                }

                                                                                plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                                                                                plugin.getClaimManager().saveAndSync(profile);

                                                                                p.sendMessage(cm.getMessage("gui.role-deleted", "<role>", roleName));
                                                                                RoleManagementGUI.open(p, profile, plugin);
                                                                        },
                                                                        () -> RoleManagementGUI.open(p, profile, plugin));
                                                } else {
                                                        RoleSetupGUI.open(p, profile, plugin, role);
                                                }
                                        };
                                }
                        });
                }

                String[] structure = {
                                "F F F F F F F F F",
                                "F x x x x x x x F",
                                "F x x x x x x x F",
                                "P b b C < b b b N"
                };

                Map<Character, SlotDefinition> ingredients = new HashMap<>();
                ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
                ingredients.put('b', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
                ingredients.put('C', GuiHelper.buildSlot(config.createRole.material, config.createRole.name,
                                config.createRole.lore, (p, e) -> {
                                        int maxRoles = 2;
                                        for (org.bukkit.permissions.PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
                                                if (pai.getPermission().startsWith("landclaim.createrole.")) {
                                                        try {
                                                                int limit = Integer.parseInt(pai.getPermission()
                                                                                .substring("landclaim.createrole.".length()));
                                                                if (limit + 2 > maxRoles) {
                                                                        maxRoles = limit + 2;
                                                                }
                                                        } catch (NumberFormatException ignored) {
                                                        }
                                                }
                                        }
                                        maxRoles += profile.getBonusRoleSlots();

                                        if (profile.getRoles().size() >= maxRoles && !profile.getProfileId().equals(ClaimProfile.ADMIN_PROFILE_ID)) {
                                                p.sendMessage(cm.getMessage("gui.role-limit-reached",
                                                                "<limit>", String.valueOf(maxRoles - 2)));
                                                return;
                                        }

                                        RoleSetupGUI.open(p, profile, plugin, null);
                                }));
                ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                (p, e) -> ClaimSettingsGUI.open(p, profile, plugin)));

                Component title = GuiHelper.MM.deserialize(config.title);
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
