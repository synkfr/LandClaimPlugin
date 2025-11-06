package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final VisualizationManager visualizationManager;

    public PlayerJoinListener(VisualizationManager visualizationManager) {
        this.visualizationManager = visualizationManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        visualizationManager.handlePlayerJoin(event.getPlayer());
    }
}