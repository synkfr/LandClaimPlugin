package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.MainMenuGUI;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

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

        // /claim menu — GUI must open on main thread
        manager.command(claimBuilder.literal("menu")
                .handler(context -> {
                    Player player = context.sender().source();
                    ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
                    Claim claim = claimManager.getClaimAt(pos);
                    if (claim == null) {
                        player.sendMessage(configManager.getMessage("not-in-claim"));
                        return;
                    }
                    // Dispatch GUI opening to main thread — async handler cannot open inventories
                    Bukkit.getScheduler().runTask(plugin, () -> MainMenuGUI.open(player, claim, plugin));
                }));

        // /claim info
        manager.command(claimBuilder.literal("info")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendClaimInfo(player);
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
}
