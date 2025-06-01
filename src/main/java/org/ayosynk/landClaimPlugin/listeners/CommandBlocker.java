package org.ayosynk.landClaimPlugin.listeners;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;

public class CommandBlocker implements Listener {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;

    public CommandBlocker(LandClaimPlugin plugin, ClaimManager claimManager, TrustManager trustManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].substring(1).toLowerCase(); // Remove slash

        // Get blocked commands
        List<String> blockedCommands = plugin.getConfigManager().getBlockedCommands();
        if (blockedCommands.isEmpty() || !blockedCommands.contains(command)) {
            return; // Command not blocked
        }

        // Check if player is in a claimed chunk
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        if (!claimManager.isChunkClaimed(pos)) {
            return; // Not in claimed land
        }

        UUID owner = claimManager.getChunkOwner(pos);

        // Allow owner and trusted players to use commands
        if (player.getUniqueId().equals(owner) ||
                trustManager.isTrusted(owner, player)) {
            return;
        }

        // Block the command
        event.setCancelled(true);
        player.sendMessage(ChatUtils.colorize(
                plugin.getConfigManager().getConfig().getString("messages.command-blocked", "&cThis command is blocked in claimed land!")
        ));
    }
}