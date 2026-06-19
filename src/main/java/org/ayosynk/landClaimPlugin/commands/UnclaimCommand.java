package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.ayosynk.landClaimPlugin.util.GeyserFormHelper;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * Handles: /unclaim, /unclaim all
 *
 * <p>Bedrock players with Geyser forms get a ModalForm confirmation before /unclaim all.</p>
 */
public class UnclaimCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public UnclaimCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        // /unclaim — registers its own top-level builder
        Command.Builder<PlayerSource> unclaimBuilder = manager.commandBuilder("unclaim", "uc")
                .senderType(PlayerSource.class);

        manager.command(unclaimBuilder
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.unclaim", plugin)) return;
                    unclaimCurrentChunk(player);
                }));

        // /unclaim all
        manager.command(unclaimBuilder.literal("all")
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.unclaim", plugin)) return;
                    promptUnclaimAll(player);
                }));
    }

    private void promptUnclaimAll(Player player) {
        if (GeyserFormHelper.shouldUseForms(player)) {
            String title = configManager.getMessage("geyser-confirm-title");
            String content = configManager.getMessage("geyser-unclaim-all-content");
            GeyserFormHelper.sendModalForm(player, title, content, "Confirm", "Cancel", confirmed -> {
                if (!confirmed) {
                    player.sendMessage(configManager.getMessage("geyser-form-cancelled"));
                    return;
                }
                FoliaScheduler.runForPlayer(plugin, player, () -> performUnclaimAll(player));
            });
        } else {
            player.sendMessage(configManager.getMessage("unclaim-all-confirm"));
        }
    }

    private void performUnclaimAll(Player player) {
        ClaimProfile profile = claimManager.getActiveProfile(player);
        if (profile == null) {
            player.sendMessage(configManager.getMessage("no-profile"));
            return;
        }
        if (!profile.isOwner(player.getUniqueId())) {
            player.sendMessage(configManager.getMessage("not-owner"));
            return;
        }
        int chunks = claimManager.abandonProfile(profile.getProfileId());
        player.sendMessage(configManager.getMessage("profile-abandoned",
                "<chunks>", String.valueOf(chunks)));
        plugin.getVisualizationManager().invalidateCache(profile.getProfileId());
        plugin.getHookManager().refreshMapHooks();
        plugin.getListenerManager().getEventListener().updatePlayerClaimCache(player);
    }

    private void unclaimCurrentChunk(Player player) {
        FoliaScheduler.runForPlayer(plugin, player, () -> {
            Chunk chunk = player.getLocation().getChunk();
            ChunkPosition pos = new ChunkPosition(chunk);

            ClaimProfile profile = claimManager.getProfileAt(pos);
            if (profile == null) {
                player.sendMessage(configManager.getMessage("not-in-claim"));
                return;
            }

            if (!profile.isOwner(player.getUniqueId())) {
                player.sendMessage(configManager.getMessage("not-owner"));
                return;
            }

            if (claimManager.unclaimChunk(chunk)) {
                player.sendMessage(configManager.getMessage("chunk-unclaimed"));
                // Update action bar cache so EventListener sends correct message
                plugin.getListenerManager().getEventListener().updatePlayerClaimCache(player);
            }
        });
    }
}
