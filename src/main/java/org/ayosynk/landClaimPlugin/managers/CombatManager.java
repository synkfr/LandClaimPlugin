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
            activeHooks.add(new DeluxeCombatHook());
            plugin.getLogger().info("Hooked into DeluxeCombat for combat tagging!");
        }
        if (plugin.getServer().getPluginManager().getPlugin("PvPManager") != null) {
            activeHooks.add(new PvPManagerHook());
            plugin.getLogger().info("Hooked into PvPManager for combat tagging!");
        }
        if (plugin.getServer().getPluginManager().getPlugin("EternalCombat") != null) {
            activeHooks.add(new EternalCombatHook());
            plugin.getLogger().info("Hooked into EternalCombat for combat tagging!");
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
