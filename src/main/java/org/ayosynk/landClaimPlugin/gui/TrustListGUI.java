package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrustListGUI {
    
    private static final ItemStack BORDER_PANE;
    
    static {
        BORDER_PANE = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = BORDER_PANE.getItemMeta();
        meta.setDisplayName(" ");
        BORDER_PANE.setItemMeta(meta);
    }
    
    public static void open(Player owner, TrustManager trustManager) {
        UUID ownerId = owner.getUniqueId();
        Set<UUID> trusted = trustManager.getTrustedPlayers(ownerId);

        // Use 54-slot inventory with borders
        int size = 54;
        String title = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-list-title"));
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Fill borders
        fillBorders(gui);

        // Add info item in top center
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatUtils.colorize("&6&lTrusted Players"));
        infoMeta.setLore(Arrays.asList(
                ChatUtils.colorize("&7Total trusted: &e" + trusted.size()),
                ChatUtils.colorize("&7Click a player to manage permissions")
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Add player heads in the middle area (slots 10-43, excluding borders)
        int[] playerSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        int slotIndex = 0;
        
        for (UUID id : trusted) {
            if (slotIndex >= playerSlots.length) break;
            
            OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(id);
            if (trustedPlayer.getName() == null) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            
            boolean isOnline = trustedPlayer.isOnline();
            String statusColor = isOnline ? "&a" : "&7";
            String statusText = isOnline ? "&a● Online" : "&c● Offline";
            
            meta.setDisplayName(ChatUtils.colorize(statusColor + trustedPlayer.getName()));
            meta.setOwningPlayer(trustedPlayer);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatUtils.colorize(statusText));
            lore.add(ChatUtils.colorize("&eClick to manage permissions"));
            meta.setLore(lore);
            
            head.setItemMeta(meta);
            gui.setItem(playerSlots[slotIndex++], head);
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatUtils.colorize("&c« Close"));
        backButton.setItemMeta(backMeta);
        gui.setItem(49, backButton);

        owner.openInventory(gui);
    }
    
    private static void fillBorders(Inventory gui) {
        // Top row
        for (int i = 0; i < 9; i++) {
            if (i != 4) gui.setItem(i, BORDER_PANE.clone());
        }
        // Bottom row
        for (int i = 45; i < 54; i++) {
            if (i != 49) gui.setItem(i, BORDER_PANE.clone());
        }
        // Left and right columns
        for (int i = 9; i < 45; i += 9) {
            gui.setItem(i, BORDER_PANE.clone());
            gui.setItem(i + 8, BORDER_PANE.clone());
        }
    }
}