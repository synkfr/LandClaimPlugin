package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.LinkedList;
import java.util.Queue;

public class UnstuckCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public UnstuckCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        manager.command(claimBuilder.literal("unstuck")
                .handler(ctx -> {
                    Player player = ctx.sender().source();
                    if (!org.ayosynk.landClaimPlugin.gui.GuiHelper.checkPermission(player, "landclaim.unstuck", plugin)) return;
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> unstuckPlayer(player));
                }));
    }

    private void unstuckPlayer(Player player) {
        ChunkPosition startPos = new ChunkPosition(player.getLocation().getChunk());

        if (!claimManager.isChunkClaimed(startPos)) {
            player.sendMessage(configManager.getMessage("unstuck-not-in-claim"));
            return;
        }

        player.sendMessage(configManager.getMessage("unstuck-searching"));

        Queue<ChunkPosition> queue = new LinkedList<>();
        java.util.Set<ChunkPosition> visited = new java.util.HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int maxRadiusChunks = 50;
        ChunkPosition safeChunk = null;

        while (!queue.isEmpty()) {
            ChunkPosition current = queue.poll();

            if (!claimManager.isChunkClaimed(current)) {
                safeChunk = current;
                break;
            }

            if (Math.abs(current.x() - startPos.x()) > maxRadiusChunks || Math.abs(current.z() - startPos.z()) > maxRadiusChunks) {
                continue;
            }

            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : dirs) {
                ChunkPosition neighbor = new ChunkPosition(current.world(), current.x() + dir[0], current.z() + dir[1]);
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        if (safeChunk != null) {
            final ChunkPosition target = safeChunk;
            Bukkit.getScheduler().runTask(plugin, () -> {
                World world = Bukkit.getWorld(target.world());
                if (world != null) {
                    int blockX = (target.x() << 4) + 8;
                    int blockZ = (target.z() << 4) + 8;
                    int blockY = world.getHighestBlockYAt(blockX, blockZ);

                    Location safeLoc = new Location(world, blockX + 0.5, blockY + 1.0, blockZ + 0.5);
                    player.teleportAsync(safeLoc).thenAccept(success -> {
                        if (success) {
                            player.sendMessage(configManager.getMessage("unstuck-success"));
                        } else {
                            player.sendMessage(configManager.getMessage("unstuck-failed"));
                        }
                    });
                }
            });
        } else {
            player.sendMessage(configManager.getMessage("unstuck-failed"));
        }
    }
}
