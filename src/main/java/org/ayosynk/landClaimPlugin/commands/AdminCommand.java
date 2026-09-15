package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
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
        register(manager, manager.commandBuilder("claim", "c", "landclaim"), claimBuilder);
    }

    public void register(PaperCommandManager<Source> manager, Command.Builder<Source> claimRoot,
            Command.Builder<PlayerSource> playerClaimBuilder) {
        Command.Builder<Source> adminBase = claimRoot.literal("admin")
                .permission("landclaim.admin");
        Command.Builder<PlayerSource> playerAdminBase = playerClaimBuilder.literal("admin")
                .permission("landclaim.admin");

        // /claim admin check (Player only - requires player location)
        manager.command(playerAdminBase.literal("check")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendAdminClaimInfo(player);
                }));

        // /claim admin unclaim (Player only - requires player location)
        manager.command(playerAdminBase.literal("unclaim")
                .handler(context -> {
                    Player player = context.sender().source();
                    adminUnclaimCurrentChunk(player);
                }));

        // /claim admin add chunk <player> <amount> (Console + Player)
        manager.command(adminBase.literal("add").literal("chunk")
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .required("amount", IntegerParser.integerParser(1))
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    String targetName = context.get("player");
                    int amount = context.get("amount");
                    adminAddChunk(sender, amount, targetName);
                }));

        // /claim admin edit <owner> (Player only - opens GUI)
        manager.command(playerAdminBase.literal("edit")
                .required("owner", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    Player sender = context.sender().source();
                    String ownerName = context.get("owner");
                    adminEditProfile(sender, ownerName);
                }));

        // /claim admin trust list <owner> (Console + Player)
        manager.command(adminBase.literal("trust").literal("list")
                .required("owner", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    String ownerName = context.get("owner");
                    adminTrustList(sender, ownerName);
                }));

        // /claim admin trust who <player> (Console + Player)
        manager.command(adminBase.literal("trust").literal("who")
                .required("player", StringParser.stringParser(), OfflinePlayerSuggestions.all())
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    String playerName = context.get("player");
                    adminTrustWho(sender, playerName);
                }));

        // /claim admin setalias <claimName/ownerName> <alias> (Console + Player)
        manager.command(adminBase.literal("setalias")
                .required("claim", StringParser.stringParser())
                .required("alias", StringParser.greedyStringParser())
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    String claimName = context.get("claim");
                    String alias = context.get("alias");
                    adminSetAlias(sender, claimName, alias);
                }));

        // /claim admin claim (Player only - requires player location)
        manager.command(playerAdminBase.literal("claim")
                .handler(context -> {
                    Player player = context.sender().source();
                    org.ayosynk.landClaimPlugin.models.ChunkPosition pos = new org.ayosynk.landClaimPlugin.models.ChunkPosition(player.getLocation());

                    if (claimManager.isChunkClaimed(pos)) {
                        UUID ownerId = claimManager.getChunkOwner(pos);
                        ClaimProfile existingProfile = claimManager.getProfile(ownerId);
                        String ownerName = existingProfile != null ? existingProfile.getDisplayOwnerName() : Bukkit.getOfflinePlayer(ownerId).getName();
                        player.sendMessage(configManager.getMessage("admin-already-claimed", "<owner>", ownerName != null ? ownerName : "Unknown"));
                        return;
                    }

                    // Get or Create Admin Profile
                    ClaimProfile adminProfile = claimManager.getProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID);
                    if (adminProfile == null) {
                        adminProfile = new ClaimProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, "Admin");
                        plugin.getCacheManager().getProfileCache().put(adminProfile.getProfileId(), adminProfile);
                    }

                    adminProfile.addChunk(pos);
                    claimManager.addToSpatialIndex(pos, adminProfile);
                    claimManager.saveAndSync(adminProfile);

                    plugin.getVisualizationManager().invalidateCache(adminProfile.getProfileId());
                    plugin.getHookManager().refreshMapHooks();

                    player.sendMessage(configManager.getMessage("admin-chunk-claimed"));
                }));

        // /claim admin menu (Player only - opens GUI)
        manager.command(playerAdminBase.literal("menu")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile adminProfile = claimManager.getProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID);

                    if (adminProfile == null) {
                        adminProfile = new ClaimProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, "Admin");
                        plugin.getCacheManager().getProfileCache().put(adminProfile.getProfileId(), adminProfile);
                        claimManager.saveAndSync(adminProfile);
                    }

                    final ClaimProfile finalProfile = adminProfile;
                    FoliaScheduler.runForPlayer(plugin, player, () -> MainMenuGUI.open(player, finalProfile, plugin));
                }));

        // /claim admin reload (Console + Player)
        manager.command(adminBase.literal("reload")
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    plugin.reloadPlugin();
                    sender.sendMessage(configManager.getMessage("reloaded"));
                }));
    }

    private void adminAddChunk(CommandSender sender, int amount, String targetName) {
        FoliaScheduler.runAsync(plugin, () -> {
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
        String ownerName = profile.getDisplayOwnerName();

        player.sendMessage(
                configManager.getMessage("admin-claim-info-owned-by", "<owner>", ownerName, "<uuid>",
                        profile.getProfileId().toString()));
        player.sendMessage(configManager.getMessage("admin-claim-info-id", "<id>", profile.getName()));
    }

    private void adminUnclaimCurrentChunk(Player player) {
        FoliaScheduler.runForPlayer(plugin, player, () -> {
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

    private void adminTrustList(CommandSender sender, String ownerName) {
        FoliaScheduler.runAsync(plugin, () -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
            if (owner == null || owner.getUniqueId() == null) {
                sender.sendMessage(configManager.getMessage("player-not-found"));
                return;
            }

            ClaimProfile profile = claimManager.getProfile(owner.getUniqueId());
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            var trusted = profile.getTrustedPlayerFlags();
            if (trusted.isEmpty()) {
                sender.sendMessage(configManager.getMessage("trust-list-empty"));
                return;
            }

            String safeOwnerName = escapeMiniMessage(owner.getName() != null ? owner.getName() : ownerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Trusted players for " + safeOwnerName + ":"));
            for (UUID trustedId : trusted.keySet()) {
                String name = Bukkit.getOfflinePlayer(trustedId).getName();
                if (name == null) name = trustedId.toString();
                String safeName = escapeMiniMessage(name);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gray>- <gold>" + safeName));
            }
        });
    }

    private void adminTrustWho(CommandSender sender, String playerName) {
        FoliaScheduler.runAsync(plugin, () -> {
            @SuppressWarnings("deprecation")
            OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(configManager.getMessage("player-not-found"));
                return;
            }

            UUID targetId = target.getUniqueId();
            boolean foundAny = false;
            String safeTargetName = escapeMiniMessage(target.getName() != null ? target.getName() : playerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Claims where " + safeTargetName + " is trusted:"));

            for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
                if (profile.isTrusted(targetId)) {
                    foundAny = true;
                    String ownerName = profile.getDisplayOwnerName();
                    String safeOwnerName = escapeMiniMessage(ownerName);
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<gray>- <gold>" + safeOwnerName));
                }
            }

            if (!foundAny) {
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>This player is not trusted in any claims."));
            }
        });
    }

    private void adminSetAlias(CommandSender sender, String claimName, String alias) {
        FoliaScheduler.runAsync(plugin, () -> {
            ClaimProfile profile = claimManager.getProfileByNameOrOwner(claimName);
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            String safeClaimName = escapeMiniMessage(profile.getName());
            if (alias.equalsIgnoreCase("reset") || alias.equalsIgnoreCase("none")) {
                profile.setOwnerAlias(null);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<green>Owner alias for claim '<white>" + safeClaimName + "<green>' has been reset."));
            } else {
                profile.setOwnerAlias(alias);
                String safeAlias = escapeMiniMessage(alias);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<green>Owner alias for claim '<white>" + safeClaimName + "<green>' set to: <white>" + safeAlias));
            }

            plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
        });
    }

    /**
     * Escape MiniMessage special characters to prevent injection attacks.
     */
    private String escapeMiniMessage(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("<", "\\<")
                    .replace(">", "\\>");
    }
}
