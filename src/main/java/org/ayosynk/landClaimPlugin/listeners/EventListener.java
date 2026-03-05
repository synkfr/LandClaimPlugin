package org.ayosynk.landClaimPlugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EventListener implements Listener {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final Map<UUID, ChunkPosition> lastChunkMap = new HashMap<>();
    private final Map<UUID, String> lastActionBarMap = new HashMap<>();
    private final Map<UUID, Boolean> lastClaimStatusMap = new HashMap<>();

    public EventListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;

        startActionBarTask();
    }

    private void startActionBarTask() {
        int interval = configManager.getActionBarUpdateInterval();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateActionBar(player);
                }
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    private void updateActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        ChunkPosition currentPos = new ChunkPosition(player.getLocation().getChunk());
        ChunkPosition lastPos = lastChunkMap.get(playerId);

        boolean isClaimed = claimManager.isChunkClaimed(currentPos);
        boolean chunkChanged = lastPos == null || !currentPos.equals(lastPos);

        // If chunk changed, compute the new actionbar string and handle titles
        if (chunkChanged) {
            ClaimProfile newProfile = claimManager.getProfileAt(currentPos);
            ClaimProfile oldProfile = lastPos != null ? claimManager.getProfileAt(lastPos) : null;

            // Handle entry/leave titles if transitioning between different claims or
            // wilderness
            if (oldProfile != newProfile) {
                if (oldProfile != null && oldProfile.isEnterTitleEnabled()) {
                    String ownerName = plugin.getServer().getOfflinePlayer(oldProfile.getOwnerId()).getName();
                    if (ownerName == null)
                        ownerName = "Unknown";
                    String leaveTxt = oldProfile.getLeaveTitle().replace("<owner>", ownerName);
                    player.showTitle(net.kyori.adventure.title.Title.title(
                            Component.empty(),
                            MiniMessage.miniMessage().deserialize(leaveTxt)));
                }

                if (newProfile != null && newProfile.isEnterTitleEnabled()) {
                    String ownerName = plugin.getServer().getOfflinePlayer(newProfile.getOwnerId()).getName();
                    if (ownerName == null)
                        ownerName = "Unknown";
                    String enterTxt = newProfile.getEnterTitle().replace("<owner>", ownerName);
                    player.showTitle(net.kyori.adventure.title.Title.title(
                            MiniMessage.miniMessage().deserialize(enterTxt),
                            Component.empty()));
                }
            }

            lastChunkMap.put(playerId, currentPos);
            lastClaimStatusMap.put(playerId, isClaimed);

            String message;
            if (isClaimed) {
                UUID ownerId = claimManager.getChunkOwner(currentPos);
                String ownerName = plugin.getServer().getOfflinePlayer(ownerId).getName();
                if (ownerName == null)
                    ownerName = "Unknown";

                if (playerId.equals(ownerId)) {
                    message = configManager.getActionBarMessage("actionbar-owned-by-you");
                } else {
                    String status = PermissionResolver.getPlayerStatus(newProfile, playerId);
                    if (status.equals("member") || status.equals("trusted")) {
                        message = configManager.getActionBarMessage("actionbar-trusted").replace("<owner>", ownerName);
                    } else if (player.hasPermission("landclaim.admin")) {
                        message = configManager.getActionBarMessage("actionbar-admin").replace("<owner>", ownerName);
                    } else {
                        message = configManager.getActionBarMessage("actionbar-owned-by-other").replace("<owner>",
                                ownerName);
                    }
                }
            } else {
                message = configManager.getActionBarMessage("actionbar-wilderness");
            }

            lastActionBarMap.put(playerId, message);
        }

        // Always send the cached message to ensure persistence across all claims and
        // wilderness
        String cachedMessage = lastActionBarMap.get(playerId);
        if (cachedMessage != null) {
            sendActionBar(player, cachedMessage);
        }
    }

    private void sendActionBar(Player player, String message) {
        player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null)
            return;

        Chunk fromChunk = from.getChunk();
        Chunk toChunk = to.getChunk();
        if (fromChunk.equals(toChunk))
            return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Instantly update action bar and titles on chunk crossing
        updateActionBar(player);

        if (plugin.getCommandHandler().isAutoClaimEnabled(playerId)) {
            ChunkPosition pos = new ChunkPosition(toChunk);
            if (!claimManager.isChunkClaimed(pos)) {
                if (claimManager.claimChunk(player, toChunk)) {
                    player.sendMessage(configManager.getMessage("chunk-claimed"));
                }
            }
        }

        if (plugin.getCommandHandler().isAutoUnclaimEnabled(playerId)) {
            ChunkPosition fromPos = new ChunkPosition(fromChunk);
            if (claimManager.isChunkClaimed(fromPos) && claimManager.getChunkOwner(fromPos).equals(playerId)) {
                if (!isConnectedToOtherClaims(fromPos, playerId)) {
                    claimManager.unclaimChunk(fromChunk);
                    player.sendMessage(configManager.getMessage("auto-unclaimed"));
                }
            }
        }
    }

    private boolean isConnectedToOtherClaims(ChunkPosition pos, UUID playerId) {
        if (!configManager.requireConnectedClaims())
            return false;

        ClaimProfile profile = claimManager.getProfile(playerId);
        if (profile == null)
            return false;
        Set<ChunkPosition> claims = profile.getOwnedChunks();
        if (claims.isEmpty())
            return false;

        Set<ChunkPosition> neighbors = new HashSet<>(pos.getNeighbors(configManager.allowDiagonalConnections()));
        neighbors.retainAll(claims);
        neighbors.remove(pos);

        return !neighbors.isEmpty();
    }

    /**
     * Clean up player data when they quit to prevent memory leaks
     */
    public void cleanupPlayer(UUID playerId) {
        lastChunkMap.remove(playerId);
        lastActionBarMap.remove(playerId);
        lastClaimStatusMap.remove(playerId);
    }
}