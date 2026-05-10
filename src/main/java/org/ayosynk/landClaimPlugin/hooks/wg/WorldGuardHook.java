package org.ayosynk.landClaimPlugin.hooks.wg;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook {

    public static StateFlag ALLOW_LAND_CLAIMS_FLAG;
    private static boolean registered = false;

    /**
     * Must be called during plugin's onLoad phase!
     */
    public static void onLoad() {
        try {
            // Only try if WorldGuard is actually loaded (it might not be enabled yet, but classes exist)
            Class.forName("com.sk89q.worldguard.WorldGuard");
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            try {
                StateFlag flag = new StateFlag("allow-land-claims", false); // Deny by default if set, null if not set
                registry.register(flag);
                ALLOW_LAND_CLAIMS_FLAG = flag;
                registered = true;
            } catch (Exception e) {
                // If the flag is already registered by another plugin (very unlikely)
                // or some other error occurs.
            }
        } catch (ClassNotFoundException e) {
            // WorldGuard is not installed.
        }
    }

    public static boolean isRegistered() {
        return registered;
    }
}
