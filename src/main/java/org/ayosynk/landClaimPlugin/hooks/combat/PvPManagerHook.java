package org.ayosynk.landClaimPlugin.hooks.combat;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;

public class PvPManagerHook implements CombatHook {

    public PvPManagerHook() {
    }

    @Override
    public boolean isInCombat(Player player) {
        try {
            CombatPlayer combatPlayer = CombatPlayer.get(player);
            return combatPlayer != null && combatPlayer.isInCombat();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
