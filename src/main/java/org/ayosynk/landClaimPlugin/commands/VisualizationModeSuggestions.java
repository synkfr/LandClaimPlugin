package org.ayosynk.landClaimPlugin.commands;

import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;

/**
 * A shared Cloud SuggestionProvider for visualization modes
 */
public final class VisualizationModeSuggestions {

    private VisualizationModeSuggestions() {}

    /**
     * Returns a SuggestionProvider that suggests visualization modes:
     * display_entities, particles, off
     */
    @SuppressWarnings("unchecked")
    public static <C> SuggestionProvider<C> modes() {
        return SuggestionProvider.blocking((ctx, input) -> {
            return List.of(
                    org.incendo.cloud.suggestion.Suggestion.suggestion("display_entities"),
                    org.incendo.cloud.suggestion.Suggestion.suggestion("particles"),
                    org.incendo.cloud.suggestion.Suggestion.suggestion("off")
            );
        });
    }
}