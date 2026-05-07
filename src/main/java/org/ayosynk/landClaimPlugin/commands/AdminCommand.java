package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.gui.MainMenuGUI;
import org.ayosynk.landClaimPlugin.gui.TrustManagementGUI;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

/**
 * Handles: /claim admin check, /claim admin unclaim, /claim admin add chunk
 * Permission-gated: landclaim.admin
 */
public class AdminCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public AdminCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
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

        // /claim admin add chunk <amount> <player>
        manager.command(claimBuilder.literal("admin").literal("add").literal("chunk")
                .permission("landclaim.admin")
                .required("amount", IntegerParser.integerParser(1))
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player sender = context.sender().source();
                    int amount = context.get("amount");
                    String targetName = context.get("player");
                    adminAddChunk(sender, amount, targetName);
                }));

        // /claim admin edit <owner>
        manager.command(claimBuilder.literal("admin").literal("edit")
                .permission("landclaim.admin")
                .required("owner", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player sender = context.sender().source();
                    String ownerName = context.get("owner");
                    adminEditProfile(sender, ownerName);
                }));

        // /claim admin trust <owner> add <player>
        manager.command(claimBuilder.literal("admin").literal("trust")
                .permission("landclaim.admin")
                .required("owner", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .literal("add")
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player sender = context.sender().source();
                    String ownerName = context.get("owner");
                    String playerName = context.get("player");
                    adminManageTrust(sender, ownerName, playerName, true);
                }));

        // /claim admin trust <owner> remove <player>
        manager.command(claimBuilder.literal("admin").literal("trust")
                .permission("landclaim.admin")
                .required("owner", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .literal("remove")
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player sender = context.sender().source();
                    String ownerName = context.get("owner");
                    String playerName = context.get("player");
                    adminManageTrust(sender, ownerName, playerName, false);
                }));
    }

    private void adminAddChunk(Player sender, int amount, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                sender.sendMessage(configManager.getMessage("player-not-found"));
                return;
            }

            UUID targetId = target.getUniqueId();
            ClaimPlayer claimPlayer = plugin.getCacheManager().getPlayerCache().getIfPresent(targetId);
            boolean inCache = claimPlayer != null;

            if (!inCache) {
                try {
                    claimPlayer = plugin.getDatabaseManager().getPlayerDao().getPlayer(targetId).join();
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load player data for admin add chunk: " + e.getMessage());
                    sender.sendMessage(configManager.getMessage("player-not-found"));
                    return;
                }
            }

            if (claimPlayer == null) {
                claimPlayer = new ClaimPlayer(targetId);
            }

            claimPlayer.setBonusClaimBlocks(claimPlayer.getBonusClaimBlocks() + amount);

            // Save to DB
            plugin.getDatabaseManager().getPlayerDao().savePlayer(claimPlayer).join();

            if (inCache) {
                plugin.getCacheManager().getPlayerCache().put(targetId, claimPlayer);
            }

            sender.sendMessage(configManager.getMessage("admin-add-chunk-success", "<amount>", String.valueOf(amount), "<player>", target.getName() != null ? target.getName() : targetName));
        });
    }

    private void sendAdminClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile profile = claimManager.getProfileAt(pos);

        if (profile == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }
        String ownerName = Bukkit.getOfflinePlayer(profile.getOwnerId()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        player.sendMessage(
                configManager.getMessage("admin-claim-info-owned-by", "<owner>", ownerName, "<uuid>",
                        profile.getOwnerId().toString()));
        player.sendMessage(configManager.getMessage("admin-claim-info-id", "<id>", profile.getName()));
    }

    private void adminUnclaimCurrentChunk(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Chunk chunk = player.getLocation().getChunk();
            ChunkPosition pos = new ChunkPosition(chunk);

            ClaimProfile profile = claimManager.getProfileAt(pos);
            if (profile == null) {
                player.sendMessage(configManager.getMessage("not-in-claim"));
                return;
            }

            if (claimManager.unclaimChunk(chunk)) {
                player.sendMessage(configManager.getMessage("admin-bypassed-unclaim"));
            }
        });
    }

    private void adminEditProfile(Player sender, String ownerName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(ownerName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        ClaimProfile profile = claimManager.getProfile(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(configManager.getMessage("no-profile-found"));
            return;
        }

        // Open the MainMenuGUI for the admin to manage this profile
        MainMenuGUI.open(sender, profile, plugin);
        sender.sendMessage(configManager.getMessage("admin-editing-profile", "<player>", target.getName()));
    }

    private void adminManageTrust(Player sender, String ownerName, String playerName, boolean add) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            if (owner == null || owner.getUniqueId() == null || target == null || target.getUniqueId() == null) {
                sender.sendMessage(configManager.getMessage("player-not-found"));
                return;
            }

            ClaimProfile profile = claimManager.getProfile(owner.getUniqueId());
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            if (add) {
                if (profile.isOwner(target.getUniqueId())) {
                    sender.sendMessage(configManager.getMessage("cannot-trust-self"));
                    return;
                }
                profile.addTrustedPlayer(target.getUniqueId());
                claimManager.saveAndSync(profile);
                sender.sendMessage(configManager.getMessage("admin-trust-added", "<owner>", owner.getName(), "<player>", target.getName()));
            } else {
                if (!profile.isTrusted(target.getUniqueId())) {
                    sender.sendMessage(configManager.getMessage("not-trusted"));
                    return;
                }
                profile.removeTrustedPlayer(target.getUniqueId());
                claimManager.saveAndSync(profile);
                sender.sendMessage(configManager.getMessage("admin-trust-removed", "<owner>", owner.getName(), "<player>", target.getName()));
            }
        });
    }
}
