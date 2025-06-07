package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;

public class ClaimVisualizer {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public ClaimVisualizer(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    public void showPlayerClaims(Player player, int duration) {
        UUID playerId = player.getUniqueId();
        Set<ChunkPosition> claims = claimManager.getPlayerClaims(playerId);
        World world = player.getWorld();
        Color color = configManager.getVisualizationColor();
        Location playerLoc = player.getLocation();
        double y = playerLoc.getY() + 1; // Above player's feet

        // Show particles for each claim
        for (ChunkPosition claim : claims) {
            if (!claim.getWorld().equals(world.getName())) continue;

            // Get chunk coordinates
            int chunkX = claim.getX();
            int chunkZ = claim.getZ();

            // Convert to world coordinates
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            // Show particles along the edges
            showEdgeParticles(player, minX, minZ, maxX, maxZ, y, color);
        }

        // Schedule removal of particles after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                // Particles automatically disappear after their duration
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    private void showEdgeParticles(Player player, int minX, int minZ, int maxX, int maxZ, double y, Color color) {
        World world = player.getWorld();
        double particleSpacing = 0.5; // Distance between particles

        // North edge (minZ)
        drawLine(player, world, minX, y, minZ, maxX, y, minZ, particleSpacing, color);

        // South edge (maxZ)
        drawLine(player, world, minX, y, maxZ, maxX, y, maxZ, particleSpacing, color);

        // West edge (minX)
        drawLine(player, world, minX, y, minZ, minX, y, maxZ, particleSpacing, color);

        // East edge (maxX)
        drawLine(player, world, maxX, y, minZ, maxX, y, maxZ, particleSpacing, color);
    }

    private void drawLine(Player player, World world,
                          double x1, double y1, double z1,
                          double x2, double y2, double z2,
                          double spacing, Color color) {
        Vector start = new Vector(x1, y1, z1);
        Vector end = new Vector(x2, y2, z2);
        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        direction.normalize().multiply(spacing);
        int particles = (int) (length / spacing);

        for (int i = 0; i < particles; i++) {
            Vector point = start.clone().add(direction.clone().multiply(i));
            player.spawnParticle(
                    Particle.DUST,
                    new Location(world, point.getX(), point.getY(), point.getZ()),
                    1, // Count
                    new Particle.DustOptions(color, 1.0f)
            );
        }
    }
}