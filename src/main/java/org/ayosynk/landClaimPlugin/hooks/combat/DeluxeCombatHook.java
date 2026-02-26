package org.ayosynk.landClaimPlugin.hooks.combat;

import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.entity.Player;

public class DeluxeCombatHook implements CombatHook {
    private final DeluxeCombatAPI api;

    public DeluxeCombatHook() {
        this.api = new DeluxeCombatAPI();
    }

    @Override
    public boolean isInCombat(Player player) {
        return api.isInCombat(player);
    }
}
