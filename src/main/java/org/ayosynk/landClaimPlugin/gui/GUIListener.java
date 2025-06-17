package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIListener implements Listener {
    private final TrustManager trustManager;
    private final LandClaimPlugin plugin;

    public GUIListener(TrustManager trustManager) {
        this.trustManager = trustManager;
        this.plugin = trustManager.getPlugin(); // Use getPlugin method
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        Inventory inv = event.getInventory();

        // Trust menu handling
        String trustPrefix = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-menu-title", "{player}", ""));
        if (title.startsWith(trustPrefix)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            String trustedName = ChatColor.stripColor(title.replace(trustPrefix, ""));
            Player trustedPlayer = plugin.getServer().getPlayer(trustedName);
            if (trustedPlayer == null) return;

            if (event.getRawSlot() == 8) {
                plugin.getCommandHandler().showTrustList(player);
                return;
            }

            if (event.getSlot() < 4) {
                String permission = TrustMenuGUI.PERMISSIONS[event.getSlot()];
                boolean current = trustManager.hasTrustPermission(
                        player.getUniqueId(),
                        trustedPlayer.getUniqueId(),
                        permission
                );

                trustManager.setTrustPermission(
                        player.getUniqueId(),
                        trustedPlayer.getUniqueId(),
                        permission,
                        !current
                );

                trustManager.savePermissionsAndMembers();

                TrustMenuGUI.open(player, trustedPlayer, trustManager);
            }
        }
        // Visitor menu handling
        else if (title.equals(ChatUtils.colorize(trustManager.getConfigManager().getMessage("visitor-menu-title")))) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            if (event.getSlot() < 4) {
                String permission = VisitorMenuGUI.PERMISSIONS[event.getSlot()];
                boolean current = trustManager.hasVisitorPermission(
                        player.getUniqueId(),
                        permission
                );

                trustManager.setVisitorPermission(
                        player.getUniqueId(),
                        permission,
                        !current
                );

                trustManager.savePermissionsAndMembers();

                VisitorMenuGUI.open(player, trustManager);
            }
        }
        // Trust List GUI handling
        String trustListTitle = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-list-title"));
        if (title.equals(trustListTitle)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            if (event.getSlot() == event.getInventory().getSize() - 1) {
                player.closeInventory();
                return;
            }

            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                OfflinePlayer trustedPlayer = meta.getOwningPlayer();
                if (trustedPlayer != null) {
                    Player onlineTrusted = trustedPlayer.getPlayer();
                    if (onlineTrusted != null) {
                        TrustMenuGUI.open((Player) event.getWhoClicked(), onlineTrusted, trustManager);
                    } else {
                        TrustMenuGUI.open((Player) event.getWhoClicked(), trustedPlayer, trustManager);
                    }
                }
            }
        }
    }
}