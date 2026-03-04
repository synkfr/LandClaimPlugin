package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.MainMenuGUI;
import org.ayosynk.landClaimPlugin.gui.WarpManagementGUI;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles: /claim, /claim auto, /claim visible, /claim info, /claim menu
 */
public class ClaimCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final VisualizationManager visualizationManager;

    private final Map<UUID, Boolean> autoClaimPlayers = new HashMap<>();
    private final Map<UUID, Boolean> autoUnclaimPlayers = new HashMap<>();

    public ClaimCommand(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager, VisualizationManager visualizationManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
        this.visualizationManager = visualizationManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
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

        // /claim create <name>
        manager.command(claimBuilder.literal("create")
                .required("name", StringParser.greedyStringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String name = context.get("name");
                    Bukkit.getScheduler().runTask(plugin, () -> createProfile(player, name));
                }));

        // /claim menu — accessible from anywhere, uses player's own profile
        manager.command(claimBuilder.literal("menu")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> MainMenuGUI.open(player, profile, plugin));
                }));

        // /claim info
        manager.command(claimBuilder.literal("info")
                .handler(context -> {
                    Player player = context.sender().source();
                    Bukkit.getScheduler().runTask(plugin, () -> sendClaimInfo(player));
                }));

        // /claim setwarp <name>
        manager.command(claimBuilder.literal("setwarp")
                .required("name", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String name = context.get("name");
                    Bukkit.getScheduler().runTask(plugin, () -> setWarp(player, name));
                }));

        // /claim delwarp <name>
        manager.command(claimBuilder.literal("delwarp")
                .required("name", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String name = context.get("name");
                    Bukkit.getScheduler().runTask(plugin, () -> delWarp(player, name));
                }));

        // /claim warp <name>
        manager.command(claimBuilder.literal("warp")
                .required("name", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String name = context.get("name");
                    Bukkit.getScheduler().runTask(plugin, () -> teleportToWarp(player, name));
                }));

        // /claim warps
        manager.command(claimBuilder.literal("warps")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> WarpManagementGUI.open(player, profile, plugin));
                }));
    }

    // --- State Management ---

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

    // --- Private Helpers ---

    private void claimCurrentChunk(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Chunk chunk = player.getLocation().getChunk();
            if (claimManager.claimChunk(player, chunk)) {
                player.sendMessage(configManager.getMessage("chunk-claimed"));
            }
        });
    }

    private void createProfile(Player player, String name) {
        UUID playerId = player.getUniqueId();

        if (!claimManager.canCreateProfile(playerId)) {
            player.sendMessage(configManager.getMessage("cannot-claim-as-member"));
            return;
        }

        ClaimProfile existing = claimManager.getProfile(playerId);
        if (existing != null) {
            player.sendMessage(configManager.getMessage("already-has-profile"));
            return;
        }

        if (!claimManager.isClaimNameUnique(name)) {
            player.sendMessage(configManager.getMessage("name-already-in-use"));
            return;
        }

        ClaimProfile profile = new ClaimProfile(playerId, name);
        plugin.getCacheManager().getProfileCache().put(playerId, profile);
        claimManager.saveAndSync(profile);
        player.sendMessage(configManager.getMessage("profile-created", "<name>", name));
    }

    private void toggleAutoClaim(Player player) {
        boolean current = autoClaimPlayers.getOrDefault(player.getUniqueId(), false);
        boolean newValue = !current;
        autoClaimPlayers.put(player.getUniqueId(), newValue);
        player.sendMessage(configManager.getMessage(newValue ? "auto-claim-enabled" : "auto-claim-disabled"));
    }

    private void toggleVisibility(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean isVisible = visualizationManager.toggleVisualization(player);
            if (isVisible) {
                player.sendMessage(configManager.getMessage("claim-visibility-toggled"));
            } else {
                player.sendMessage(configManager.getMessage("claim-visibility-toggled"));
            }
        });
    }

    private void sendClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile profile = claimManager.getProfileAt(pos);

        if (profile == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }

        String ownerName = Bukkit.getOfflinePlayer(profile.getOwnerId()).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        player.sendMessage(configManager.getMessage("claim-info-owner", "<owner>", ownerName));
    }

    private void setWarp(Player player, String name) {
        ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(configManager.getMessage("no-profile"));
            return;
        }

        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile atLoc = claimManager.getProfileAt(pos);

        if (atLoc == null || !atLoc.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(configManager.getMessage("not-in-own-claim"));
            return;
        }

        if (plugin.getWarpManager().getWarpCount(player.getUniqueId()) >= plugin.getWarpManager()
                .getWarpLimit(player)) {
            player.sendMessage(configManager.getMessage("warp-limit-reached"));
            return;
        }

        plugin.getWarpManager().setWarp(player.getUniqueId(), name, player.getLocation(), Material.ENDER_PEARL);
        profile.addWarp(new Warp(name, player.getLocation(), Material.ENDER_PEARL));
        player.sendMessage(configManager.getMessage("warp-set", "<name>", name));
    }

    private void delWarp(Player player, String name) {
        ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(configManager.getMessage("no-profile"));
            return;
        }

        if (plugin.getWarpManager().deleteWarp(player.getUniqueId(), name)) {
            profile.removeWarp(name);
            player.sendMessage(configManager.getMessage("warp-deleted", "<name>", name));
        } else {
            player.sendMessage(configManager.getMessage("warp-not-found", "<name>", name));
        }
    }

    private void teleportToWarp(Player player, String name) {
        ClaimProfile profile = claimManager.getProfile(player.getUniqueId());
        if (profile == null) {
            player.sendMessage(configManager.getMessage("no-profile"));
            return;
        }

        Warp warp = profile.getWarp(name);
        if (warp == null) {
            player.sendMessage(configManager.getMessage("warp-not-found", "<name>", name));
            return;
        }

        player.teleport(warp.getLocation());
        player.sendMessage(configManager.getMessage("warp-teleport", "<name>", name));
    }
}
