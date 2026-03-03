package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared GUI utility class — eliminates duplicated item-building boilerplate
 * across all GUI files. Returns plain Bukkit {@link ItemStack} instances.
 */
public final class GuiHelper {

    /** Cached MiniMessage singleton — avoids re-creation per call. */
    public static final MiniMessage MM = MiniMessage.miniMessage();

    /** Pre-allocated ItemFlag array — avoids ItemFlag.values() clone per item. */
    private static final ItemFlag[] ALL_FLAGS = ItemFlag.values();

    private GuiHelper() {
    }

    // --- Non-placeholder variants ---

    public static ItemStack buildItemStack(String material, String name, List<String> lore) {
        return buildItemStackInternal(material, name, lore);
    }

    // --- Placeholder variants (for GUIs that inject claim/player data) ---

    public static ItemStack buildItemStack(String material, String name, List<String> lore,
            ClaimProfile profile, Player player, String ownerName, String claimName) {
        String resolvedName = name != null ? replacePlaceholders(name, profile, player, ownerName, claimName) : null;
        List<String> resolvedLore = null;
        if (lore != null && !lore.isEmpty()) {
            resolvedLore = new ArrayList<>(lore.size());
            for (String line : lore) {
                resolvedLore.add(replacePlaceholders(line, profile, player, ownerName, claimName));
            }
        }
        return buildItemStackInternal(material, resolvedName, resolvedLore);
    }

    // --- SlotDefinition convenience builders ---

    public static SlotDefinition buildSlot(String material, String name, List<String> lore) {
        return new SlotDefinition(buildItemStack(material, name, lore));
    }

    public static SlotDefinition buildSlot(String material, String name, List<String> lore, ClickAction action) {
        return new SlotDefinition(buildItemStack(material, name, lore), action);
    }

    public static SlotDefinition buildSlot(String material, String name, List<String> lore,
            ClaimProfile profile, Player player, String ownerName, String claimName) {
        return new SlotDefinition(buildItemStack(material, name, lore, profile, player, ownerName, claimName));
    }

    public static SlotDefinition buildSlot(String material, String name, List<String> lore,
            ClaimProfile profile, Player player, String ownerName, String claimName, ClickAction action) {
        return new SlotDefinition(buildItemStack(material, name, lore, profile, player, ownerName, claimName), action);
    }

    // --- Placeholder resolution ---

    public static String replacePlaceholders(String text, ClaimProfile profile, Player player,
            String ownerName, String claimName) {
        return text.replace("{claim_name}", claimName)
                .replace("{owner}", ownerName)
                .replace("{size}", String.valueOf(profile.getOwnedChunks().size()))
                .replace("{power}", "0")
                .replace("{members}", String.valueOf(profile.getMemberRoles().size()))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
    }

    // --- Core builder logic ---

    private static ItemStack buildItemStackInternal(String materialName, String name, List<String> lore) {
        Material mat = Material.matchMaterial(materialName.toUpperCase());
        if (mat == null)
            mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        // Hide all default tooltip attributes
        meta.addItemFlags(ALL_FLAGS);

        // Strip default attributes to shrink the NMS component map
        try {
            meta.setAttributeModifiers(
                    com.google.common.collect.LinkedListMultimap.create());
        } catch (Exception ignored) {
        }

        if (name != null && !name.isEmpty()) {
            Component comp = MM.deserialize(name);
            meta.displayName(comp);
        }

        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>(lore.size());
            for (String line : lore) {
                loreComponents.add(MM.deserialize(line));
            }
            meta.lore(loreComponents);
        }

        item.setItemMeta(meta);
        return item;
    }
}
