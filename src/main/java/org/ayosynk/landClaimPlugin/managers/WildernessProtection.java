package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Central gate for "wilderness protection" — when enabled, all block/entity
 * interactions in unclaimed chunks are denied. Used by the protection
 * listeners to short-circuit before reaching {@code PermissionResolver}.
 *
 * <p>Decisions are made in this order, first match wins:</p>
 * <ol>
 *   <li>Plugin / config not loaded yet — allow (fail-open during startup).</li>
 *   <li>{@code wildernessProtection.enabled} is {@code false} — allow.</li>
 *   <li>Player has {@code landclaim.admin} — allow (admin bypass).</li>
 *   <li>World is in {@code exceptionWorlds} — allow.</li>
 *   <li>Otherwise — deny (the caller should cancel the event and notify).</li>
 * </ol>
 *
 * <p>The flag parameter is currently ignored because the simple config
 * blocks all interactions uniformly. It's part of the API so future
 * per-flag configuration (e.g. "allow USE_DOORS in wilderness") can be
 * added without changing call sites.</p>
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
        return false;
    }

    /**
     * Convenience inverse of {@link #isAllowed(World, Player, String)}.
     */
    public static boolean isDenied(World world, Player player, String flag) {
        return !isAllowed(world, player, flag);
    }
}
