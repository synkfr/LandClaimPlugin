package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.hooks.map.BlueMapHook;
import org.ayosynk.landClaimPlugin.hooks.map.DynmapHook;
import org.ayosynk.landClaimPlugin.hooks.map.Pl3xMapHook;
import org.ayosynk.landClaimPlugin.hooks.map.SquaremapHook;
import org.bukkit.Bukkit;

/**
 * Manages integrations with third-party plugins (Map plugins, WorldGuard,
 * etc.).
 */
public class HookManager {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    private boolean worldGuardEnabled = false;

    private BlueMapHook blueMapHook;
    private DynmapHook dynmapHook;
    private SquaremapHook squaremapHook;
    private Pl3xMapHook pl3xmapHook;

    public HookManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    public void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardEnabled = true;
            plugin.getLogger().info("WorldGuard detected. Enabling region gap protection.");
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (configManager.getPluginConfig().bluemap.enabled
                    && Bukkit.getPluginManager().getPlugin("BlueMap") != null) {
                blueMapHook = new BlueMapHook(plugin, claimManager);
                plugin.getLogger().info("BlueMap detected. Enabling map integration.");
            }
            if (configManager.getPluginConfig().dynmap.enabled
                    && Bukkit.getPluginManager().getPlugin("dynmap") != null) {
                dynmapHook = new DynmapHook(plugin, claimManager);
                plugin.getLogger().info("Dynmap detected. Enabling map integration.");
            }
            if (configManager.getPluginConfig().squaremap.enabled
                    && Bukkit.getPluginManager().getPlugin("squaremap") != null) {
                squaremapHook = new SquaremapHook(plugin, claimManager);
                plugin.getLogger().info("Squaremap detected. Enabling map integration.");
            }
            if (configManager.getPluginConfig().pl3xmap.enabled
                    && Bukkit.getPluginManager().getPlugin("Pl3xMap") != null) {
                pl3xmapHook = new Pl3xMapHook(plugin, claimManager);
                plugin.getLogger().info("Pl3xMap detected. Enabling map integration.");
            }
        });
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }

    public void refreshMapHooks() {
        if (blueMapHook != null && blueMapHook.isActive()) {
            blueMapHook.update();
        }
        if (dynmapHook != null && dynmapHook.isActive()) {
            dynmapHook.update();
        }
        if (squaremapHook != null && squaremapHook.isActive()) {
            squaremapHook.update();
        }
        if (pl3xmapHook != null && pl3xmapHook.isActive()) {
            pl3xmapHook.update();
        }
    }

    public BlueMapHook getBlueMapHook() {
        return blueMapHook;
    }

    public DynmapHook getDynmapHook() {
        return dynmapHook;
    }
}
