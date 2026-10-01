package org.ayosynk.landClaimPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A shared Cloud SuggestionProvider that includes both online and offline players
 * (everyone who has ever joined the server) in tab completion.
 */
public final class OfflinePlayerSuggestions {

    private static final List<String> COMMON_AMOUNTS = List.of("1", "5", "10", "20", "50", "100");
    private static final List<String> SELECTORS = List.of("@p", "@s", "@r", "@a");
    private static final List<String> BOOLEAN_STATES = List.of("true", "false");

    private OfflinePlayerSuggestions() {}

    /**
     * Returns a SuggestionProvider for any sender type that suggests all known
     * offline/online player names.
     */
    @SuppressWarnings("unchecked")
    public static <C> SuggestionProvider<C> all() {
        return withSelectors();
    }

    public static <C> SuggestionProvider<C> playersOnly() {
        return SuggestionProvider.blocking((ctx, input) -> {
            List<Suggestion> suggestions = new ArrayList<>();
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

    public static <C> SuggestionProvider<C> withSelectors() {
        return SuggestionProvider.blocking((ctx, input) -> {
            List<Suggestion> suggestions = new ArrayList<>();
            for (String selector : SELECTORS) {
                suggestions.add(Suggestion.suggestion(selector));
            }
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

    public static <C> SuggestionProvider<C> adminChunk() {
        return SuggestionProvider.blocking((ctx, input) -> {
            String remaining = input.remainingInput();
            List<Suggestion> suggestions = new ArrayList<>();

            int lastSpace = remaining.lastIndexOf(' ');
            if (lastSpace == -1) {
                for (String selector : SELECTORS) {
                    suggestions.add(Suggestion.suggestion(selector));
                }
                for (String amount : COMMON_AMOUNTS) {
                    suggestions.add(Suggestion.suggestion(amount));
                }
                @SuppressWarnings("deprecation")
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                Arrays.stream(offlinePlayers)
                        .map(OfflinePlayer::getName)
                        .filter(name -> name != null && !name.isEmpty())
                        .map(Suggestion::suggestion)
                        .forEach(suggestions::add);
                return suggestions;
            }

            String prefix = remaining.substring(0, lastSpace + 1);
            String firstToken = remaining.substring(0, lastSpace).trim();

            if (isPositiveInt(firstToken)) {
                for (String selector : SELECTORS) {
                    suggestions.add(Suggestion.suggestion(prefix + selector));
                }
                @SuppressWarnings("deprecation")
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                Arrays.stream(offlinePlayers)
                        .map(OfflinePlayer::getName)
                        .filter(name -> name != null && !name.isEmpty())
                        .map(name -> Suggestion.suggestion(prefix + name))
                        .forEach(suggestions::add);
            } else {
                for (String amount : COMMON_AMOUNTS) {
                    suggestions.add(Suggestion.suggestion(prefix + amount));
                }
            }

            return suggestions;
        });
    }

    public static <C> SuggestionProvider<C> decayExempt() {
        return SuggestionProvider.blocking((ctx, input) -> {
            String remaining = input.remainingInput();
            List<Suggestion> suggestions = new ArrayList<>();

            int lastSpace = remaining.lastIndexOf(' ');
            if (lastSpace == -1) {
                for (String selector : SELECTORS) {
                    suggestions.add(Suggestion.suggestion(selector));
                }
                @SuppressWarnings("deprecation")
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                Arrays.stream(offlinePlayers)
                        .map(OfflinePlayer::getName)
                        .filter(name -> name != null && !name.isEmpty())
                        .map(Suggestion::suggestion)
                        .forEach(suggestions::add);
                return suggestions;
            }

            String prefix = remaining.substring(0, lastSpace + 1);
            for (String state : BOOLEAN_STATES) {
                suggestions.add(Suggestion.suggestion(prefix + state));
            }

            return suggestions;
        });
    }

    private static boolean isPositiveInt(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            return Integer.parseInt(s) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
