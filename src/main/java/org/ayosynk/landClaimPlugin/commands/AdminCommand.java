package org.ayosynk.landClaimPlugin.commands;

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
 * Handles: /claim admin check, /claim admin unclaim
 * Permission-gated: landclaim.admin
 */
public class AdminCommand implements LandClaimCommand {

    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public AdminCommand(ClaimManager claimManager, ConfigManager configManager) {
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        // /claim admin check
        manager.command(claimBuilder.literal("admin").literal("check")
                .permission("landclaim.admin")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendAdminClaimInfo(player);
                }));

        // /claim admin unclaim
        manager.command(claimBuilder.literal("admin").literal("unclaim")
                .permission("landclaim.admin")
                .handler(context -> {
                    Player player = context.sender().source();
                    adminUnclaimCurrentChunk(player);
                }));
    }

    private void sendAdminClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }
        String ownerName = Bukkit.getOfflinePlayer(claim.getOwnerId()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        player.sendMessage(
                configManager.getMessage("admin-claim-info-owned-by", "<owner>", ownerName, "<uuid>",
                        claim.getOwnerId().toString()));
        player.sendMessage(configManager.getMessage("admin-claim-info-id", "<id>", claim.getId().toString()));
    }

    private void adminUnclaimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        ChunkPosition pos = new ChunkPosition(chunk);

        Claim claim = claimManager.getClaimAt(pos);
        if (claim == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }

        if (claimManager.unclaimChunk(chunk)) {
            player.sendMessage(configManager.getMessage("admin-bypassed-unclaim"));
        }
    }
}
