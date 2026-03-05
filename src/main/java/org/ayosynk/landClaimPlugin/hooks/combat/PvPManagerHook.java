package org.ayosynk.landClaimPlugin.hooks.combat;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;

public class PvPManagerHook implements CombatHook {

    public PvPManagerHook() {
        // Validation could go here if needed
    }

    @Override
    public boolean isInCombat(Player player) {
        CombatPlayer combatPlayer = CombatPlayer.get(player);
        return combatPlayer != null && combatPlayer.isInCombat();
    }
}
