package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoleSelectorGUI {

    public static void open(Player player, Claim claim, UUID targetPlayerId, LandClaimPlugin plugin) {
        List<Item> items = new ArrayList<>();

        String currentRole = claim.getPlayerRole(targetPlayerId);

        // Add "Remove Role" button if they have a role
        items.add(Item.builder().setItemProvider(new ItemBuilder(Material.RED_DYE)
                .setLegacyName("§cRemove Role")
                .addLegacyLoreLines("§7Remove all permissions from this player"))
                .addClickHandler(click -> {
                    plugin.getTrustManager().removeRoleFromPlayer(claim, targetPlayerId);
                    player.sendMessage(plugin.getConfigManager().getMessage("role-removed"));
                    MemberListGUI.open(player, claim, plugin);
                }).build());

        // Fetch roles from cache, sorted by priority
        List<Role> roles = new ArrayList<>(plugin.getCacheManager().getRoleCache().asMap().values());
        roles.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));

        String targetName = Bukkit.getOfflinePlayer(targetPlayerId).getName();

        // Target player info
        items.add(Item.simple(new ItemBuilder(Material.PLAYER_HEAD)
                .setLegacyName("§b" + (targetName != null ? targetName : "Unknown"))
                .addLegacyLoreLines("§7Target Player")));

        // Available roles - click to assign
        for (Role role : roles) {
            boolean isCurrent = role.getName().equalsIgnoreCase(currentRole);
            Material mat = isCurrent ? Material.LIME_DYE : Material.GRAY_DYE;

            items.add(Item.builder().setItemProvider(new ItemBuilder(mat)
                    .setLegacyName("§eRole: §6" + role.getName())
                    .addLegacyLoreLines(
                            "§7Priority: " + role.getPriority(),
                            "§7Flags: " + String.join(", ", role.getFlags()),
                            "",
                            isCurrent ? "§aCurrent Role" : "§7Click to assign this role"))
                    .addClickHandler(click -> {
                        if (!isCurrent) {
                            plugin.getTrustManager().addRoleToPlayer(claim, targetPlayerId, role.getName());
                            player.sendMessage(plugin.getConfigManager().getMessage("role-assigned")
                                    .replace("{role}", role.getName()));
                        }
                        // Refresh GUI
                        RoleSelectorGUI.open(player, claim, targetPlayerId, plugin);
                    }).build());
        }

        // Untrust item
        items.add(Item.builder().setItemProvider(new ItemBuilder(Material.RED_WOOL)
                .setLegacyName("§cRemove all roles (Untrust)")
                .addLegacyLoreLines("§7Click to untrust this player"))
                .addClickHandler(click -> {
                    plugin.getTrustManager().removeRoleFromPlayer(claim, targetPlayerId); // Changed to
                                                                                          // removeRoleFromPlayer
                    // Go back to member list
                    MemberListGUI.open(player, claim, plugin);
                }).build());

        BoundItem.Builder<?> back = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW).setLegacyName("§aPrevious Page"))
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1));

        BoundItem.Builder<?> forward = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW).setLegacyName("§aNext Page"))
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1));

        Gui gui = PagedGui.itemsBuilder()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "# # < # B # > # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', Item.simple(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setLegacyName(" ")))
                .addIngredient('<', back)
                .addIngredient('>', forward)
                .addIngredient('B',
                        Item.builder().setItemProvider(new ItemBuilder(Material.BARRIER).setLegacyName("§cBack"))
                                .addClickHandler(click -> MemberListGUI.open(player, claim, plugin)).build())
                .setContent(items)
                .build();

        Window.builder()
                .setTitle("Select Role: " + (targetName != null ? targetName : "Unknown"))
                .setUpperGui(gui)
                .open(player);
    }
}
