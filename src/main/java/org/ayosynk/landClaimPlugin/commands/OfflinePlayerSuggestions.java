package org.ayosynk.landClaimPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A shared Cloud SuggestionProvider that includes both online and offline players
 * (everyone who has ever joined the server) in tab completion.
 */
public final class OfflinePlayerSuggestions {

    private OfflinePlayerSuggestions() {}

    /**
     * Returns a SuggestionProvider for any sender type that suggests all known
     * offline/online player names.
     */
    @SuppressWarnings("unchecked")
    public static <C> SuggestionProvider<C> all() {
        return SuggestionProvider.blocking((ctx, input) -> {
            List<Suggestion> suggestions = new java.util.ArrayList<>();
            suggestions.add(Suggestion.suggestion("@p"));
            suggestions.add(Suggestion.suggestion("@s"));
            suggestions.add(Suggestion.suggestion("@r"));
            suggestions.add(Suggestion.suggestion("@a"));
            @SuppressWarnings("deprecation")
            OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
            Arrays.stream(offlinePlayers)
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null && !name.isEmpty())
                    .map(Suggestion::suggestion)
                    .forEach(suggestions::add);
            return suggestions;
        });
    }
}
