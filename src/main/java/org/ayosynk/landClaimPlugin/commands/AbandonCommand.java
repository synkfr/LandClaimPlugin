package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.ayosynk.landClaimPlugin.util.GeyserFormHelper;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.UUID;

/**
 * Handles: /claim abandon
 * Atomically deletes the player's entire ClaimProfile.
 *
 * <p>For Bedrock players with Geyser forms enabled, a ModalForm confirmation is shown
 * before destructive actions are taken. Java players use the existing /confirm-style
 * flow (no change in UX).</p>
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
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.abandon", plugin)) return;
                    UUID playerId = player.getUniqueId();

                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }

                    if (!profile.isOwner(playerId)) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    if (GeyserFormHelper.shouldUseForms(player)) {
                        showAbandonForm(player, profile);
                    } else {
                        performAbandon(player, profile);
                    }
                }));
    }

    private void showAbandonForm(Player player, ClaimProfile profile) {
        String title = configManager.getMessage("geyser-confirm-title");
        String content = configManager.getMessage("geyser-abandon-content");
        GeyserFormHelper.sendModalForm(player, title, content, "Confirm", "Cancel", confirmed -> {
            if (!confirmed) {
                player.sendMessage(configManager.getMessage("geyser-form-cancelled"));
                return;
            }
            FoliaScheduler.runForPlayer(plugin, player, () -> performAbandon(player, profile));
        });
    }

    private void performAbandon(Player player, ClaimProfile profile) {
        UUID playerId = player.getUniqueId();
        if (!profile.isOwner(playerId)) {
            player.sendMessage(configManager.getMessage("not-owner"));
            return;
        }

        int chunksDeleted = claimManager.abandonProfile(profile.getProfileId());
        player.sendMessage(configManager.getMessage("profile-abandoned",
                "<chunks>", String.valueOf(chunksDeleted)));

        plugin.getVisualizationManager().invalidateCache(profile.getProfileId());
        plugin.getHookManager().refreshMapHooks();
        plugin.getListenerManager().getEventListener().updatePlayerClaimCache(player);
    }
}
