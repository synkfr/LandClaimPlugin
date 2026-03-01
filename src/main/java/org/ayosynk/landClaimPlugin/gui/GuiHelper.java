package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared GUI utility class — eliminates duplicated item-building boilerplate
 * across all 21 GUI files. Caches MiniMessage singleton to avoid repeated
 * allocations on every item construction.
 */
public final class GuiHelper {

    /** Cached MiniMessage singleton — avoids re-creation per call. */
    public static final MiniMessage MM = MiniMessage.miniMessage();

    private GuiHelper() {
    }

    // --- Non-placeholder variants ---

    public static Item buildItem(String material, String name, List<String> lore) {
        return Item.simple(buildItemBuilder(material, name, lore));
    }

    public static ItemBuilder buildItemBuilder(String material, String name, List<String> lore) {
        return buildItemBuilderInternal(material, name, lore);
    }

    // --- Placeholder variants (for GUIs that inject claim/player data) ---

    public static Item buildItem(String material, String name, List<String> lore,
            Claim claim, Player player, String ownerName, String claimName) {
        return Item.simple(buildItemBuilder(material, name, lore, claim, player, ownerName, claimName));
    }

    public static ItemBuilder buildItemBuilder(String material, String name, List<String> lore,
            Claim claim, Player player, String ownerName, String claimName) {
        String resolvedName = name != null ? replacePlaceholders(name, claim, player, ownerName, claimName) : null;
        List<String> resolvedLore = null;
        if (lore != null && !lore.isEmpty()) {
            resolvedLore = new ArrayList<>(lore.size());
            for (String line : lore) {
                resolvedLore.add(replacePlaceholders(line, claim, player, ownerName, claimName));
            }
        }
        return buildItemBuilderInternal(material, resolvedName, resolvedLore);
    }

    public static String replacePlaceholders(String text, Claim claim, Player player,
            String ownerName, String claimName) {
        return text.replace("{claim_name}", claimName)
                .replace("{owner}", ownerName)
                .replace("{size}", String.valueOf(claim.getChunks().size()))
                .replace("{power}", "0")
                .replace("{members}", String.valueOf(claim.getPlayerRoles().size()))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
    }

    // --- Core builder logic ---

    private static ItemBuilder buildItemBuilderInternal(String materialName, String name, List<String> lore) {
        Material mat = Material.matchMaterial(materialName.toUpperCase());
        if (mat == null)
            mat = Material.STONE;

        ItemBuilder builder = new ItemBuilder(mat);
        builder.hideTooltip(true);

        if (name != null && !name.isEmpty()) {
            Component comp = MM.deserialize(name);
            builder.setCustomName(comp);
        }

        if (lore != null && !lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>(lore.size());
            for (String line : lore) {
                loreComponents.add(MM.deserialize(line));
            }
            builder.setLore(loreComponents);
        }

        return builder;
    }
}
