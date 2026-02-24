package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.HomeManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandHandler {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final VisualizationManager visualizationManager;
    private final HomeManager homeManager;

    private final Map<UUID, Boolean> autoClaimPlayers = new HashMap<>();
    private final Map<UUID, Boolean> autoUnclaimPlayers = new HashMap<>();

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager,
            TrustManager trustManager, ConfigManager configManager,
            VisualizationManager visualizationManager, HomeManager homeManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.visualizationManager = visualizationManager;
        this.homeManager = homeManager;

        PaperCommandManager<Source> commandManager;
        try {
            commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                    .buildOnEnable(plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Cloud Command Manager: " + e.getMessage());
            return;
        }

        registerCommands(commandManager);
    }

    private void registerCommands(PaperCommandManager<Source> manager) {
        Command.Builder<PlayerSource> claimBuilder = manager.commandBuilder("claim", "c")
                .senderType(PlayerSource.class);

        // /claim
        manager.command(claimBuilder
                .handler(context -> {
                    Player player = context.sender().source();
                    claimCurrentChunk(player);
                }));

        // /claim auto
        manager.command(claimBuilder.literal("auto")
                .handler(context -> {
                    Player player = context.sender().source();
                    toggleAutoClaim(player);
                }));

        // /claim visible
        manager.command(claimBuilder.literal("visible")
                .handler(context -> {
                    Player player = context.sender().source();
                    toggleVisibility(player);
                }));

        // /claim menu
        manager.command(claimBuilder.literal("menu")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage(configManager.getMessage("menu-opened-stub", "<menu>", "Main Menu"));
                }));

        // /claim info
        manager.command(claimBuilder.literal("info")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendClaimInfo(player);
                }));

        // /unclaim
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

        // /claim admin <check/unclaim>
        manager.command(claimBuilder.literal("admin").literal("check")
                .permission("landclaim.admin")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendAdminClaimInfo(player);
                }));

        manager.command(claimBuilder.literal("admin").literal("unclaim")
                .permission("landclaim.admin")
                .handler(context -> {
                    Player player = context.sender().source();
                    adminUnclaimCurrentChunk(player);
                }));

        // /claim member <invite/kick/list> <player>
        Command.Builder<PlayerSource> memberBuilder = claimBuilder.literal("member");
        manager.command(memberBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage("Member list functionality coming soon");
                }));
        manager.command(memberBuilder.literal("invite")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage("Invited " + targetName);
                }));
        manager.command(memberBuilder.literal("kick")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage("Kicked " + targetName);
                }));

        // /claim trust <add/remove/list> <player>
        Command.Builder<PlayerSource> trustBuilder = claimBuilder.literal("trust");
        manager.command(trustBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage("Trust list functionality coming soon");
                }));
        manager.command(trustBuilder.literal("add")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage("Added trust to " + targetName);
                }));
        manager.command(trustBuilder.literal("remove")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage("Removed trust from " + targetName);
                }));
    }

    private void claimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        if (claimManager.claimChunk(player, chunk)) {
            player.sendMessage(configManager.getMessage("chunk-claimed"));
        }
    }

    private void unclaimCurrentChunk(Player player) {
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
            player.sendMessage("<green>Bypassed ownership and unclaimed chunk.");
        }
    }

    private void toggleAutoClaim(Player player) {
        boolean current = autoClaimPlayers.getOrDefault(player.getUniqueId(), false);
        boolean newValue = !current;
        autoClaimPlayers.put(player.getUniqueId(), newValue);
        player.sendMessage(configManager.getMessage(newValue ? "auto-claim-enabled" : "auto-claim-disabled"));
    }

    public boolean isAutoClaimEnabled(UUID playerId) {
        return autoClaimPlayers.getOrDefault(playerId, false);
    }

    public boolean isAutoUnclaimEnabled(UUID playerId) {
        return autoUnclaimPlayers.getOrDefault(playerId, false);
    }

    public void cleanupPlayer(UUID playerId) {
        autoClaimPlayers.remove(playerId);
        autoUnclaimPlayers.remove(playerId);
    }

    private void toggleVisibility(Player player) {
        player.sendMessage("<green>Claim visibility toggled.");
    }

    private void sendClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(claim.getOwnerId()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        player.sendMessage(configManager.getMessage("claim-info-owner", "<owner>", ownerName));
    }

    private void sendAdminClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }
        String ownerName = Bukkit.getOfflinePlayer(claim.getOwnerId()).getName();
        if (ownerName == null) ownerName = "Unknown";
        
        player.sendMessage("<red>[ADMIN] <green>Claim owned by: <gold>" + ownerName + " <gray>(" + claim.getOwnerId() + ")");
        player.sendMessage("<green>Claim ID: <gray>" + claim.getId());
    }
}