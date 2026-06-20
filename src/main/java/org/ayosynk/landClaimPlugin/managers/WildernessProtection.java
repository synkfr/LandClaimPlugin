package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Central gate for "wilderness protection" — when enabled, the flags
 * listed in {@code wildernessProtection.deniedFlags} are denied in
 * unclaimed chunks. Used by the protection listeners to short-circuit
 * before reaching {@code PermissionResolver}.
 *
 * <p>Decisions are made in this order, first match wins:</p>
 * <ol>
 *   <li>Plugin / config not loaded yet — allow (fail-open during startup).</li>
 *   <li>{@code wildernessProtection.enabled} is {@code false} — allow.</li>
 *   <li>Player has {@code landclaim.admin} — allow (admin bypass).</li>
 *   <li>World is in {@code exceptionWorlds} — allow.</li>
 *   <li>{@code flag} is in {@code deniedFlags} — deny.</li>
 *   <li>Otherwise — allow.</li>
 * </ol>
 *
 * <p>The default {@code deniedFlags} list contains build / interaction flags
 * only (BLOCK_BREAK, BLOCK_PLACE, USE_CONTAINERS, USE_DOORS, etc.). Combat
 * flags (DAMAGE_ANIMALS, DAMAGE_MONSTERS, BREED_ANIMALS, SHEAR_ENTITIES,
 * TRADE_VILLAGERS, FEED_ANIMALS, LEASH_ENTITIES) and PvP are NOT denied by
 * default, so players can still hunt mobs in the wilderness. Admins can
 * add or remove flags at runtime via the config — matching is
 * case-insensitive against the upper-cased flag name.</p>
 */
public final class WildernessProtection {

    private WildernessProtection() {}

    /**
     * @return {@code true} if the action should be allowed in the wilderness,
     *         {@code false} if it should be denied.
     */
    public static boolean isAllowed(World world, Player player, String flag) {
        LandClaimPlugin plugin = LandClaimPlugin.getInstance();
        if (plugin == null || plugin.getConfigManager() == null) {
            return true;
        }
        PluginConfig config = plugin.getConfigManager().getPluginConfig();
        if (config == null || !config.wildernessProtection.enabled) {
            return true;
        }
        if (player != null && player.hasPermission("landclaim.admin")) {
            return true;
        }
        if (world != null && config.wildernessProtection.exceptionWorlds.contains(world.getName())) {
            return true;
        }
        return !isFlagDenied(config.wildernessProtection.deniedFlags, flag);
    }

    /**
     * Convenience inverse of {@link #isAllowed(World, Player, String)}.
     */
    public static boolean isDenied(World world, Player player, String flag) {
        return !isAllowed(world, player, flag);
    }

    private static boolean isFlagDenied(List<String> deniedFlags, String flag) {
        if (deniedFlags == null || deniedFlags.isEmpty() || flag == null) {
            return false;
        }
        String upper = flag.toUpperCase();
        for (String denied : deniedFlags) {
            if (denied != null && denied.toUpperCase().equals(upper)) {
                return true;
            }
        }
        return false;
    }
}
