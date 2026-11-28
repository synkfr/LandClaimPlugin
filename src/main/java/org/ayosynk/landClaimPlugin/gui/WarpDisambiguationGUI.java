package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarpDisambiguationGUI {

    public static void open(Player player, String warpName, List<Map.Entry<UUID, Warp>> matches, LandClaimPlugin plugin) {
        FoliaScheduler.runAsync(plugin, () -> {
            var cm = plugin.getConfigManager();
            List<GuiItem> contentItems = new ArrayList<>();

            for (Map.Entry<UUID, Warp> entry : matches) {
                UUID ownerId = entry.getKey();
                Warp warp = entry.getValue();

                ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
                String ownerName = profile != null
                        ? profile.getDisplayOwnerName()
                        : Bukkit.getOfflinePlayer(ownerId).getName();
                if (ownerName == null) {
                    ownerName = ownerId.toString();
                }
                final String finalOwnerName = ownerName;

                String claimName = profile != null && profile.getName() != null
                        ? profile.getName()
                        : cm.getRawTemplate("gui.default-unnamed-claim");

                String locStr = warp.getLocation().getWorld().getName() + " ("
                        + warp.getLocation().getBlockX() + ", "
                        + warp.getLocation().getBlockY() + ", "
                        + warp.getLocation().getBlockZ() + ")";

                contentItems.add(new GuiItem() {
                    @Override
                    public ItemStack render(Player viewer) {
                        return GuiHelper.buildItemStack(
                                warp.getIcon().name(),
                                cm.getRawTemplate("gui.warp-item-display",
                                        "<name>", warp.getName(),
                                        "<owner>", finalOwnerName),
                                List.of(
                                        cm.getRawTemplate("gui.warp-owner-line", "<owner>", finalOwnerName),
                                        cm.getRawTemplate("gui.warp-claim-line", "<claim>", claimName),
                                        cm.getRawTemplate("gui.warp-location-line", "<location>", locStr),
                                        "",
                                        cm.getRawTemplate("gui.warp-teleport-hint")
                                )
                        );
                    }

                    @Override
                    public ClickAction clickAction() {
                        return (p, e) -> {
                            if (e.getClick() == ClickType.LEFT) {
                                p.closeInventory();
                                if (plugin.getCombatManager().isInCombat(p)) {
                                    p.sendMessage(cm.getMessage("in-combat"));
                                    return;
                                }
                                p.teleportAsync(warp.getLocation()).thenAccept(success -> {
                                    if (success) {
                                        p.sendMessage(cm.getMessage(
                                                "publicwarps-teleported",
                                                "<owner>", finalOwnerName,
                                                "<name>", warp.getName()
                                        ));
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
                    "n P n n X n n N n"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot("BLACK_STAINED_GLASS_PANE", " ", List.of()));
            ingredients.put('n', GuiHelper.buildSlot("GRAY_STAINED_GLASS_PANE", " ", List.of()));
            ingredients.put('X', GuiHelper.buildSlot("BARRIER",
                    cm.getRawTemplate("gui.warp-close-name"),
                    List.of(cm.getRawTemplate("gui.warp-close-lore")),
                    (p, e) -> p.closeInventory()));

            Component title = cm.getRawComponent("gui.warp-select-title", "<name>", warpName);
            PaginatedGui gui = new PaginatedGui(title, 5, structure, ingredients, 'x');

            ItemStack navFiller = GuiHelper.buildItemStack("GRAY_STAINED_GLASS_PANE", " ", List.of());
            gui.setPrevButton(
                    GuiHelper.buildItemStack("ARROW", cm.getRawTemplate("gui.warp-prev-name"), List.of()),
                    navFiller);
            gui.setNextButton(
                    GuiHelper.buildItemStack("ARROW", cm.getRawTemplate("gui.warp-next-name"), List.of()),
                    navFiller);

            FoliaScheduler.runForPlayer(plugin, player, () -> {
                gui.setContent(contentItems, player);
                gui.open(player);
            });
        });
    }
}
