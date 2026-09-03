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
            List<GuiItem> contentItems = new ArrayList<>();

            for (Map.Entry<UUID, Warp> entry : matches) {
                UUID ownerId = entry.getKey();
                Warp warp = entry.getValue();

                String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
                if (ownerName == null) {
                    ownerName = ownerId.toString();
                }
                final String finalOwnerName = ownerName;

                ClaimProfile profile = plugin.getClaimManager().getProfile(ownerId);
                String claimName = profile != null && profile.getName() != null ? profile.getName() : "Claim";

                String locStr = warp.getLocation().getWorld().getName() + " ("
                        + warp.getLocation().getBlockX() + ", "
                        + warp.getLocation().getBlockY() + ", "
                        + warp.getLocation().getBlockZ() + ")";

                contentItems.add(new GuiItem() {
                    @Override
                    public ItemStack render(Player viewer) {
                        return GuiHelper.buildItemStack(
                                warp.getIcon().name(),
                                "<gold>" + warp.getName() + " <gray>(by <yellow>" + finalOwnerName + "<gray>)",
                                List.of(
                                        "<gray>Owner: <yellow>" + finalOwnerName,
                                        "<gray>Claim: <aqua>" + claimName,
                                        "<gray>Location: <white>" + locStr,
                                        "",
                                        "<green>Left-Click to teleport"
                                )
                        );
                    }

                    @Override
                    public ClickAction clickAction() {
                        return (p, e) -> {
                            if (e.getClick() == ClickType.LEFT) {
                                p.closeInventory();
                                if (plugin.getCombatManager().isInCombat(p)) {
                                    p.sendMessage(plugin.getConfigManager().getMessage("in-combat"));
                                    return;
                                }
                                p.teleportAsync(warp.getLocation()).thenAccept(success -> {
                                    if (success) {
                                        p.sendMessage(plugin.getConfigManager().getMessage(
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
                    "x x x x x x x x x",
                    "x x x x x x x x x",
                    "x x x x x x x x x",
                    "P n n n X n n n N"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('n', GuiHelper.buildSlot("GRAY_STAINED_GLASS_PANE", " ", List.of()));
            ingredients.put('X', GuiHelper.buildSlot("BARRIER", "<red>Close", List.of("<gray>Click to close"), (p, e) -> p.closeInventory()));

            Component title = GuiHelper.MM.deserialize("<gold>Select Warp: <yellow>" + warpName);
            PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

            gui.setPrevButton(27,
                    GuiHelper.buildItemStack("ARROW", "<yellow>Previous Page", List.of()),
                    GuiHelper.buildItemStack("GRAY_STAINED_GLASS_PANE", " ", List.of()));
            gui.setNextButton(35,
                    GuiHelper.buildItemStack("ARROW", "<yellow>Next Page", List.of()),
                    GuiHelper.buildItemStack("GRAY_STAINED_GLASS_PANE", " ", List.of()));

            FoliaScheduler.runTask(plugin, () -> {
                gui.setContent(contentItems, player);
                gui.open(player);
            });
        });
    }
}
