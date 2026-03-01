package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Edge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VisualizationManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    private static final int MAX_CACHE_ENTRIES = 100;
    private final Map<UUID, Map<String, Set<Edge>>> mergedEdgesCache = new ConcurrentHashMap<>();
    private final AtomicInteger cacheSize = new AtomicInteger(0);

    private final Map<UUID, VisualizationMode> visualizationModes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporaryTimers = new ConcurrentHashMap<>();

    private final Map<UUID, List<BlockDisplay>> activeDisplays = new ConcurrentHashMap<>();

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
        // Run a task every 20 ticks (1 second) to check for expired temporary
        // visualizations
        // and spawn displays if they are missing.
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                // Clean up expired temporary timers
                for (Iterator<Map.Entry<UUID, Long>> it = temporaryTimers.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<UUID, Long> entry = it.next();
                    if (now > entry.getValue()) {
                        it.remove();
                        // If player doesn't also have ALWAYS mode, clear displays
                        if (!visualizationModes.containsKey(entry.getKey())) {
                            clearDisplays(entry.getKey());
                        } else {
                            // Needs redraw because temporary material might be different from ALWAYS
                            // material
                            redrawDisplays(Bukkit.getPlayer(entry.getKey()));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean toggleVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (visualizationModes.containsKey(playerId)) {
            visualizationModes.remove(playerId);
            clearDisplays(playerId);
            return false;
        } else {
            visualizationModes.put(playerId, VisualizationMode.ALWAYS);
            redrawDisplays(player);
            return true;
        }
    }

    public void showTemporary(Player player) {
        UUID playerId = player.getUniqueId();
        temporaryTimers.put(playerId, System.currentTimeMillis() + 10000);
        redrawDisplays(player);
    }

    public void invalidateCache(UUID playerId) {
        if (mergedEdgesCache.remove(playerId) != null) {
            cacheSize.decrementAndGet();
        }
        // If they currently have displays showing, redraw them
        if (activeDisplays.containsKey(playerId)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                redrawDisplays(player);
            }
        }
    }

    private void redrawDisplays(Player player) {
        if (player == null || !player.isOnline())
            return;
        UUID playerId = player.getUniqueId();

        clearDisplays(playerId);

        // Determine material based on mode
        Material material = temporaryTimers.containsKey(playerId) ? Material.ORANGE_STAINED_GLASS
                : Material.LIME_STAINED_GLASS;

        Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = new HashSet<>(
                claimManager.getPlayerClaims(playerId));

        // Also include the claim the player is currently standing in, even if they
        // don't own it
        ChunkPosition currentChunk = new ChunkPosition(player.getLocation().getChunk());
        org.ayosynk.landClaimPlugin.models.Claim currentClaim = claimManager.getClaimAt(currentChunk);
        if (currentClaim != null) {
            claimObjects.add(currentClaim);
        }

        Set<ChunkPosition> claims = claimObjects.stream()
                .flatMap(claim -> claim.getChunks().stream())
                .collect(Collectors.toSet());

        if (claims.isEmpty()) {
            player.sendMessage("§cDebug: No claims to visualize.");
            return;
        }

        Set<Edge> edges = getMergedEdges(playerId, player.getWorld().getName(), claims);
        if (edges.isEmpty()) {
            player.sendMessage("§cDebug: No edges found.");
            return;
        }

        player.sendMessage("§aDebug: Spawning " + edges.size() + " edges.");
        spawnDisplays(player, edges, material);
    }

    private void spawnDisplays(Player player, Set<Edge> edges, Material material) {
        UUID playerId = player.getUniqueId();
        List<BlockDisplay> displays = new ArrayList<>();
        World world = player.getWorld();

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Edge edge : edges) {
                double minX = Math.min(edge.x1(), edge.x2());
                double minZ = Math.min(edge.z1(), edge.z2());
                double lengthX = Math.abs(edge.x2() - edge.x1());
                double lengthZ = Math.abs(edge.z2() - edge.z1());

                float scaleX = (float) (lengthX == 0 ? 0.05f : lengthX);
                float scaleZ = (float) (lengthZ == 0 ? 0.05f : lengthZ);
                float scaleY = 384f; // Build limit from -64 to 320

                double playerY = player.getLocation().getY();
                Location loc = new Location(world, minX, playerY, minZ);

                BlockDisplay display = world.spawn(loc, BlockDisplay.class, e -> {
                    e.setPersistent(false);
                    e.setVisibleByDefault(false);
                    e.setBlock(Bukkit.createBlockData(material));
                    e.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
                    e.setGravity(false);

                    // Adjust translation to center the extremely thin boundary block
                    float transX = lengthX == 0 ? -0.025f : 0f;
                    float transY = (float) (-64 - playerY); // shift origin back down to bedrock
                    float transZ = lengthZ == 0 ? -0.025f : 0f;

                    Transformation transform = new Transformation(
                            new Vector3f(transX, transY, transZ),
                            new AxisAngle4f(),
                            new Vector3f(scaleX, scaleY, scaleZ),
                            new AxisAngle4f());
                    e.setTransformation(transform);
                });

                player.showEntity(plugin, display);
                displays.add(display);
            }
            activeDisplays.put(playerId, displays);
        });
    }

    private void clearDisplays(UUID playerId) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            List<BlockDisplay> displays = activeDisplays.remove(playerId);
            if (displays != null) {
                for (BlockDisplay display : displays) {
                    if (display.isValid()) {
                        display.remove();
                    }
                }
            }
        });
    }

    // --- Caching and Edge Merging ---

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
        // Simplify adjacent edges
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

    public void setVisualizationMode(UUID playerId, VisualizationMode mode) {
        if (mode == null) {
            visualizationModes.remove(playerId);
            clearDisplays(playerId);
        } else {
            visualizationModes.put(playerId, mode);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                redrawDisplays(player);
            }
        }
    }

    public VisualizationMode getVisualizationMode(UUID playerId) {
        return visualizationModes.get(playerId);
    }

    public void handlePlayerJoin(Player player) {
        String defaultMode = configManager.getDefaultVisualizationMode();
        if ("ALWAYS".equalsIgnoreCase(defaultMode)) {
            visualizationModes.put(player.getUniqueId(), VisualizationMode.ALWAYS);
            redrawDisplays(player);
        }
    }

    public void handlePlayerQuit(UUID playerId) {
        visualizationModes.remove(playerId);
        temporaryTimers.remove(playerId);
        clearDisplays(playerId);
        if (mergedEdgesCache.remove(playerId) != null) {
            cacheSize.decrementAndGet();
        }
    }

    public void cleanupLocalDisplays() {
        for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
            clearDisplays(playerId);
        }
    }
}
