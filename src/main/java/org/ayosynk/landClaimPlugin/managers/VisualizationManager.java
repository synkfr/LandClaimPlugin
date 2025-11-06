package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Edge;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class VisualizationManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    // Cache for merged edges: PlayerID -> WorldName -> Edges
    private final Map<UUID, Map<String, Set<Edge>>> mergedEdgesCache = new HashMap<>();

    // Visualization modes: PlayerID -> Mode
    private final Map<UUID, VisualizationMode> visualizationModes = new HashMap<>();

    public enum VisualizationMode {
        ALWAYS
    }

    public VisualizationManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
        startVisualizationTask();
    }

    private void startVisualizationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, VisualizationMode> entry : visualizationModes.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        showPlayerClaims(player, entry.getValue());
                    }
                }
            }
        }.runTaskTimer(plugin, 0, configManager.getVisualizationUpdateInterval());
    }

    public void showPlayerClaims(Player player, VisualizationMode mode) {
        UUID playerId = player.getUniqueId();
        Set<ChunkPosition> claims = claimManager.getPlayerClaims(playerId);
        World world = player.getWorld();
        String worldName = world.getName();

        // Get merged edges
        Set<Edge> edges = getMergedEdges(playerId, worldName, claims);

        // Get visualization color
        Color color = mode == VisualizationMode.ALWAYS ?
                configManager.getVisualizationColor("always-color") :
                configManager.getVisualizationColor("temporary-color");

        // Show particles along edges
        showEdges(player, edges, color);
    }

    public void showTemporary(Player player) {
        UUID playerId = player.getUniqueId();
        Set<ChunkPosition> claims = claimManager.getPlayerClaims(playerId);
        World world = player.getWorld();
        String worldName = world.getName();

        // Get merged edges
        Set<Edge> edges = getMergedEdges(playerId, worldName, claims);

        // Get visualization color
        Color color = configManager.getVisualizationColor("temporary-color");

        // Show particles along edges
        showEdges(player, edges, color);
    }

    private Set<Edge> getMergedEdges(UUID playerId, String worldName, Set<ChunkPosition> claims) {
        // Use cached edges if available
        if (mergedEdgesCache.containsKey(playerId)) {
            Map<String, Set<Edge>> worldCache = mergedEdgesCache.get(playerId);
            if (worldCache.containsKey(worldName)) {
                return worldCache.get(worldName);
            }
        }

        // Calculate merged edges
        Map<Edge, Integer> edgeCounts = new HashMap<>();

        for (ChunkPosition claim : claims) {
            if (!claim.getWorld().equals(worldName)) continue;

            int minX = claim.getX() << 4;
            int minZ = claim.getZ() << 4;
            int maxX = minX + 16;
            int maxZ = minZ + 16;

            // Create edges for this claim
            Edge north = new Edge(minX, minZ, maxX, minZ);
            Edge south = new Edge(minX, maxZ, maxX, maxZ);
            Edge west = new Edge(minX, minZ, minX, maxZ);
            Edge east = new Edge(maxX, minZ, maxX, maxZ);

            // Count edge occurrences
            edgeCounts.put(north, edgeCounts.getOrDefault(north, 0) + 1);
            edgeCounts.put(south, edgeCounts.getOrDefault(south, 0) + 1);
            edgeCounts.put(west, edgeCounts.getOrDefault(west, 0) + 1);
            edgeCounts.put(east, edgeCounts.getOrDefault(east, 0) + 1);
        }

        // Only keep edges that are unique (not shared with another claim)
        Set<Edge> uniqueEdges = new HashSet<>();
        for (Map.Entry<Edge, Integer> entry : edgeCounts.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueEdges.add(entry.getKey());
            }
        }

        // Cache the result
        cacheEdges(playerId, worldName, uniqueEdges);
        return uniqueEdges;
    }

    private void cacheEdges(UUID playerId, String worldName, Set<Edge> edges) {
        mergedEdgesCache.computeIfAbsent(playerId, k -> new HashMap<>()).put(worldName, edges);
    }

    public void invalidateCache(UUID playerId) {
        mergedEdgesCache.remove(playerId);
    }

    private void showEdges(Player player, Set<Edge> edges, Color color) {
        double spacing = configManager.getParticleSpacing();
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        double y = player.getLocation().getY() + 1;

        for (Edge edge : edges) {
            drawEdge(player, edge, y, spacing, dustOptions);
        }
    }

    private void drawEdge(Player player, Edge edge, double y, double spacing, Particle.DustOptions dustOptions) {
        World world = player.getWorld();
        Vector start = new Vector(edge.x1, y, edge.z1);
        Vector end = new Vector(edge.x2, y, edge.z2);

        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        direction.normalize().multiply(spacing);
        int particles = (int) (length / spacing);

        for (int i = 0; i < particles; i++) {
            Vector point = start.clone().add(direction.clone().multiply(i));
            player.spawnParticle(
                    Particle.DUST,
                    point.getX(), point.getY(), point.getZ(),
                    1, // Count
                    0, 0, 0, // Offset
                    0, // Speed
                    dustOptions
            );
        }
    }

    public void setVisualizationMode(UUID playerId, VisualizationMode mode) {
        if (mode == null) {
            visualizationModes.remove(playerId);
        } else {
            visualizationModes.put(playerId, mode);
        }
    }

    public VisualizationMode getVisualizationMode(UUID playerId) {
        return visualizationModes.get(playerId);
    }

    // Add player join handler
    public void handlePlayerJoin(Player player) {
        if (!visualizationModes.containsKey(player.getUniqueId())) {
            String defaultMode = configManager.getDefaultVisualizationMode();
            if ("ALWAYS".equalsIgnoreCase(defaultMode)) {
                visualizationModes.put(player.getUniqueId(), VisualizationMode.ALWAYS);
            }
        }
    }

}