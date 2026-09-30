package org.ayosynk.landClaimPlugin.hooks.combat;

import com.eternalcode.combat.EternalCombatApi;
import com.eternalcode.combat.EternalCombatProvider;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EternalCombatHook implements CombatHook {
    private EternalCombatApi combatApi;

    public EternalCombatHook() {
        try {
            this.combatApi = EternalCombatProvider.provide();
        } catch (Throwable ignored) {
            this.combatApi = null;
        }
    }

    private EternalCombatApi getApi() {
        if (combatApi != null) {
            return combatApi;
        }
        try {
            combatApi = EternalCombatProvider.provide();
        } catch (Throwable ignored) {
        }
        return combatApi;
    }

    @Override
    public boolean isInCombat(Player player) {
        EternalCombatApi api = getApi();
        if (api == null) {
            return false;
        }

        try {
            UUID playerId = player.getUniqueId();
            return api.getFightManager().isInCombat(playerId);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
