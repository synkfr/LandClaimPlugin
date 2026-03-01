package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * Handles: /unclaim, /unclaim all
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
                    unclaimCurrentChunk(player);
                }));

        // /unclaim all
        manager.command(unclaimBuilder.literal("all")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage(configManager.getMessage("unclaim-all-confirm"));
                }));
    }

    private void unclaimCurrentChunk(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Chunk chunk = player.getLocation().getChunk();
            ChunkPosition pos = new ChunkPosition(chunk);

            Claim claim = claimManager.getSubClaimAt(pos);
            if (claim == null) {
                claim = claimManager.getClaimAt(pos);
            }

            if (claim == null) {
                player.sendMessage(configManager.getMessage("not-in-claim"));
                return;
            }

            if (!claim.getOwnerId().equals(player.getUniqueId())) {
                player.sendMessage(configManager.getMessage("not-owner"));
                return;
            }

            if (claimManager.unclaimChunk(chunk)) {
                player.sendMessage(configManager.getMessage("chunk-unclaimed"));
            }
        });
    }
}
