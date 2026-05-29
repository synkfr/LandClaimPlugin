package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A Cloud SuggestionProvider that suggests claim names that the player
 * is currently a member or trusted player of, allowing quick tab-completion
 * for the "/claim leave" command.
 */
public final class ClaimLeaveSuggestions {

    private ClaimLeaveSuggestions() {}

    /**
     * Returns a SuggestionProvider suggesting all claims the command sender
     * can leave (claims they are trusted or members of).
     */
    public static SuggestionProvider<Source> get(LandClaimPlugin plugin) {
        return SuggestionProvider.blocking((ctx, input) -> {
            if (!(ctx.sender().source() instanceof Player player)) {
                return List.of();
            }
            UUID playerId = player.getUniqueId();
            List<Suggestion> suggestions = new ArrayList<>();
            for (ClaimProfile profile : plugin.getClaimManager().getAllProfiles()) {
                if (profile.isMember(playerId) || profile.isTrusted(playerId)) {
                    String name = profile.getName();
                    if (name != null && !name.isEmpty()) {
                        suggestions.add(Suggestion.suggestion(name));
                    }
                }
            }
            return suggestions;
        });
    }
}
