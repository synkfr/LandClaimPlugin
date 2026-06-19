package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.ayosynk.landClaimPlugin.util.GeyserFormHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.UUID;

/**
 * Handles: /claim ban, /claim unban, /claim banlist
 *
 * <p>Banning a player adds them to the claim's ban list — they lose every flag and
 * are physically prevented from entering any chunk owned by the claim. Bans are
 * independent of role/trust: even a CoOwner can be banned. Use /claim unban to
 * restore them, and /claim banlist to view current bans.</p>
 */
public class BanCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public BanCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> banBuilder = claimBuilder.literal("ban");

        // /claim ban <player>
        manager.command(banBuilder
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.ban", plugin)) return;
                    String targetName = context.get("player");

                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    UUID targetId = resolveTargetId(targetName);
                    if (targetId == null) {
                        player.sendMessage(configManager.getMessage("player-not-found"));
                        return;
                    }

                    if (profile.isOwner(targetId)) {
                        player.sendMessage(configManager.getMessage("cannot-ban-self"));
                        return;
                    }
                    if (profile.isBanned(targetId)) {
                        player.sendMessage(configManager.getMessage("already-banned",
                                "<player>", Bukkit.getOfflinePlayer(targetId).getName()));
                        return;
                    }

                    applyBan(player, profile, targetId, targetName);
                }));

        // /claim unban <player>
        manager.command(claimBuilder.literal("unban")
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.ban", plugin)) return;
                    String targetName = context.get("player");

                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    UUID targetId = resolveTargetId(targetName);
                    if (targetId == null) {
                        player.sendMessage(configManager.getMessage("player-not-found"));
                        return;
                    }

                    if (!profile.isBanned(targetId)) {
                        player.sendMessage(configManager.getMessage("not-banned",
                                "<player>", Bukkit.getOfflinePlayer(targetId).getName()));
                        return;
                    }

                    profile.removeBannedPlayer(targetId);
                    plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
                    claimManager.saveAndSync(profile);

                    player.sendMessage(configManager.getMessage("player-unbanned",
                            "<player>", Bukkit.getOfflinePlayer(targetId).getName()));

                    Player target = Bukkit.getPlayer(targetId);
                    if (target != null) {
                        target.sendMessage(configManager.getMessage("unbanned-by-owner",
                                "<owner>", player.getName()));
                    }
                }));

        // /claim banlist
        manager.command(claimBuilder.literal("banlist")
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.ban", plugin)) return;

                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }

                    var banned = profile.getBannedPlayers();
                    if (banned.isEmpty()) {
                        player.sendMessage(configManager.getMessage("banlist-empty"));
                        return;
                    }
                    player.sendMessage(configManager.getMessage("banlist-header"));
                    for (UUID bannedId : banned) {
                        String name = Bukkit.getOfflinePlayer(bannedId).getName();
                        if (name == null) name = bannedId.toString();
                        player.sendMessage(configManager.getMessage("banlist-entry",
                                "<player>", name));
                    }
                }));
    }

    /**
     * Apply a ban and (if the target is online and currently inside the claim) eject
     * them via the EventListener. Bedrock players see a ModalForm confirmation first.
     */
    public void applyBan(Player owner, ClaimProfile profile, UUID targetId, String targetName) {
        if (GeyserFormHelper.shouldUseForms(owner)) {
            String title = configManager.getMessage("geyser-confirm-title");
            String content = configManager.getMessage("geyser-ban-content",
                    "<player>", targetName);
            GeyserFormHelper.sendModalForm(owner, title, content, "Confirm", "Cancel", confirmed -> {
                if (!confirmed) {
                    owner.sendMessage(configManager.getMessage("geyser-form-cancelled"));
                    return;
                }
                FoliaScheduler.runForPlayer(plugin, owner, () -> finishBan(owner, profile, targetId, targetName));
            });
        } else {
            finishBan(owner, profile, targetId, targetName);
        }
    }

    private void finishBan(Player owner, ClaimProfile profile, UUID targetId, String targetName) {
        profile.addBannedPlayer(targetId);
        // Removing the player's role/trust entries so a stale role can't accidentally
        // bypass the ban check via a future bug in PermissionResolver.
        profile.removeMember(targetId);
        profile.removeTrustedPlayer(targetId);

        plugin.getCacheManager().getProfileCache().put(profile.getProfileId(), profile);
        claimManager.saveAndSync(profile);

        owner.sendMessage(configManager.getMessage("player-banned",
                "<player>", targetName));

        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage(configManager.getMessage("banned-from-claim",
                    "<owner>", owner.getName()));
            // Eject from the claim if they're currently inside it.
            plugin.getListenerManager().getEventListener().ejectBannedPlayer(target, profile);
        }
    }

    private UUID resolveTargetId(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return online.getUniqueId();
        @SuppressWarnings("deprecation")
        var offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore() || offline.getUniqueId() != null) {
            // hasPlayedBefore() is true for any player with a UUID; offline.getUniqueId()
            // is non-null for any looked-up name. Only proceed if the name resolved to a
            // real account (i.e. UUID is not the "unknown" zero-UUID).
            UUID id = offline.getUniqueId();
            if (id != null && id.getLeastSignificantBits() != 0 && id.getMostSignificantBits() != 0) {
                return id;
            }
        }
        return null;
    }
}
