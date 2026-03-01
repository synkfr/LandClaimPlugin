package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

/**
 * Handles: /claim member list/invite/kick, /claim accept, /claim deny
 */
public class MemberCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;

    public MemberCommand(LandClaimPlugin plugin, ClaimManager claimManager,
            TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> memberBuilder = claimBuilder.literal("member");

        // /claim member list
        manager.command(memberBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage(configManager.getMessage("member-list-stub"));
                }));

        // /claim member invite <player>
        manager.command(memberBuilder.literal("invite")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");

                    ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
                    Claim claim = claimManager.getClaimAt(pos);
                    if (claim == null) {
                        player.sendMessage(configManager.getMessage("not-in-claim"));
                        return;
                    }
                    if (!trustManager.canManageTrust(claim, player)) {
                        player.sendMessage(configManager.getMessage("access-denied"));
                        return;
                    }

                    Player target = plugin.getServer().getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(configManager.getMessage("player-not-online"));
                        return;
                    }
                    if (claim.getPlayerRoles().containsKey(target.getUniqueId())
                            || claim.getOwnerId().equals(target.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("already-in-claim"));
                        return;
                    }

                    trustManager.invitePlayer(claim, target);
                    player.sendMessage(configManager.getMessage("member-invited", "<player>", target.getName()));
                }));

        // /claim member kick <player>
        manager.command(memberBuilder.literal("kick")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage(configManager.getMessage("member-kicked", "<player>", targetName));
                }));

        // /claim accept
        manager.command(claimBuilder.literal("accept")
                .handler(context -> {
                    Player player = context.sender().source();
                    trustManager.acceptInvite(player);
                }));

        // /claim deny
        manager.command(claimBuilder.literal("deny")
                .handler(context -> {
                    Player player = context.sender().source();
                    trustManager.denyInvite(player);
                }));
    }
}
