package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles player join and quit events for initialization and cleanup
 */
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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Clean up visualization data
        visualizationManager.handlePlayerQuit(playerId);
        
        // Clean up command handler data
        plugin.getCommandHandler().cleanupPlayer(playerId);
        
        // Clean up event listener data
        plugin.getEventListener().cleanupPlayer(playerId);
    }
}