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
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        String title = event.getView().getTitle();

        String trustListTitle = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-list-title"));
        if (title.equals(trustListTitle)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR)
                return;

            if (item.getType() == Material.GRAY_STAINED_GLASS_PANE)
                return;

            if (event.getSlot() == 49) {
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

        String visitorMenuTitle = ChatUtils.colorize(trustManager.getConfigManager().getMessage("visitor-menu-title"));
        if (title.equals(visitorMenuTitle)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR)
                return;

            if (item.getType() == Material.GRAY_STAINED_GLASS_PANE)
                return;

            if (event.getSlot() == 22) {
                player.closeInventory();
                return;
            }

            int clickedSlot = event.getSlot();
            int permissionIndex = -1;
            for (int i = 0; i < VisitorMenuGUI.PERMISSION_SLOTS.length; i++) {
                if (VisitorMenuGUI.PERMISSION_SLOTS[i] == clickedSlot) {
                    permissionIndex = i;
                    break;
                }
            }

            if (permissionIndex >= 0 && permissionIndex < VisitorMenuGUI.PERMISSIONS.length) {
                String permission = VisitorMenuGUI.PERMISSIONS[permissionIndex];
                boolean current = trustManager.hasVisitorPermission(
                        player.getUniqueId(),
                        permission);

                trustManager.setVisitorPermission(
                        player.getUniqueId(),
                        permission,
                        !current);

                trustManager.savePermissionsAndMembers();

                VisitorMenuGUI.open(player, trustManager);
            }
            return;
        }

        String trustMenuTitleTemplate = trustManager.getConfigManager().getMessage("trust-menu-title", "{player}", "");
        String trustPrefix = ChatUtils.colorize(trustMenuTitleTemplate);
        if (trustPrefix != null && !trustPrefix.isEmpty() && title.startsWith(trustPrefix)) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR)
                return;

            if (item.getType() == Material.GRAY_STAINED_GLASS_PANE)
                return;

            String trustedName;
            try {
                trustedName = ChatColor.stripColor(title.substring(trustPrefix.length()).trim());
            } catch (Exception e) {
                return;
            }
            if (trustedName == null || trustedName.isEmpty())
                return;

            OfflinePlayer trustedPlayer = findOfflinePlayer(trustedName);
            if (trustedPlayer == null)
                return;

            if (event.getSlot() == 22) {
                TrustListGUI.open(player, trustManager);
                return;
            }

            int clickedSlot = event.getSlot();
            int permissionIndex = -1;
            for (int i = 0; i < TrustMenuGUI.PERMISSION_SLOTS.length; i++) {
                if (TrustMenuGUI.PERMISSION_SLOTS[i] == clickedSlot) {
                    permissionIndex = i;
                    break;
                }
            }

            if (permissionIndex >= 0 && permissionIndex < TrustMenuGUI.PERMISSIONS.length) {
                String permission = TrustMenuGUI.PERMISSIONS[permissionIndex];
                boolean current = trustManager.hasTrustPermission(
                        player.getUniqueId(),
                        trustedPlayer.getUniqueId(),
                        permission);

                trustManager.setTrustPermission(
                        player.getUniqueId(),
                        trustedPlayer.getUniqueId(),
                        permission,
                        !current);

                trustManager.savePermissionsAndMembers();

                TrustMenuGUI.open(player, trustedPlayer, trustManager);
            }
        }
    }

    private OfflinePlayer findOfflinePlayer(String name) {
        Player onlinePlayer = plugin.getServer().getPlayerExact(name);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(name);

        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer;
        }

        return null;
    }
}