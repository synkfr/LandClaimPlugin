package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles: /claim member list/invite/kick, /claim accept, /claim deny
 * Manages member invitations and role assignments on the owner's ClaimProfile.
 */
public class MemberCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    /** Pending invites: invitee UUID → owner UUID */
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public MemberCommand(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> memberBuilder = claimBuilder.literal("member");

        // /claim member list
        manager.command(memberBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }

                    var members = profile.getMemberRoles();
                    if (members.isEmpty()) {
                        player.sendMessage(configManager.getMessage("member-list-empty"));
                        return;
                    }

                    player.sendMessage(configManager.getMessage("member-list-header"));
                    for (Map.Entry<UUID, String> entry : members.entrySet()) {
                        String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                        if (name == null)
                            name = entry.getKey().toString();
                        player.sendMessage(configManager.getMessage("member-list-entry",
                                "<player>", name, "<role>", entry.getValue()));
                    }
                }));

        // /claim member invite <player>
        manager.command(memberBuilder.literal("invite")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");

                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId()) && !player.hasPermission("landclaim.admin")) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(configManager.getMessage("player-not-online"));
                        return;
                    }
                    UUID targetId = target.getUniqueId();

                    if (profile.isOwner(targetId) || profile.isMember(targetId)) {
                        player.sendMessage(configManager.getMessage("already-in-claim"));
                        return;
                    }

                    // Check if target can join (not owner/member elsewhere)
                    if (!claimManager.canCreateProfile(targetId)) {
                        player.sendMessage(configManager.getMessage("target-already-in-claim",
                                "<player>", target.getName()));
                        return;
                    }

                    pendingInvites.put(targetId, player.getUniqueId());

                    player.sendMessage(configManager.getMessage("member-invited", "<player>", target.getName()));
                    target.sendMessage(configManager.getMessage("invite-received",
                            "<owner>", player.getName()));
                }));

        // /claim member kick <player>
        manager.command(memberBuilder.literal("kick")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");

                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    // Resolve target UUID
                    Player target = Bukkit.getPlayer(targetName);
                    UUID targetId;
                    if (target != null) {
                        targetId = target.getUniqueId();
                    } else {
                        @SuppressWarnings("deprecation")
                        var offline = Bukkit.getOfflinePlayer(targetName);
                        targetId = offline.getUniqueId();
                    }

                    if (!profile.isMember(targetId)) {
                        player.sendMessage(configManager.getMessage("not-a-member", "<player>", targetName));
                        return;
                    }

                    profile.removeMember(targetId);

                    plugin.getCacheManager().getProfileCache().put(player.getUniqueId(), profile);
                    claimManager.saveAndSync(profile);

                    player.sendMessage(configManager.getMessage("member-kicked", "<player>", targetName));
                    if (target != null) {
                        target.sendMessage(configManager.getMessage("you-were-kicked",
                                "<owner>", player.getName()));
                    }
                }));

        // /claim accept
        manager.command(claimBuilder.literal("accept")
                .handler(context -> {
                    Player player = context.sender().source();
                    UUID playerId = player.getUniqueId();

                    UUID ownerId = pendingInvites.remove(playerId);
                    if (ownerId == null) {
                        player.sendMessage(configManager.getMessage("no-pending-invite"));
                        return;
                    }

                    ClaimProfile profile = claimManager.getProfile(ownerId);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("invite-expired"));
                        return;
                    }

                    // Add as "Member" role by default
                    profile.setMemberRole(playerId, "Member");

                    plugin.getCacheManager().getProfileCache().put(ownerId, profile);
                    claimManager.saveAndSync(profile);

                    player.sendMessage(configManager.getMessage("invite-accepted",
                            "<owner>", Bukkit.getOfflinePlayer(ownerId).getName()));

                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null) {
                        owner.sendMessage(configManager.getMessage("member-joined",
                                "<player>", player.getName()));
                    }
                }));

        // /claim deny
        manager.command(claimBuilder.literal("deny")
                .handler(context -> {
                    Player player = context.sender().source();
                    UUID playerId = player.getUniqueId();

                    UUID ownerId = pendingInvites.remove(playerId);
                    if (ownerId == null) {
                        player.sendMessage(configManager.getMessage("no-pending-invite"));
                        return;
                    }

                    player.sendMessage(configManager.getMessage("invite-denied"));

                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner != null) {
                        owner.sendMessage(configManager.getMessage("invite-was-denied",
                                "<player>", player.getName()));
                    }
                }));
    }
}
