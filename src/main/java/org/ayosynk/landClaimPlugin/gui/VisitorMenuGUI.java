package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VisitorMenuGUI {
    public static final String[] PERMISSIONS = {"BUILD", "INTERACT", "CONTAINER", "TELEPORT"};
    public static final int[] PERMISSION_SLOTS = {10, 12, 14, 16};
    
    private static final ItemStack BORDER_PANE;
    
    static {
        BORDER_PANE = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = BORDER_PANE.getItemMeta();
        meta.setDisplayName(" ");
        BORDER_PANE.setItemMeta(meta);
    }

    public static void open(Player owner, TrustManager trustManager) {
        UUID ownerId = owner.getUniqueId();

        String title = trustManager.getConfigManager().getMessage("visitor-menu-title");
        title = ChatUtils.colorize(title);

        Inventory gui = Bukkit.createInventory(null, 27, title);
        
        // Fill borders
        fillBorders(gui);
        
        // Add info item at top center
        ItemStack infoItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatUtils.colorize("&6&lVisitor Permissions"));
        infoMeta.setLore(Arrays.asList(
                ChatUtils.colorize("&7Configure what non-trusted"),
                ChatUtils.colorize("&7players can do in your claims"),
                "",
                ChatUtils.colorize("&c⚠ Be careful with BUILD permission!")
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);

        // Add permission toggles
        for (int i = 0; i < PERMISSIONS.length; i++) {
            String permission = PERMISSIONS[i];
            boolean hasPermission = trustManager.hasVisitorPermission(ownerId, permission);

            Material material = getPermissionMaterial(permission, hasPermission);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            String status = trustManager.getConfigManager().getMessage(
                    hasPermission ? "permission-enabled" : "permission-disabled",
                    "{permission}", permission
            );

            List<String> lore = new ArrayList<>();
            lore.add(ChatUtils.colorize(getPermissionDescription(permission)));
            lore.add("");
            lore.add(ChatUtils.colorize(hasPermission ? "&a✔ Enabled" : "&c✖ Disabled"));
            lore.add(ChatUtils.colorize("&eClick to toggle"));
            
            // Warning for dangerous permissions
            if (permission.equals("BUILD") && hasPermission) {
                lore.add("");
                lore.add(ChatUtils.colorize("&c⚠ Warning: Anyone can build!"));
            }

            meta.setDisplayName(ChatUtils.colorize(status));
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(PERMISSION_SLOTS[i], item);
        }

        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatUtils.colorize("&c« Close"));
        backButton.setItemMeta(backMeta);
        gui.setItem(22, backButton);

        owner.openInventory(gui);
    }
    
    private static Material getPermissionMaterial(String permission, boolean enabled) {
        if (!enabled) return Material.GRAY_DYE;
        return switch (permission) {
            case "BUILD" -> Material.BRICKS;
            case "INTERACT" -> Material.LEVER;
            case "CONTAINER" -> Material.CHEST;
            case "TELEPORT" -> Material.ENDER_PEARL;
            default -> Material.LIME_DYE;
        };
    }
    
    private static String getPermissionDescription(String permission) {
        return switch (permission) {
            case "BUILD" -> "&7Place and break blocks";
            case "INTERACT" -> "&7Use doors, buttons, levers";
            case "CONTAINER" -> "&7Open chests, furnaces, etc.";
            case "TELEPORT" -> "&7Teleport to this claim";
            default -> "&7Unknown permission";
        };
    }
    
    private static void fillBorders(Inventory gui) {
        // Top row
        for (int i = 0; i < 9; i++) {
            if (i != 4) gui.setItem(i, BORDER_PANE.clone());
        }
        // Bottom row
        for (int i = 18; i < 27; i++) {
            if (i != 22) gui.setItem(i, BORDER_PANE.clone());
        }
        // Left and right columns (middle row)
        gui.setItem(9, BORDER_PANE.clone());
        gui.setItem(17, BORDER_PANE.clone());
        // Fill gaps between permission items
        gui.setItem(11, BORDER_PANE.clone());
        gui.setItem(13, BORDER_PANE.clone());
        gui.setItem(15, BORDER_PANE.clone());
    }
}
