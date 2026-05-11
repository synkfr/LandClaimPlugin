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

                    ClaimProfile targetProfile = claimManager.getProfileByNameOrOwner(targetName);
                    if (targetProfile == null) {
                        player.sendMessage(configManager.getMessage("claim-not-found"));
                        return;
                    }

                    if (senderProfile.getProfileId().equals(targetProfile.getProfileId())) {
                        player.sendMessage(configManager.getMessage("cannot-ally-self"));
                        return;
                    }

                    if (senderProfile.hasAlly(targetProfile.getProfileId())) {
                        player.sendMessage(configManager.getMessage("already-allied"));
                        return;
                    }

                    claimManager.sendAllyInvite(player, targetProfile);
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
                        senderProfile = claimManager.getProfileByNameOrOwner(targetName);
                        if (senderProfile == null
                                || !claimManager.hasAllyInvite(profile.getProfileId(), senderProfile.getProfileId())) {
                            player.sendMessage(configManager.getMessage("no-ally-invite-from", "<name>", targetName));
                            return;
                        }
                    } else {
                        player.sendMessage(configManager.getMessage("specify-ally-name"));
                        return;
                    }

                    // Mutual ally additions
                    profile.addAlly(senderProfile.getProfileId());
                    senderProfile.addAlly(profile.getProfileId());

                    claimManager.removeAllyInvite(profile.getProfileId(), senderProfile.getProfileId());

                    claimManager.saveAndSync(profile);
                    claimManager.saveAndSync(senderProfile);

                    player.sendMessage(configManager.getMessage("ally-accepted", "<name>", senderProfile.getName()));
                    Player senderOwner = Bukkit.getPlayer(senderProfile.getProfileId());
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
                    ClaimProfile senderProfile = claimManager.getProfileByNameOrOwner(targetName);

                    if (senderProfile == null
                            || !claimManager.hasAllyInvite(profile.getProfileId(), senderProfile.getProfileId())) {
                        player.sendMessage(configManager.getMessage("no-ally-invite-from", "<name>", targetName));
                        return;
                    }

                    claimManager.removeAllyInvite(profile.getProfileId(), senderProfile.getProfileId());
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
                    ClaimProfile targetProfile = claimManager.getProfileByNameOrOwner(targetName);

                    if (targetProfile == null || !profile.hasAlly(targetProfile.getProfileId())) {
                        player.sendMessage(configManager.getMessage("not-allied"));
                        return;
                    }

                    profile.removeAlly(targetProfile.getProfileId());
                    targetProfile.removeAlly(profile.getProfileId());

                    claimManager.saveAndSync(profile);
                    claimManager.saveAndSync(targetProfile);

                    player.sendMessage(configManager.getMessage("ally-removed", "<name>", targetProfile.getName()));
                }));
    }
}
