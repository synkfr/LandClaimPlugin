package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.MemberManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class MemberManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                if (!GuiHelper.checkMenuPermission(player, "members", plugin)) {
                        return;
                }
                FoliaScheduler.runAsync(plugin, () -> {
                        MemberManagementConfig config = plugin.getConfigManager().getMemberManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Build player head items for each member
                        for (Map.Entry<UUID, String> entry : profile.getMemberRoles().entrySet()) {
                                UUID memberId = entry.getKey();
                                String role = entry.getValue();

                                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                                if (memberName == null)
                                        memberName = memberId.toString();
                                final String displayName = memberName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                var cm = plugin.getConfigManager();
                                                List<Component> lore = List.of(
                                                                cm.getRawComponent("gui.member-role-line", "<role>", role),
                                                                Component.empty(),
                                                                cm.getRawComponent("gui.click-manage"));
                                                return GuiHelper.buildPlayerHead(
                                                                memberId,
                                                                displayName,
                                                                cm.getRawComponent("gui.skull-item-name", "<name>", displayName),
                                                                lore);
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> PlayerControlPanelGUI.open(p, profile, plugin, memberId, displayName);
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
                        ingredients.put('+', GuiHelper.buildSlot(config.inviteMember.material, config.inviteMember.name,
                                        config.inviteMember.lore, (p, e) -> {
                                                OnlinePlayerSelectorGUI.open(p, plugin, target -> {
                                                        // Callback: target selected
                                                        var cm = plugin.getConfigManager();
                                                        if (profile.isOwner(target.getUniqueId())) {
                                                                p.sendMessage(cm.getMessage("cannot-invite-self"));
                                                                return;
                                                        }
                                                        if (profile.isMember(target.getUniqueId())) {
                                                                p.sendMessage(cm.getMessage("already-member"));
                                                                return;
                                                        }
                                                        if (!plugin.getClaimManager().canCreateProfile(target.getUniqueId())) {
                                                                p.sendMessage(cm.getMessage("target-already-in-claim", "<player>", target.getName()));
                                                                return;
                                                        }
                                                        int maxMembers = cm.getPluginConfig().limits.maxClaimMembers + profile.getBonusMemberSlots();
                                                        if (profile.getMemberRoles().size() >= maxMembers && !p.hasPermission("landclaim.admin")) {
                                                                p.sendMessage(cm.getMessage("members.limit-reached", "<limit>", String.valueOf(maxMembers)));
                                                                return;
                                                        }

                                                        plugin.getClaimManager().sendMemberInvite(p, target, profile);
                                                        MemberManagementGUI.open(p, profile, plugin);
                                                }, () -> MemberManagementGUI.open(p, profile, plugin));
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
