package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Edge;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

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

    // Store active block displays per player
    private final Map<UUID, List<BlockDisplay>> activeDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, List<BlockDisplay>> selectionDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporaryTimers = new ConcurrentHashMap<>();

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
        // We only need to check for claim updates or temporary timer expiration now
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        clearDisplays(playerId);
                        continue;
                    }

                    // Check temporary timer
                    if (temporaryTimers.containsKey(playerId)) {
                        if (now > temporaryTimers.get(playerId)) {
                            temporaryTimers.remove(playerId);
                            clearDisplays(playerId);
                            continue;
                        }
                    }

                    // For ALWAYS mode, ensure displays are valid
                    if (!temporaryTimers.containsKey(playerId) && !visualizationModes.containsKey(playerId)) {
                        clearDisplays(playerId);
                        continue;
                    }

                    // Note: If claims update, invalidateCache handles the redraw
                }
            }
        }.runTaskTimer(plugin, 0, 20L); // Check every second
    }

    public void showPlayerClaims(Player player, VisualizationMode mode) {
        UUID playerId = player.getUniqueId();

        // If we already have displays and cache is valid, do nothing
        if (activeDisplays.containsKey(playerId))
            return;

        Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = claimManager.getPlayerClaims(playerId);
        Set<ChunkPosition> claims = claimObjects.stream()
                .flatMap(claim -> claim.getChunks().stream())
                .collect(java.util.stream.Collectors.toSet());
        World world = player.getWorld();

        Set<Edge> edges = getMergedEdges(playerId, world.getName(), claims);
        Color color = configManager.getVisualizationColor("always-color");

        spawnDisplays(player, edges, color);
    }

    public void showTemporary(Player player) {
        UUID playerId = player.getUniqueId();

        clearDisplays(playerId); // Clear existing before showing temp

        Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = claimManager.getPlayerClaims(playerId);
        Set<ChunkPosition> claims = claimObjects.stream()
                .flatMap(claim -> claim.getChunks().stream())
                .collect(java.util.stream.Collectors.toSet());
        World world = player.getWorld();

        Set<Edge> edges = getMergedEdges(playerId, world.getName(), claims);
        Color color = configManager.getVisualizationColor("temporary-color");

        spawnDisplays(player, edges, color);
        // Show for 10 seconds
        temporaryTimers.put(playerId, System.currentTimeMillis() + 10000);
    }

    private void spawnDisplays(Player player, Set<Edge> edges, Color color) {
        UUID playerId = player.getUniqueId();
        List<BlockDisplay> displays = new ArrayList<>();
        World world = player.getWorld();

        // Use stained glass for the border, maybe match nearest color
        // For simplicity, LIME_STAINED_GLASS or LIGHT_BLUE depending on the config
        // color
        Material borderMaterial = Material.LIME_STAINED_GLASS;
        BlockData blockData = Bukkit.createBlockData(borderMaterial);

        for (Edge edge : edges) {
            double minX = Math.min(edge.x1(), edge.x2());
            double minZ = Math.min(edge.z1(), edge.z2());
            double lengthX = Math.abs(edge.x2() - edge.x1());
            double lengthZ = Math.abs(edge.z2() - edge.z1());

            // Adjust length specifically for the border wall.
            // If it's a north/south edge, lengthX is 16, lengthZ is 0.
            if (lengthX == 0)
                lengthX = 0.1f;
            if (lengthZ == 0)
                lengthZ = 0.1f;

            // Spawn location (midpoint of the edge, y=-64 for a full height wall)
            Location loc = new Location(world, minX, -64, minZ);

            float scaleX = (float) lengthX;
            float scaleY = 384f; // Build limit from -64 to 320
            float scaleZ = (float) lengthZ;

            BlockDisplay display = world.spawn(loc, BlockDisplay.class, e -> {
                e.setPersistent(false);
                e.setVisibleByDefault(false);
                e.setBlock(blockData);
                // Make it glow and visible slightly
                e.setGlowing(true);
                e.setGravity(false);

                // Transformation for scaling
                Transformation transform = new Transformation(
                        new Vector3f(),
                        new AxisAngle4f(),
                        new Vector3f(scaleX, scaleY, scaleZ),
                        new AxisAngle4f());
                e.setTransformation(transform);
            });

            player.showEntity(plugin, display);
            displays.add(display);
        }

        activeDisplays.put(playerId, displays);
    }

    public void clearDisplays(UUID playerId) {
        List<BlockDisplay> displays = activeDisplays.remove(playerId);
        if (displays != null) {
            for (BlockDisplay display : displays) {
                if (display.isValid()) {
                    display.remove();
                }
            }
        }
    }

    public void clearSelectionDisplays(UUID playerId) {
        List<BlockDisplay> displays = selectionDisplays.remove(playerId);
        if (displays != null) {
            for (BlockDisplay display : displays) {
                if (display.isValid()) {
                    display.remove();
                }
            }
        }
    }

    public void visualizeSelection(Player player, org.ayosynk.landClaimPlugin.models.ChunkSelection selection) {
        UUID playerId = player.getUniqueId();
        clearSelectionDisplays(playerId);

        Set<ChunkPosition> chunks = selection.getSelectedChunks();
        if (chunks.isEmpty() && selection.getPos1() != null)
            chunks.add(selection.getPos1());
        if (chunks.isEmpty() && selection.getPos2() != null)
            chunks.add(selection.getPos2());
        if (chunks.isEmpty())
            return;

        World world = player.getWorld();
        Set<Edge> edges = getMergedEdges(playerId, world.getName(), chunks);

        List<BlockDisplay> displays = new ArrayList<>();
        Material material = Material.LIME_STAINED_GLASS;
        if (selection.isComplete())
            material = Material.CYAN_STAINED_GLASS;

        BlockData blockData = Bukkit.createBlockData(material);

        for (Edge edge : edges) {
            double startX = edge.x1() * 16 + (edge.z1() == edge.z2() && edge.x1() < edge.x2() ? 16 : 0);
            double startZ = edge.z1() * 16 + (edge.x1() == edge.x2() && edge.z1() < edge.z2() ? 16 : 0);
            double endX = edge.x2() * 16 + (edge.z1() == edge.z2() && edge.x1() > edge.x2() ? 16 : 0);
            double endZ = edge.z2() * 16 + (edge.x1() == edge.x2() && edge.z1() > edge.z2() ? 16 : 0);

            double minX = Math.min(startX, endX);
            double minZ = Math.min(startZ, endZ);
            double maxX = Math.max(startX, endX);
            double maxZ = Math.max(startZ, endZ);

            double lengthX = maxX - minX;
            double lengthZ = maxZ - minZ;

            if (lengthX == 0)
                lengthX = 0.1f;
            if (lengthZ == 0)
                lengthZ = 0.1f;

            Location loc = new Location(world, minX, -64, minZ);

            float scaleX = (float) lengthX;
            float scaleY = 384f;
            float scaleZ = (float) lengthZ;

            BlockDisplay display = world.spawn(loc, BlockDisplay.class, e -> {
                e.setPersistent(false);
                e.setVisibleByDefault(false);
                e.setBlock(blockData);
                e.setGlowing(true);
                e.setGravity(false);

                Transformation transform = new Transformation(
                        new Vector3f(),
                        new AxisAngle4f(),
                        new Vector3f(scaleX, scaleY, scaleZ),
                        new AxisAngle4f());
                e.setTransformation(transform);
            });

            player.showEntity(plugin, display);
            displays.add(display);
        }

        selectionDisplays.put(playerId, displays);
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
        // Force redraw by clearing displays
        clearDisplays(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            if (visualizationModes.containsKey(playerId)) {
                showPlayerClaims(player, visualizationModes.get(playerId));
            } else if (temporaryTimers.containsKey(playerId)) {
                showTemporary(player);
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
            if (player != null && player.isOnline()) {
                showPlayerClaims(player, mode);
            }
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

        if (visualizationModes.containsKey(player.getUniqueId())) {
            showPlayerClaims(player, visualizationModes.get(player.getUniqueId()));
        }
    }

    public void handlePlayerQuit(UUID playerId) {
        savePlayerState(playerId);
        visualizationModes.remove(playerId);
        temporaryTimers.remove(playerId);
        clearDisplays(playerId);
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

        // Also cleanup displays on disable
        for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
            clearDisplays(playerId);
        }
    }
}
