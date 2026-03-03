package org.ayosynk.landClaimPlugin.gui;

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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        RoleManagementConfig config = plugin.getConfigManager().getRoleManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Build items for each role in the profile
                        for (Role role : profile.getRoles().values()) {
                                final String roleName = role.getName();
                                final boolean isDefault = roleName.equalsIgnoreCase("Member")
                                                || roleName.equalsIgnoreCase("CoOwner");

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                // Use NAME_TAG for default roles, PAPER for custom roles
                                                Material mat = isDefault ? Material.NAME_TAG : Material.PAPER;
                                                ItemStack item = new ItemStack(mat);
                                                ItemMeta meta = item.getItemMeta();
                                                if (meta != null) {
                                                        String color = isDefault ? "<gold>" : "<yellow>";
                                                        meta.displayName(GuiHelper.MM.deserialize(color + roleName));

                                                        List<Component> lore = new ArrayList<>();
                                                        if (isDefault) {
                                                                lore.add(GuiHelper.MM.deserialize(
                                                                                "<gray><i>Default Role</i>"));
                                                        }
                                                        lore.add(Component.empty());
                                                        lore.add(GuiHelper.MM
                                                                        .deserialize("<gray>Left-click: Edit Role"));

                                                        // Only custom roles can be deleted
                                                        if (!isDefault) {
                                                                lore.add(GuiHelper.MM.deserialize(
                                                                                "<gray>Right-click: Delete Role"));
                                                        } else {
                                                                lore.add(GuiHelper.MM.deserialize(
                                                                                "<dark_gray><st>Right-click: Delete Role</st>"));
                                                        }

                                                        meta.lore(lore);
                                                        item.setItemMeta(meta);
                                                }
                                                return item;
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        if (e.getClick() == ClickType.RIGHT) {
                                                                if (isDefault) {
                                                                        p.sendMessage(GuiHelper.MM.deserialize(
                                                                                        "<red>You cannot delete default roles."));
                                                                        return;
                                                                }

                                                                // Delete Role Confirmation
                                                                Bukkit.getScheduler().runTask(plugin, () -> {
                                                                        ConfirmationGUI.open(p,
                                                                                        "<red>Delete role " + roleName
                                                                                                        + "?",
                                                                                        () -> {
                                                                                                // 1. Remove role from
                                                                                                // profile
                                                                                                profile.removeRole(
                                                                                                                roleName);

                                                                                                // 2. Unassign this role
                                                                                                // from any members who
                                                                                                // had it
                                                                                                boolean changedMember = false;
                                                                                                for (Map.Entry<java.util.UUID, String> memberEntry : profile
                                                                                                                .getMemberRoles()
                                                                                                                .entrySet()) {
                                                                                                        if (memberEntry.getValue()
                                                                                                                        .equalsIgnoreCase(
                                                                                                                                        roleName)) {
                                                                                                                // Fallback
                                                                                                                // to
                                                                                                                // Member
                                                                                                                profile.setMemberRole(
                                                                                                                                memberEntry.getKey(),
                                                                                                                                "Member");
                                                                                                                changedMember = true;
                                                                                                        }
                                                                                                }

                                                                                                // 3. Save
                                                                                                plugin.getCacheManager()
                                                                                                                .getProfileCache()
                                                                                                                .put(profile.getOwnerId(),
                                                                                                                                profile);
                                                                                                plugin.getClaimManager()
                                                                                                                .saveAndSync(profile);

                                                                                                p.sendMessage(GuiHelper.MM
                                                                                                                .deserialize("<yellow>Deleted role "
                                                                                                                                + roleName
                                                                                                                                + "."));
                                                                                                RoleManagementGUI.open(
                                                                                                                p,
                                                                                                                profile,
                                                                                                                plugin);
                                                                                        },
                                                                                        () -> RoleManagementGUI.open(p,
                                                                                                        profile,
                                                                                                        plugin));
                                                                });
                                                        } else {
                                                                // Edit Role
                                                                p.closeInventory();
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
                                        "P N N C < N N N V"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('N', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('C', GuiHelper.buildSlot(config.createRole.material, config.createRole.name,
                                        config.createRole.lore, (p, e) -> {
                                                // Check claim role limit permission: landclaim.createrole.<number>
                                                int maxRoles = 2; // Default allows 0 custom roles (2 defaults taking up
                                                                  // the slots)
                                                for (org.bukkit.permissions.PermissionAttachmentInfo pai : p
                                                                .getEffectivePermissions()) {
                                                        if (pai.getPermission().startsWith("landclaim.createrole.")) {
                                                                try {
                                                                        int limit = Integer.parseInt(pai.getPermission()
                                                                                        .substring("landclaim.createrole."
                                                                                                        .length()));
                                                                        // limit is extra custom roles, total = limit +
                                                                        // 2
                                                                        if (limit + 2 > maxRoles) {
                                                                                maxRoles = limit + 2;
                                                                        }
                                                                } catch (NumberFormatException ignored) {
                                                                }
                                                        }
                                                }

                                                if (profile.getRoles().size() >= maxRoles) {
                                                        p.sendMessage(GuiHelper.MM.deserialize(
                                                                        "<red>You have reached your maximum role limit ("
                                                                                        + (maxRoles - 2)
                                                                                        + " custom roles). Upgrade your rank to create more."));
                                                        return;
                                                }

                                                p.closeInventory();
                                                // Open setup in create mode (null role)
                                                RoleSetupGUI.open(p, profile, plugin, null);
                                        }));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

                        gui.setPrevButton(27,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
