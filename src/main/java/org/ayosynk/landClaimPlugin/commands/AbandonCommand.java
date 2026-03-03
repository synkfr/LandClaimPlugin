package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.UUID;

/**
 * Handles: /claim abandon
 * Atomically deletes the player's entire ClaimProfile.
 */
public class AbandonCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public AbandonCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        // /claim abandon
        manager.command(claimBuilder.literal("abandon")
                .handler(context -> {
                    Player player = context.sender().source();
                    UUID playerId = player.getUniqueId();

                    ClaimProfile profile = claimManager.getProfile(playerId);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }

                    if (!profile.isOwner(playerId)) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    int chunksDeleted = claimManager.abandonProfile(playerId);
                    player.sendMessage(configManager.getMessage("profile-abandoned",
                            "<chunks>", String.valueOf(chunksDeleted)));

                    plugin.getVisualizationManager().invalidateCache(playerId);
                    plugin.refreshMapHooks();
                }));
    }
}
