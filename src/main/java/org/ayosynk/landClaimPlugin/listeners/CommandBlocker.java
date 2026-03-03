package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.PermissionResolver;

import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlocker implements Listener {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;

    public CommandBlocker(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("landclaim.admin"))
            return;

        String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();

        List<String> blockedCommands = plugin.getConfigManager().getBlockedCommands();
        if (blockedCommands.isEmpty() || !blockedCommands.contains(command)) {
            return;
        }

        ChunkPosition pos = new ChunkPosition(player.getLocation());
        if (!claimManager.isChunkClaimed(pos)) {
            return;
        }

        ClaimProfile profile = claimManager.getProfileAt(pos);
        if (profile == null) {
            return;
        }

        if (PermissionResolver.hasPermission(profile, player.getUniqueId(), "MANAGE_SETTINGS")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getMessage("command-blocked"));
    }
}