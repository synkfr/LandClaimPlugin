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

    public static boolean isTooCloseToWorldGuardRegion(org.ayosynk.landClaimPlugin.models.ChunkPosition pos, int gap) {
        if (!registered) return false;
        
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(pos.world());
        if (world == null) return false;

        try {
            com.sk89q.worldguard.protection.regions.RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.managers.RegionManager regionManager = container.get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
            if (regionManager == null) return false;

            int chunkX = pos.x();
            int chunkZ = pos.z();

            for (int dx = -gap; dx <= gap; dx++) {
                for (int dz = -gap; dz <= gap; dz++) {
                    int checkX = (chunkX + dx) * 16;
                    int checkZ = (chunkZ + dz) * 16;

                    com.sk89q.worldedit.math.BlockVector3 min = com.sk89q.worldedit.math.BlockVector3.at(checkX, world.getMinHeight(), checkZ);
                    com.sk89q.worldedit.math.BlockVector3 max = com.sk89q.worldedit.math.BlockVector3.at(checkX + 15, world.getMaxHeight(), checkZ + 15);
                    com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion checkRegion = 
                            new com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion("temp-claim-check", min, max);

                    com.sk89q.worldguard.protection.ApplicableRegionSet regions = regionManager.getApplicableRegions(checkRegion);
                    for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : regions) {
                        if (region.getId().equals("__global__")) {
                            continue;
                        }
                        
                        boolean allowed = false;
                        if (ALLOW_LAND_CLAIMS_FLAG != null) {
                            com.sk89q.worldguard.protection.flags.StateFlag.State flagState = region.getFlag(ALLOW_LAND_CLAIMS_FLAG);
                            if (flagState == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW) {
                                allowed = true;
                            }
                        }
                        
                        if (!allowed) {
                            return true; // Overlaps a region that doesn't explicitly allow land claims
                        }
                    }
                }
            }
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().warning("Error checking WorldGuard regions: " + e.getMessage());
        }
        return false;
    }
}
