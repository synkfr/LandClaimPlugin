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

        // Only handle clicks in the top inventory (GUI), not player's inventory
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        String title = event.getView().getTitle();
        
        // Trust List GUI handling - check first to avoid conflicts
        String trustListTitle = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-list-title"));
        if (title.equals(trustListTitle)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            if (event.getSlot() == event.getInventory().getSize() - 1) {
                player.closeInventory();
                return;
            }

            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                if (meta != null) {
                    OfflinePlayer trustedPlayer = meta.getOwningPlayer();
                    if (trustedPlayer != null) {
                        TrustMenuGUI.open(player, trustedPlayer, trustManager);
                    }
                }
            }
            return;
        }

        // Visitor menu handling
        String visitorMenuTitle = ChatUtils.colorize(trustManager.getConfigManager().getMessage("visitor-menu-title"));
        if (title.equals(visitorMenuTitle)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            if (event.getSlot() == 8) {
                // Back button - close inventory
                player.closeInventory();
                return;
            }

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
            return;
        }

        // Trust menu handling
        String trustMenuTitleTemplate = trustManager.getConfigManager().getMessage("trust-menu-title", "{player}", "");
        String trustPrefix = ChatUtils.colorize(trustMenuTitleTemplate);
        if (title.startsWith(trustPrefix)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            // Extract player name from title
            String trustedName = ChatColor.stripColor(title.substring(trustPrefix.length()).trim());
            if (trustedName.isEmpty()) return;

            // Try to find the player (online or offline)
            OfflinePlayer trustedPlayer = null;
            Player onlinePlayer = plugin.getServer().getPlayerExact(trustedName);
            if (onlinePlayer != null) {
                trustedPlayer = onlinePlayer;
            } else {
                // Try to find offline player
                for (OfflinePlayer offline : plugin.getServer().getOfflinePlayers()) {
                    if (offline.getName() != null && offline.getName().equals(trustedName)) {
                        trustedPlayer = offline;
                        break;
                    }
                }
            }

            if (trustedPlayer == null) return;

            // Handle back button
            if (event.getSlot() == 8) {
                TrustListGUI.open(player, trustManager);
                return;
            }

            // Handle permission toggles
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
    }
}