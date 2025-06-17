package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class VisitorMenuGUI {
    public static final String[] PERMISSIONS = {"BUILD", "INTERACT", "CONTAINER", "TELEPORT"};

    public static void open(Player owner, TrustManager trustManager) {
        UUID ownerId = owner.getUniqueId();

        String title = trustManager.getConfigManager().getMessage("visitor-menu-title");
        title = ChatUtils.colorize(title);

        Inventory gui = Bukkit.createInventory(null, 9, title);

        for (int i = 0; i < PERMISSIONS.length; i++) {
            String permission = PERMISSIONS[i];
            boolean hasPermission = trustManager.hasVisitorPermission(ownerId, permission);

            ItemStack item = new ItemStack(hasPermission ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();

            String status = trustManager.getConfigManager().getMessage(
                    hasPermission ? "permission-enabled" : "permission-disabled",
                    "{permission}", permission
            );

            String toggleText = trustManager.getConfigManager().getMessage(
                    "permission-toggle",
                    "{permission}", permission
            );

            meta.setDisplayName(ChatUtils.colorize(status));
            meta.setLore(Arrays.asList(ChatUtils.colorize(toggleText)));
            item.setItemMeta(meta);

            gui.setItem(i, item);
        }

        owner.openInventory(gui);
    }
}
