package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.MinimapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MinimapListener implements Listener {

    private final MinimapManager minimapManager;

    public MinimapListener(LandClaimPlugin plugin) {
        this.minimapManager = plugin.getMinimapManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && minimapManager.isMinimapItem(event.getItem())) {
                event.setCancelled(true);
                minimapManager.toggleZoom(event.getPlayer());
            }
        }
    }
}
