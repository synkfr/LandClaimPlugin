package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Edge;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;

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

    private final Map<UUID, Boolean> visualizationActive = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporaryTimers = new ConcurrentHashMap<>();

    private final Map<UUID, List<BlockDisplay>> activeDisplays = new ConcurrentHashMap<>();

    // Hex → stained glass mapping for Display Entity mode
    private static final Map<String, Material> HEX_TO_GLASS = Map.ofEntries(
            Map.entry("#1D1D21", Material.BLACK_STAINED_GLASS),
            Map.entry("#3C44AA", Material.BLUE_STAINED_GLASS),
            Map.entry("#835432", Material.BROWN_STAINED_GLASS),
            Map.entry("#169C9C", Material.CYAN_STAINED_GLASS),
            Map.entry("#474F52", Material.GRAY_STAINED_GLASS),
            Map.entry("#5E7C16", Material.GREEN_STAINED_GLASS),
            Map.entry("#3AB3DA", Material.LIGHT_BLUE_STAINED_GLASS),
            Map.entry("#80C71F", Material.LIME_STAINED_GLASS),
            Map.entry("#9D9D97", Material.LIGHT_GRAY_STAINED_GLASS),
            Map.entry("#C74EBD", Material.MAGENTA_STAINED_GLASS),
            Map.entry("#F9801D", Material.ORANGE_STAINED_GLASS),
            Map.entry("#F38BAA", Material.PINK_STAINED_GLASS),
            Map.entry("#8932B8", Material.PURPLE_STAINED_GLASS),
            Map.entry("#B02E26", Material.RED_STAINED_GLASS),
            Map.entry("#F9FFFE", Material.WHITE_STAINED_GLASS),
            Map.entry("#FED83D", Material.YELLOW_STAINED_GLASS));

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
                long now = System.currentTimeMillis();

                // Clean up expired temporary timers
                for (Iterator<Map.Entry<UUID, Long>> it = temporaryTimers.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<UUID, Long> entry = it.next();
                    if (now > entry.getValue()) {
                        it.remove();
                        if (!visualizationActive.containsKey(entry.getKey())) {
                            clearDisplays(entry.getKey());
                        } else {
                            redrawDisplays(Bukkit.getPlayer(entry.getKey()));
                        }
                    }
                }

                // Respawn particles for players using PARTICLE mode
                for (Map.Entry<UUID, Boolean> entry : visualizationActive.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null || !player.isOnline())
                        continue;

                    ClaimProfile profile = claimManager.getProfile(entry.getKey());
                    if (profile == null)
                        continue;

                    if ("PARTICLE".equals(profile.getVisualizationMode())) {
                        spawnParticles(player, profile);
                    }
                }

                // Also respawn particles for temporary timers
                for (UUID playerId : temporaryTimers.keySet()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline())
                        continue;

                    ClaimProfile profile = claimManager.getProfile(playerId);
                    if (profile != null && "PARTICLE".equals(profile.getVisualizationMode())) {
                        spawnParticles(player, profile);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean toggleVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (visualizationActive.containsKey(playerId)) {
            visualizationActive.remove(playerId);
            clearDisplays(playerId);
            return false;
        } else {
            visualizationActive.put(playerId, true);
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

        // Collect chunks from player's own profile
        Set<ChunkPosition> claims = new HashSet<>();
        ClaimProfile ownProfile = claimManager.getProfile(playerId);
        if (ownProfile != null) {
            claims.addAll(ownProfile.getOwnedChunks());
        }

        // Also include the claim the player is currently standing in
        ChunkPosition currentChunk = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile currentProfile = claimManager.getProfileAt(currentChunk);
        if (currentProfile != null) {
            claims.addAll(currentProfile.getOwnedChunks());
        }

        if (claims.isEmpty())
            return;

        Set<Edge> edges = getMergedEdges(playerId, player.getWorld().getName(), claims);
        if (edges.isEmpty())
            return;

        // Determine visualization mode from profile
        String visMode = ownProfile != null ? ownProfile.getVisualizationMode() : "DISPLAY_ENTITY";
        if (visMode == null)
            visMode = "DISPLAY_ENTITY";

        if ("PARTICLE".equals(visMode)) {
            // Particles are spawned by the repeating task; just ensure edges are cached
            return;
        }

        // Display Entity mode
        Material material = resolveMaterial(ownProfile, playerId);
        spawnDisplays(player, edges, material);
    }

    /**
     * Resolve the stained glass Material from the profile's claim color.
     */
    private Material resolveMaterial(ClaimProfile profile, UUID playerId) {
        // Temporary mode uses orange
        if (temporaryTimers.containsKey(playerId)) {
            return Material.ORANGE_STAINED_GLASS;
        }

        if (profile != null && profile.getClaimColor() != null) {
            Material mapped = HEX_TO_GLASS.get(profile.getClaimColor().toUpperCase());
            if (mapped != null)
                return mapped;

            // For custom hex colors, find the closest stained glass
            return findClosestGlass(profile.getClaimColor());
        }

        return Material.LIME_STAINED_GLASS; // default
    }

    /**
     * Find the closest stained glass color to a given hex string.
     */
    private Material findClosestGlass(String hex) {
        Color target = hexToColor(hex);
        if (target == null)
            return Material.LIME_STAINED_GLASS;

        double bestDistance = Double.MAX_VALUE;
        Material bestMaterial = Material.LIME_STAINED_GLASS;

        for (Map.Entry<String, Material> entry : HEX_TO_GLASS.entrySet()) {
            Color c = hexToColor(entry.getKey());
            if (c == null)
                continue;
            double dist = colorDistance(target, c);
            if (dist < bestDistance) {
                bestDistance = dist;
                bestMaterial = entry.getValue();
            }
        }
        return bestMaterial;
    }

    private static double colorDistance(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return dr * dr + dg * dg + db * db;
    }

    /**
     * Spawn Particle.DUST along claim edges at the player's Y level.
     */
    private void spawnParticles(Player player, ClaimProfile profile) {
        UUID playerId = player.getUniqueId();

        Set<ChunkPosition> claims = new HashSet<>();
        if (profile != null) {
            claims.addAll(profile.getOwnedChunks());
        }
        ChunkPosition currentChunk = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile currentProfile = claimManager.getProfileAt(currentChunk);
        if (currentProfile != null) {
            claims.addAll(currentProfile.getOwnedChunks());
        }
        if (claims.isEmpty())
            return;

        Set<Edge> edges = getMergedEdges(playerId, player.getWorld().getName(), claims);
        if (edges.isEmpty())
            return;

        Color color = Color.LIME; // default
        if (temporaryTimers.containsKey(playerId)) {
            color = Color.ORANGE;
        } else if (profile != null && profile.getClaimColor() != null) {
            Color parsed = hexToColor(profile.getClaimColor());
            if (parsed != null)
                color = parsed;
        }

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        World world = player.getWorld();
        double y = player.getLocation().getY() + 0.5;

        // Only render particles near the player (within 64 blocks)
        double px = player.getLocation().getX();
        double pz = player.getLocation().getZ();
        double maxDistSq = 64 * 64;

        for (Edge edge : edges) {
            double x1 = edge.x1();
            double z1 = edge.z1();
            double x2 = edge.x2();
            double z2 = edge.z2();

            double len = Math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
            if (len < 0.1)
                continue;

            int steps = (int) Math.ceil(len / 0.5); // particle every 0.5 blocks
            double dx = (x2 - x1) / steps;
            double dz = (z2 - z1) / steps;

            for (int i = 0; i <= steps; i++) {
                double x = x1 + dx * i;
                double z = z1 + dz * i;

                // Distance check
                double distSq = (x - px) * (x - px) + (z - pz) * (z - pz);
                if (distSq > maxDistSq)
                    continue;

                world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, dustOptions);
            }
        }
    }

    /**
     * Parse a hex color string to a Bukkit Color.
     */
    static Color hexToColor(String hex) {
        if (hex == null || hex.length() < 7)
            return null;
        try {
            String clean = hex.startsWith("#") ? hex.substring(1) : hex;
            int r = Integer.parseInt(clean.substring(0, 2), 16);
            int g = Integer.parseInt(clean.substring(2, 4), 16);
            int b = Integer.parseInt(clean.substring(4, 6), 16);
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void spawnDisplays(Player player, Set<Edge> edges, Material material) {
        UUID playerId = player.getUniqueId();
        List<BlockDisplay> displays = new ArrayList<>();
        World world = player.getWorld();

        Bukkit.getScheduler().runTask(plugin, () -> {
            final org.bukkit.block.data.BlockData blockData = Bukkit.createBlockData(material);
            final org.joml.AxisAngle4f emptyRotation = new org.joml.AxisAngle4f();

            for (Edge edge : edges) {
                double minX = Math.min(edge.x1(), edge.x2());
                double minZ = Math.min(edge.z1(), edge.z2());
                double lengthX = Math.abs(edge.x2() - edge.x1());
                double lengthZ = Math.abs(edge.z2() - edge.z1());

                float scaleX = (float) (lengthX == 0 ? 0.05f : lengthX);
                float scaleZ = (float) (lengthZ == 0 ? 0.05f : lengthZ);
                float scaleY = 384f;

                double playerY = player.getLocation().getY();
                Location loc = new Location(world, minX, playerY, minZ);

                BlockDisplay display = world.spawn(loc, BlockDisplay.class, e -> {
                    e.setPersistent(false);
                    e.setVisibleByDefault(false);
                    e.setBlock(blockData);
                    e.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
                    e.setGravity(false);

                    float transX = lengthX == 0 ? -0.025f : 0f;
                    float transY = (float) (-64 - playerY);
                    float transZ = lengthZ == 0 ? -0.025f : 0f;

                    Transformation transform = new Transformation(
                            new Vector3f(transX, transY, transZ),
                            emptyRotation,
                            new Vector3f(scaleX, scaleY, scaleZ),
                            emptyRotation);
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

    public void handlePlayerJoin(Player player) {
        String defaultMode = configManager.getDefaultVisualizationMode();
        if ("ALWAYS".equalsIgnoreCase(defaultMode)) {
            visualizationActive.put(player.getUniqueId(), true);
            redrawDisplays(player);
        }
    }

    public void handlePlayerQuit(UUID playerId) {
        visualizationActive.remove(playerId);
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
