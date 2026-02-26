package org.ayosynk.landClaimPlugin.hooks.combat;

import org.bukkit.entity.Player;

public interface CombatHook {
    boolean isInCombat(Player player);
}
