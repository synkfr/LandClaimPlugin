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

import java.util.Set;
import java.util.UUID;

public class TrustListGUI {
    public static void open(Player owner, TrustManager trustManager) {
        UUID ownerId = owner.getUniqueId();
        Set<UUID> trusted = trustManager.getTrustedPlayers(ownerId);

        int size = 9 * ((trusted.size() + 8) / 9); // Round up to multiple of 9
        if (size < 9) size = 9;
        if (size > 54) size = 54;

        String title = ChatUtils.colorize(trustManager.getConfigManager().getMessage("trust-list-title"));
        Inventory gui = Bukkit.createInventory(null, size, title);

        int slot = 0;
        for (UUID id : trusted) {
            OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(id);
            if (trustedPlayer.getName() == null) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName(ChatUtils.colorize("&e" + trustedPlayer.getName()));
            meta.setOwningPlayer(trustedPlayer);
            head.setItemMeta(meta);

            gui.setItem(slot++, head);
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatUtils.colorize("&cBack"));
        backButton.setItemMeta(backMeta);
        gui.setItem(gui.getSize() - 1, backButton);

        owner.openInventory(gui);
    }
}