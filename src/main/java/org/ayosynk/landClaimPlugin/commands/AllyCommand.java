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

import java.util.UUID;

public class AllyCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public AllyCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> commandManager, Command.Builder<PlayerSource> base) {
        commandManager.command(base.literal("ally").literal("invite")
                .required("name", StringParser.greedyStringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("name");

                    ClaimProfile senderProfile = claimManager.getProfile(player.getUniqueId());
                    if (senderProfile == null) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    ClaimProfile targetProfile = claimManager.getProfileByName(targetName);
                    if (targetProfile == null) {
                        player.sendMessage(configManager.getMessage("claim-not-found"));
                        return;
                    }

                    if (senderProfile.getOwnerId().equals(targetProfile.getOwnerId())) {
                        player.sendMessage(configManager.getMessage("cannot-ally-self"));
                        return;
                    }

                    if (senderProfile.hasAlly(targetProfile.getOwnerId())) {
                        player.sendMessage(configManager.getMessage("already-allied"));
                        return;
                    }

                    claimManager.addAllyInvite(targetProfile.getOwnerId(), senderProfile.getOwnerId());
                    player.sendMessage(configManager.getMessage("ally-invite-sent", "<name>", targetProfile.getName()));

                    Player targetOwner = Bukkit.getPlayer(targetProfile.getOwnerId());
                    if (targetOwner != null && targetOwner.isOnline()) {
                        targetOwner.sendMessage(
                                configManager.getMessage("ally-invite-received", "<name>", senderProfile.getName()));
                    }
                }));

        commandManager.command(base.literal("ally").literal("accept")
                .optional("name", StringParser.greedyStringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    String targetName = context.getOrDefault("name", null);
                    ClaimProfile senderProfile = null;

                    if (targetName != null) {
                        senderProfile = claimManager.getProfileByName(targetName);
                        if (senderProfile == null
                                || !claimManager.hasAllyInvite(profile.getOwnerId(), senderProfile.getOwnerId())) {
                            player.sendMessage(configManager.getMessage("no-ally-invite-from", "<name>", targetName));
                            return;
                        }
                    } else {
                        // Find any pending invite if name not specified. Note: no easy way to get just
                        // one from Map without iterating.
                        // For simplicity, we can require name if multiple, or just take the first.
                        // PendingAllyInvites in ClaimManager isn't easily accessible for a "get first",
                        // so require name if they have multiple.
                        player.sendMessage(configManager.getMessage("specify-ally-name"));
                        return;
                    }

                    // Mutual ally additions
                    profile.addAlly(senderProfile.getOwnerId());
                    senderProfile.addAlly(profile.getOwnerId());

                    claimManager.removeAllyInvite(profile.getOwnerId(), senderProfile.getOwnerId());

                    claimManager.saveAndSync(profile);
                    claimManager.saveAndSync(senderProfile);

                    player.sendMessage(configManager.getMessage("ally-accepted", "<name>", senderProfile.getName()));
                    Player senderOwner = Bukkit.getPlayer(senderProfile.getOwnerId());
                    if (senderOwner != null && senderOwner.isOnline()) {
                        senderOwner.sendMessage(
                                configManager.getMessage("ally-accepted-target", "<name>", profile.getName()));
                    }
                }));

        commandManager.command(base.literal("ally").literal("deny")
                .required("name", StringParser.greedyStringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    String targetName = context.get("name");
                    ClaimProfile senderProfile = claimManager.getProfileByName(targetName);

                    if (senderProfile == null
                            || !claimManager.hasAllyInvite(profile.getOwnerId(), senderProfile.getOwnerId())) {
                        player.sendMessage(configManager.getMessage("no-ally-invite-from", "<name>", targetName));
                        return;
                    }

                    claimManager.removeAllyInvite(profile.getOwnerId(), senderProfile.getOwnerId());
                    player.sendMessage(configManager.getMessage("ally-denied", "<name>", senderProfile.getName()));
                }));

        commandManager.command(base.literal("ally").literal("remove")
                .required("name", StringParser.greedyStringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    String targetName = context.get("name");
                    ClaimProfile targetProfile = claimManager.getProfileByName(targetName);

                    if (targetProfile == null || !profile.hasAlly(targetProfile.getOwnerId())) {
                        player.sendMessage(configManager.getMessage("not-allied"));
                        return;
                    }

                    profile.removeAlly(targetProfile.getOwnerId());
                    targetProfile.removeAlly(profile.getOwnerId());

                    claimManager.saveAndSync(profile);
                    claimManager.saveAndSync(targetProfile);

                    player.sendMessage(configManager.getMessage("ally-removed", "<name>", targetProfile.getName()));
                }));
    }
}
