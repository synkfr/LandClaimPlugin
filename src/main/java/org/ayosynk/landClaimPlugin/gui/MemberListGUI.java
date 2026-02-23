package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import java.util.Map;
import java.util.UUID;

public class MemberListGUI {

    public static void open(Player player, Claim claim, LandClaimPlugin plugin) {
        List<Item> items = new ArrayList<>();

        // Add owner as a special item
        OfflinePlayer owner = Bukkit.getOfflinePlayer(claim.getOwnerId());
        items.add(Item.simple(new ItemBuilder(Material.PLAYER_HEAD)
                .setLegacyName("§6" + (owner.getName() != null ? owner.getName() : "Unknown"))
                .addLegacyLoreLines("§eRole: §cOwner")));

        for (Map.Entry<UUID, String> entry : claim.getPlayerRoles().entrySet()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(entry.getKey());
            String roleName = entry.getValue();

            items.add(Item.builder().setItemProvider(new ItemBuilder(Material.PLAYER_HEAD)
                    .setLegacyName("§b" + (member.getName() != null ? member.getName() : "Unknown"))
                    .addLegacyLoreLines("§eRole: §7" + roleName, "", "§cClick to edit/remove"))
                    .addClickHandler(click -> {
                        // Open Role Selector GUI for this player
                        RoleSelectorGUI.open(player, claim, entry.getKey(), plugin);
                    }).build());
        }

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
                                .addClickHandler(click -> MainMenuGUI.open(player, claim, plugin)).build())
                .setContent(items)
                .build();

        Window.builder()
                .setTitle("Claim Members")
                .setUpperGui(gui)
                .open(player);
    }
}
