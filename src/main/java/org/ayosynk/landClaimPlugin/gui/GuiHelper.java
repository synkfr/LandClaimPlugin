package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared GUI utility class with material and prototype item caching.
 */
public final class GuiHelper {

    /** Cached MiniMessage singleton. */
    public static final MiniMessage MM = MiniMessage.miniMessage();

    /** Pre-allocated ItemFlag array to avoid per-item enum clones. */
    private static final ItemFlag[] ALL_FLAGS = ItemFlag.values();

    private static final int MAX_ITEM_CACHE = 512;
    private static final Map<String, Material> MATERIAL_CACHE = new ConcurrentHashMap<>();
    private static final Map<ItemCacheKey, ItemStack> ITEM_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ItemCacheKey, ItemStack> eldest) {
                    return size() > MAX_ITEM_CACHE;
                }
            });

    private record ItemCacheKey(String material, String name, List<String> lore, String statusLine, boolean glow) {
    }

    private GuiHelper() {
    }

    /** Clears cached materials and built ItemStack prototypes on config reload. */
    public static void clearCache() {
        MATERIAL_CACHE.clear();
        ITEM_CACHE.clear();
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
                .replace("<claim_name>", claimName)
                .replace("{owner}", ownerName)
                .replace("<owner>", ownerName)
                .replace("{size}", String.valueOf(profile.getOwnedChunks().size()))
                .replace("<size>", String.valueOf(profile.getOwnedChunks().size()))
                .replace("{members}", String.valueOf(profile.getMemberRoles().size()))
                .replace("<members>", String.valueOf(profile.getMemberRoles().size()))
                .replace("{world}", player.getWorld().getName())
                .replace("<world>", player.getWorld().getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("<x>", String.valueOf(player.getLocation().getBlockX()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                .replace("<z>", String.valueOf(player.getLocation().getBlockZ()));
    }

    // --- Flag toggle & dynamic builders ---

    /** Builds and caches toggleable permission-flag items for Visitor, Trust, Ally, and Role editors. */
    public static ItemStack buildToggleFlagItem(String materialName, String name, List<String> lore,
            String statusText, boolean active) {
        return getOrBuildCached(materialName, name, lore, statusText != null ? statusText : "", active);
    }

    /** Builds a clean non-italic dynamic item without hitting the static template cache. */
    public static ItemStack buildItem(Material material, Component displayName, List<Component> lore) {
        ItemStack item = new ItemStack(material != null ? material : Material.STONE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        applyCleanMeta(meta, displayName, lore, false);
        item.setItemMeta(meta);
        return item;
    }

    /** Builds a player head using Paper PlayerProfile to avoid synchronous offline lookups. */
    public static ItemStack buildPlayerHead(UUID uuid, String playerName, Component displayName, List<Component> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        if (uuid != null) {
            if (playerName != null && !playerName.isBlank()) {
                meta.setPlayerProfile(Bukkit.createProfile(uuid, playerName));
            } else {
                meta.setPlayerProfile(Bukkit.createProfile(uuid));
            }
        }
        applyCleanMeta(meta, displayName, lore, false);
        skull.setItemMeta(meta);
        return skull;
    }

    /** Builds a player head directly from an online player's already-loaded skin profile. */
    public static ItemStack buildPlayerHead(Player player, Component displayName, List<Component> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        if (player != null) {
            meta.setPlayerProfile(player.getPlayerProfile());
        }
        applyCleanMeta(meta, displayName, lore, false);
        skull.setItemMeta(meta);
        return skull;
    }

    // --- Core builder & cache logic ---

    private static ItemStack buildItemStackInternal(String materialName, String name, List<String> lore) {
        return getOrBuildCached(materialName, name, lore, null, false);
    }

    private static ItemStack getOrBuildCached(String materialName, String name, List<String> lore,
            String statusLine, boolean glow) {
        String normMat = materialName != null ? materialName.toUpperCase(Locale.ROOT) : "STONE";
        List<String> frozenLore = lore != null && !lore.isEmpty() ? List.copyOf(lore) : List.of();
        ItemCacheKey key = new ItemCacheKey(normMat, name != null ? name : "", frozenLore, statusLine, glow);

        ItemStack cached = ITEM_CACHE.get(key);
        if (cached != null) {
            return cached.clone();
        }

        Material mat = resolveMaterial(normMat);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component displayName = (name != null && !name.isEmpty()) ? MM.deserialize("<i:false>" + name) : null;
        List<Component> loreComponents = null;
        if (!frozenLore.isEmpty() || statusLine != null) {
            loreComponents = new ArrayList<>(frozenLore.size() + (statusLine != null ? 2 : 0));
            for (String line : frozenLore) {
                loreComponents.add(MM.deserialize("<i:false>" + line));
            }
            if (statusLine != null && !statusLine.isEmpty()) {
                if (!frozenLore.isEmpty()) {
                    loreComponents.add(Component.empty());
                }
                loreComponents.add(MM.deserialize("<i:false>" + statusLine));
            }
        }

        applyCleanMeta(meta, displayName, loreComponents, glow);
        item.setItemMeta(meta);
        ITEM_CACHE.put(key, item);
        return item.clone();
    }

    private static void applyCleanMeta(ItemMeta meta, Component displayName, List<Component> lore, boolean glow) {
        meta.addItemFlags(ALL_FLAGS);
        try {
            meta.setAttributeModifiers(com.google.common.collect.LinkedListMultimap.create());
        } catch (Exception ignored) {
        }
        if (displayName != null) {
            meta.displayName(displayName);
        }
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    private static Material resolveMaterial(String upperName) {
        return MATERIAL_CACHE.computeIfAbsent(upperName, k -> {
            Material direct = Material.getMaterial(k);
            if (direct != null) return direct;
            Material matched = Material.matchMaterial(k);
            return matched != null ? matched : Material.STONE;
        });
    }

    public static boolean checkMenuPermission(Player player, String menuName, LandClaimPlugin plugin) {
        if (!plugin.getConfigManager().useSeparatePremission()) {
            return true;
        }
        if (player.hasPermission("landclaim.admin") || player.hasPermission("landclaim.menu.*") || player.hasPermission("landclaim.menu." + menuName.toLowerCase())) {
            return true;
        }
        player.sendMessage(plugin.getConfigManager().getMessage("access-denied"));
        return false;
    }

    public static boolean checkPermission(Player player, String permission, LandClaimPlugin plugin) {
        if (!plugin.getConfigManager().useSeparatePremission()) {
            return true;
        }
        if (player.hasPermission("landclaim.admin") || player.hasPermission(permission)) {
            return true;
        }
        player.sendMessage(plugin.getConfigManager().getMessage("access-denied"));
        return false;
    }
}
