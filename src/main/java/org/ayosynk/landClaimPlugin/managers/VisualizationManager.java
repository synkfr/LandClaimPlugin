package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Edge;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VisualizationManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    private static final int MAX_CACHE_ENTRIES = 100;
    private final Map<UUID, Map<String, Set<Edge>>> mergedEdgesCache = new ConcurrentHashMap<>();
    private final AtomicInteger cacheSize = new AtomicInteger(0);

    private final Map<UUID, VisualizationMode> visualizationModes = new ConcurrentHashMap<>();

    public enum VisualizationMode {
        ALWAYS
    }

    public VisualizationManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
        loadPlayerData();
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
        Color color = mode == VisualizationMode.ALWAYS ? configManager.getVisualizationColor("always-color")
                : configManager.getVisualizationColor("temporary-color");

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
        if (mergedEdgesCache.containsKey(playerId)) {
            Map<String, Set<Edge>> worldCache = mergedEdgesCache.get(playerId);
            if (worldCache.containsKey(worldName)) {
                return worldCache.get(worldName);
            }
        }

        Map<Edge, Integer> edgeCounts = new HashMap<>();

        for (ChunkPosition claim : claims) {
            if (!claim.world().equals(worldName))
                continue;

            int minX = claim.x() << 4;
            int minZ = claim.z() << 4;
            int maxX = minX + 16;
            int maxZ = minZ + 16;

            Edge north = new Edge(minX, minZ, maxX, minZ);
            Edge south = new Edge(minX, maxZ, maxX, maxZ);
            Edge west = new Edge(minX, minZ, minX, maxZ);
            Edge east = new Edge(maxX, minZ, maxX, maxZ);

            edgeCounts.put(north, edgeCounts.getOrDefault(north, 0) + 1);
            edgeCounts.put(south, edgeCounts.getOrDefault(south, 0) + 1);
            edgeCounts.put(west, edgeCounts.getOrDefault(west, 0) + 1);
            edgeCounts.put(east, edgeCounts.getOrDefault(east, 0) + 1);
        }

        Set<Edge> uniqueEdges = new HashSet<>();
        for (Map.Entry<Edge, Integer> entry : edgeCounts.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueEdges.add(entry.getKey());
            }
        }

        cacheEdges(playerId, worldName, uniqueEdges);
        return uniqueEdges;
    }

    private void cacheEdges(UUID playerId, String worldName, Set<Edge> edges) {
        if (!mergedEdgesCache.containsKey(playerId) && cacheSize.get() >= MAX_CACHE_ENTRIES) {
            evictOldestCacheEntry();
        }

        boolean isNewPlayer = !mergedEdgesCache.containsKey(playerId);
        mergedEdgesCache.computeIfAbsent(playerId, k -> new HashMap<>()).put(worldName, edges);

        if (isNewPlayer) {
            cacheSize.incrementAndGet();
        }
    }

    private void evictOldestCacheEntry() {
        Iterator<UUID> iterator = mergedEdgesCache.keySet().iterator();
        if (iterator.hasNext()) {
            UUID toRemove = iterator.next();
            if (Bukkit.getPlayer(toRemove) == null) {
                mergedEdgesCache.remove(toRemove);
                cacheSize.decrementAndGet();
            }
        }
    }

    public void invalidateCache(UUID playerId) {
        if (mergedEdgesCache.remove(playerId) != null) {
            cacheSize.decrementAndGet();
        }
    }

    private void showEdges(Player player, Set<Edge> edges, Color color) {
        double spacing = configManager.getParticleSpacing();
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        double y = player.getLocation().getY() + 1;
        World world = player.getWorld();

        List<Location> particleLocations = new ArrayList<>();

        for (Edge edge : edges) {
            collectEdgeParticles(world, edge, y, spacing, particleLocations);
        }

        for (Location loc : particleLocations) {
            player.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dustOptions);
        }
    }

    private void collectEdgeParticles(World world, Edge edge, double y, double spacing, List<Location> locations) {
        double dx = edge.x2() - edge.x1();
        double dz = edge.z2() - edge.z1();
        double length = Math.sqrt(dx * dx + dz * dz);

        if (length == 0)
            return;

        int particles = (int) (length / spacing);
        double stepX = dx / particles;
        double stepZ = dz / particles;

        for (int i = 0; i < particles; i++) {
            locations.add(new Location(world, edge.x1() + stepX * i, y, edge.z1() + stepZ * i));
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

    public void handlePlayerJoin(Player player) {
        if (!visualizationModes.containsKey(player.getUniqueId())) {
            String defaultMode = configManager.getDefaultVisualizationMode();
            if ("ALWAYS".equalsIgnoreCase(defaultMode)) {
                visualizationModes.put(player.getUniqueId(), VisualizationMode.ALWAYS);
            }
        }
    }

    public void handlePlayerQuit(UUID playerId) {
        savePlayerState(playerId);
        visualizationModes.remove(playerId);
        if (mergedEdgesCache.remove(playerId) != null) {
            cacheSize.decrementAndGet();
        }
    }

    private void loadPlayerData() {
        var playerDataConfig = configManager.getPlayerDataConfig();
        var vizSection = playerDataConfig.getConfigurationSection("visualization-modes");

        if (vizSection != null) {
            for (String uuidStr : vizSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    String modeStr = vizSection.getString(uuidStr);
                    if ("ALWAYS".equalsIgnoreCase(modeStr)) {
                        visualizationModes.put(playerId, VisualizationMode.ALWAYS);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void savePlayerState(UUID playerId) {
        var playerDataConfig = configManager.getPlayerDataConfig();
        String uuidStr = playerId.toString();

        if (visualizationModes.containsKey(playerId)) {
            playerDataConfig.set("visualization-modes." + uuidStr, visualizationModes.get(playerId).name());
        } else {
            playerDataConfig.set("visualization-modes." + uuidStr, null);
        }

        configManager.savePlayerData();
    }

    public void saveAllPlayerData() {
        var playerDataConfig = configManager.getPlayerDataConfig();

        for (Map.Entry<UUID, VisualizationMode> entry : visualizationModes.entrySet()) {
            playerDataConfig.set("visualization-modes." + entry.getKey().toString(), entry.getValue().name());
        }

        configManager.savePlayerData();
    }
}