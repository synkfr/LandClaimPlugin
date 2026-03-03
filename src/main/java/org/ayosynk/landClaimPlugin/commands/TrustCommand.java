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

import java.util.Set;
import java.util.UUID;

/**
 * Handles: /claim trust list/add/remove
 * Manages trusted players on the owner's ClaimProfile.
 */
public class TrustCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public TrustCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> trustBuilder = claimBuilder.literal("trust");

        // /claim trust list
        manager.command(trustBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }

                    var trusted = profile.getTrustedPlayerFlags();
                    if (trusted.isEmpty()) {
                        player.sendMessage(configManager.getMessage("trust-list-empty"));
                        return;
                    }

                    player.sendMessage(configManager.getMessage("trust-list-header"));
                    for (UUID trustedId : trusted.keySet()) {
                        String name = Bukkit.getOfflinePlayer(trustedId).getName();
                        if (name == null)
                            name = trustedId.toString();
                        Set<String> flags = trusted.get(trustedId);
                        player.sendMessage(configManager.getMessage("trust-list-entry",
                                "<player>", name, "<flags>", String.join(", ", flags)));
                    }
                }));

        // /claim trust add <player>
        manager.command(trustBuilder.literal("add")
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

                    Player target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        player.sendMessage(configManager.getMessage("player-not-online"));
                        return;
                    }
                    UUID targetId = target.getUniqueId();

                    if (profile.isOwner(targetId)) {
                        player.sendMessage(configManager.getMessage("cannot-trust-self"));
                        return;
                    }
                    if (profile.isTrusted(targetId)) {
                        player.sendMessage(configManager.getMessage("already-trusted"));
                        return;
                    }

                    // Add with empty flags — permissions managed via PlayerTrustPermissionGUI
                    profile.addTrustedPlayer(targetId);

                    plugin.getCacheManager().getProfileCache().put(player.getUniqueId(), profile);
                    claimManager.saveAndSync(profile);

                    player.sendMessage(configManager.getMessage("trust-added", "<player>", target.getName()));
                }));

        // /claim trust remove <player>
        manager.command(trustBuilder.literal("remove")
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

                    Player target = Bukkit.getPlayer(targetName);
                    UUID targetId;
                    if (target != null) {
                        targetId = target.getUniqueId();
                    } else {
                        // Try offline player by name
                        @SuppressWarnings("deprecation")
                        var offline = Bukkit.getOfflinePlayer(targetName);
                        targetId = offline.getUniqueId();
                    }

                    if (!profile.isTrusted(targetId)) {
                        player.sendMessage(configManager.getMessage("not-trusted"));
                        return;
                    }

                    profile.removeTrustedPlayer(targetId);

                    plugin.getCacheManager().getProfileCache().put(player.getUniqueId(), profile);
                    claimManager.saveAndSync(profile);

                    player.sendMessage(configManager.getMessage("trust-removed", "<player>", targetName));
                }));
    }
}
