package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final LandClaimPlugin plugin;
    private final VisualizationManager visualizationManager;

    public PlayerJoinListener(LandClaimPlugin plugin, VisualizationManager visualizationManager) {
        this.plugin = plugin;
        this.visualizationManager = visualizationManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        visualizationManager.handlePlayerJoin(event.getPlayer());
        plugin.getDatabaseManager().getPlayerDao().getPlayer(event.getPlayer().getUniqueId())
                .thenAccept(claimPlayer -> {
                    plugin.getCacheManager().getPlayerCache().put(event.getPlayer().getUniqueId(), claimPlayer);
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        visualizationManager.handlePlayerQuit(playerId);
        plugin.getCommandHandler().cleanupPlayer(playerId);
        plugin.getEventListener().cleanupPlayer(playerId);
    }
}