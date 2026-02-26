package org.ayosynk.landClaimPlugin.hooks.combat;

import com.eternalcode.combat.EternalCombatApi;
import com.eternalcode.combat.EternalCombatProvider;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EternalCombatHook implements CombatHook {
    private final EternalCombatApi combatApi;

    public EternalCombatHook() {
        this.combatApi = EternalCombatProvider.provide();
    }

    @Override
    public boolean isInCombat(Player player) {
        if (combatApi == null)
            return false;

        UUID playerId = player.getUniqueId();
        return combatApi.getFightManager().isInCombat(playerId);
    }
}
