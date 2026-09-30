package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.hooks.combat.CombatHook;
import org.ayosynk.landClaimPlugin.hooks.combat.DeluxeCombatHook;
import org.ayosynk.landClaimPlugin.hooks.combat.EternalCombatHook;
import org.ayosynk.landClaimPlugin.hooks.combat.PvPManagerHook;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CombatManager {
    private final LandClaimPlugin plugin;
    private final List<CombatHook> activeHooks = new ArrayList<>();

    public CombatManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        initializeHooks();
    }

    private void initializeHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("DeluxeCombat") != null) {
            try {
                activeHooks.add(new DeluxeCombatHook());
                plugin.getLogger().info("Hooked into DeluxeCombat for combat tagging!");
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to hook into DeluxeCombat: " + t.getMessage());
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("PvPManager") != null) {
            try {
                activeHooks.add(new PvPManagerHook());
                plugin.getLogger().info("Hooked into PvPManager for combat tagging!");
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to hook into PvPManager: " + t.getMessage());
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("EternalCombat") != null) {
            try {
                activeHooks.add(new EternalCombatHook());
                plugin.getLogger().info("Hooked into EternalCombat for combat tagging!");
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to hook into EternalCombat: " + t.getMessage());
            }
        }
    }

    public boolean isInCombat(Player player) {
        for (CombatHook hook : activeHooks) {
            if (hook.isInCombat(player)) {
                return true;
            }
        }
        return false;
    }
}
