package org.ayosynk.landClaimPlugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventListener implements Listener {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final Map<UUID, ChunkPosition> lastChunkMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastActionBarMap = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> lastClaimStatusMap = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastPlayerStatusMap = new ConcurrentHashMap<>();

    public EventListener(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;

        startActionBarTask();
    }

    private void startActionBarTask() {
        int interval = configManager.getActionBarUpdateInterval();
        // Folia: dispatch per-player work to each player's region thread.
        // Paper: a single global timer that iterates online players.
        FoliaScheduler.runPlayerTaskTimer(plugin, this::updateActionBar, 0, interval);
    }

    private void updateActionBar(Player player) {
        UUID playerId = player.getUniqueId();
        ChunkPosition currentPos = new ChunkPosition(player.getLocation().getChunk());
        ChunkPosition lastPos = lastChunkMap.get(playerId);

        boolean isClaimed = claimManager.isChunkClaimed(currentPos);
        ClaimProfile newProfile = claimManager.getProfileAt(currentPos);

        String currentStatus;
        if (!isClaimed || newProfile == null) {
            currentStatus = "wilderness";
        } else {
            UUID profileId = newProfile.getProfileId();
            String relation = PermissionResolver.getPlayerStatus(newProfile, playerId);
            boolean isAdmin = player.hasPermission("landclaim.admin");
            boolean isOwner = playerId.equals(newProfile.getOwnerId());
            currentStatus = (profileId != null ? profileId.toString() : "null") + ":" + (isOwner ? "owner" : (isAdmin ? "admin" : relation));
        }

        String lastStatus = lastPlayerStatusMap.get(playerId);
        boolean chunkChanged = lastPos == null || !currentPos.equals(lastPos);
        boolean statusChanged = lastStatus == null || !currentStatus.equals(lastStatus);
        boolean isClaimedChanged = lastClaimStatusMap.get(playerId) == null || isClaimed != lastClaimStatusMap.get(playerId);

        // If chunk, claim status, or relationship status changed, compute the new actionbar string and handle titles
        if (chunkChanged || statusChanged || isClaimedChanged) {
            ClaimProfile oldProfile = lastPos != null ? claimManager.getProfileAt(lastPos) : null;

            // Handle entry/leave titles if transitioning between different claims or wilderness
            if (oldProfile != newProfile) {
                net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofMillis(1000),
                        java.time.Duration.ofMillis(500));

                if (oldProfile != null && oldProfile.isEnterTitleEnabled()) {
                    String ownerName = oldProfile.getColoredOwnerName();
                    String claimName = oldProfile.getColoredName();
                    String leaveTxt = oldProfile.getLeaveTitle()
                            .replace("<owner>", ownerName)
                            .replace("<claim>", claimName)
                            .replace("<name>", claimName);
                    Component leaveComp = MiniMessage.miniMessage().deserialize(leaveTxt);
                    boolean leaveSub = "SUBTITLE".equalsIgnoreCase(oldProfile.getLeaveTitleMode());
                    player.showTitle(net.kyori.adventure.title.Title.title(
                            leaveSub ? Component.empty() : leaveComp,
                            leaveSub ? leaveComp : Component.empty(),
                            times));
                }

                if (newProfile != null && newProfile.isEnterTitleEnabled()) {
                    String ownerName = newProfile.getColoredOwnerName();
                    String claimName = newProfile.getColoredName();
                    String enterTxt = newProfile.getEnterTitle()
                            .replace("<owner>", ownerName)
                            .replace("<claim>", claimName)
                            .replace("<name>", claimName);
                    Component enterComp = MiniMessage.miniMessage().deserialize(enterTxt);
                    boolean enterSub = "SUBTITLE".equalsIgnoreCase(newProfile.getEnterTitleMode());
                    player.showTitle(net.kyori.adventure.title.Title.title(
                            enterSub ? Component.empty() : enterComp,
                            enterSub ? enterComp : Component.empty(),
                            times));
                }
            }

            lastChunkMap.put(playerId, currentPos);
            lastClaimStatusMap.put(playerId, isClaimed);
            lastPlayerStatusMap.put(playerId, currentStatus);

            String message;
            if (isClaimed && newProfile != null) {
                UUID ownerId = claimManager.getChunkOwner(currentPos);
                String ownerName = newProfile.getColoredOwnerName();
                String claimName = newProfile.getColoredName();

                if (playerId.equals(ownerId)) {
                    message = configManager.getActionBarMessage("actionbar-owned-by-you")
                            .replace("<claim>", claimName)
                            .replace("<name>", claimName);
                } else {
                    String status = PermissionResolver.getPlayerStatus(newProfile, playerId);
                    if (status.equals("member") || status.equals("trusted")) {
                        message = configManager.getActionBarMessage("actionbar-trusted")
                                .replace("<owner>", ownerName)
                                .replace("<claim>", claimName)
                                .replace("<name>", claimName);
                    } else if (player.hasPermission("landclaim.admin")) {
                        message = configManager.getActionBarMessage("actionbar-admin")
                                .replace("<owner>", ownerName)
                                .replace("<claim>", claimName)
                                .replace("<name>", claimName);
                    } else {
                        message = configManager.getActionBarMessage("actionbar-owned-by-other")
                                .replace("<owner>", ownerName)
                                .replace("<claim>", claimName)
                                .replace("<name>", claimName);
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

        if (configManager.requireConnectedClaims() && plugin.getCommandHandler().isAutoUnclaimEnabled(playerId)) {
            ChunkPosition fromPos = new ChunkPosition(fromChunk);
            ClaimProfile activeProfile = claimManager.getActiveProfile(player);
            if (activeProfile != null && claimManager.isChunkClaimed(fromPos) && activeProfile.getProfileId().equals(claimManager.getChunkOwner(fromPos))) {
                if (!isConnectedToOtherClaims(fromPos, activeProfile)) {
                    claimManager.unclaimChunk(fromChunk);
                    player.sendMessage(configManager.getMessage("auto-unclaimed"));
                }
            }
        }
    }

    private boolean isConnectedToOtherClaims(ChunkPosition pos, ClaimProfile profile) {
        if (!configManager.requireConnectedClaims())
            return false;

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
        lastPlayerStatusMap.remove(playerId);
    }

    /**
     * Invalidate player's action bar cache so the next periodic tick recomputes.
     * Called by commands that change claim state (claim/unclaim).
     */
    public void updatePlayerClaimCache(Player player) {
        UUID playerId = player.getUniqueId();
        // Remove cached state to force full recomputation on next periodic tick
        lastChunkMap.remove(playerId);
        lastActionBarMap.remove(playerId);
        lastClaimStatusMap.remove(playerId);
        lastPlayerStatusMap.remove(playerId);
    }
}
