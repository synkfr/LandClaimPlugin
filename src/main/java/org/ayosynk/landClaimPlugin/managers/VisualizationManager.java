package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
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
import org.bukkit.util.Transformation;

import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VisualizationManager {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    private static final int DISPLAY_RANGE = 64;
    private static final int Y_BAND_STEP = 4;
    private static final int[] HORIZONTAL_BAND_OFFSETS = { -4, 0, 4, 8, 12 };
    private static final float RAIL_THICKNESS = 0.05f;
    private static final float PILLAR_THICKNESS = 0.07f;
    private static final int MAX_CACHE_ENTRIES = 100;
    private final Map<String, Map<String, Set<Edge>>> mergedEdgesCache = new ConcurrentHashMap<>();
    private final AtomicInteger cacheSize = new AtomicInteger(0);

    private final Map<UUID, Boolean> visualizationActive = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporaryTimers = new ConcurrentHashMap<>();

    private final Map<UUID, List<BlockDisplay>> activeDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicLong> displayGenerations = new ConcurrentHashMap<>();
    private final Map<UUID, ChunkPosition> lastVisualizedChunk = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastVisualizedYBand = new ConcurrentHashMap<>();

    // Hex → concrete mapping for opaque Display Entity wireframe borders
    private static final Map<String, Material> HEX_TO_CONCRETE = Map.ofEntries(
            Map.entry("#1D1D21", Material.BLACK_CONCRETE),
            Map.entry("#3C44AA", Material.BLUE_CONCRETE),
            Map.entry("#835432", Material.BROWN_CONCRETE),
            Map.entry("#169C9C", Material.CYAN_CONCRETE),
            Map.entry("#474F52", Material.GRAY_CONCRETE),
            Map.entry("#5E7C16", Material.GREEN_CONCRETE),
            Map.entry("#3AB3DA", Material.LIGHT_BLUE_CONCRETE),
            Map.entry("#80C71F", Material.LIME_CONCRETE),
            Map.entry("#9D9D97", Material.LIGHT_GRAY_CONCRETE),
            Map.entry("#C74EBD", Material.MAGENTA_CONCRETE),
            Map.entry("#F9801D", Material.ORANGE_CONCRETE),
            Map.entry("#F38BAA", Material.PINK_CONCRETE),
            Map.entry("#8932B8", Material.PURPLE_CONCRETE),
            Map.entry("#B02E26", Material.RED_CONCRETE),
            Map.entry("#F9FFFE", Material.WHITE_CONCRETE),
            Map.entry("#FED83D", Material.YELLOW_CONCRETE));

    public VisualizationManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
        startVisualizationTask();
    }

    private void startVisualizationTask() {
        // The "tick" body splits into two parts:
        //   1) Global bookkeeping (cleanup of expired temporary timers) — safe anywhere
        //   2) Per-player particle respawn — must run on each player's region thread on Folia
        // runPlayerTaskTimer dispatches per-player work to the correct thread on Folia and
        // runs the global bookkeeping on the global region (or main thread on Paper).
        FoliaScheduler.runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            // Clean up expired temporary timers (thread-safe with ConcurrentHashMap)
            for (Iterator<Map.Entry<UUID, Long>> it = temporaryTimers.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, Long> entry = it.next();
                if (now > entry.getValue()) {
                    UUID pid = entry.getKey();
                    it.remove();
                    if (!visualizationActive.containsKey(pid)) {
                        clearDisplays(pid);
                    } else {
                        Player p = Bukkit.getPlayer(pid);
                        if (p != null) {
                            redrawDisplays(p);
                        }
                    }
                }
            }
        }, 40L, 40L);

        FoliaScheduler.runPlayerTaskTimer(plugin, player -> {
            if (player == null || !player.isOnline()) return;
            UUID playerId = player.getUniqueId();
            if (!isVisualizing(playerId)) return;

            ClaimProfile profile = claimManager.getActiveProfile(player);
            ChunkPosition currentChunk = new ChunkPosition(player.getLocation().getChunk());
            ClaimProfile currentProfile = claimManager.getProfileAt(currentChunk);
            if (profile == null && currentProfile == null) {
                clearDisplays(playerId);
                return;
            }
            String visMode = resolveVisMode(player, profile);
            if ("OFF".equalsIgnoreCase(visMode)) {
                clearDisplays(playerId);
                return;
            }
            if ("PARTICLE".equals(visMode)) {
                spawnParticles(player, profile, currentProfile);
            } else {
                ChunkPosition prevChunk = lastVisualizedChunk.get(playerId);
                int currentYBand = yBandFor(player.getLocation().getY());
                Integer prevYBand = lastVisualizedYBand.get(playerId);
                if (!currentChunk.equals(prevChunk) || prevYBand == null || currentYBand != prevYBand) {
                    redrawDisplays(player);
                }
            }
        }, 40L, 40L);
    }

    public boolean toggleVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (visualizationActive.containsKey(playerId)) {
            disableVisualization(player);
            return false;
        } else {
            visualizationActive.put(playerId, true);
            redrawDisplays(player);
            return true;
        }
    }

    public void disableVisualization(Player player) {
        if (player == null) return;
        UUID playerId = player.getUniqueId();
        visualizationActive.remove(playerId);
        temporaryTimers.remove(playerId);
        clearDisplays(playerId);
    }

    public void showTemporary(Player player) {
        UUID playerId = player.getUniqueId();
        temporaryTimers.put(playerId, System.currentTimeMillis() + 10000);
        redrawDisplays(player);
    }

    /**
     * Redraw visualization for a specific player (public API).
     */
    public void redrawDisplaysForPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        ClaimProfile ownProfile = claimManager.getActiveProfile(player);
        if ("OFF".equalsIgnoreCase(resolveVisMode(player, ownProfile))) {
            disableVisualization(player);
            return;
        }
        // Ensure visualization is active and redraw
        visualizationActive.put(player.getUniqueId(), true);
        redrawDisplays(player);
    }

    public void invalidateCache(UUID profileOrPlayerId) {
        if (profileOrPlayerId == null) return;
        String idToken = profileOrPlayerId.toString();
        for (Iterator<String> it = mergedEdgesCache.keySet().iterator(); it.hasNext();) {
            if (it.next().contains(idToken)) {
                it.remove();
                cacheSize.decrementAndGet();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            if (!isVisualizing(playerId) && !activeDisplays.containsKey(playerId)) {
                continue;
            }
            if (playerId.equals(profileOrPlayerId)) {
                redrawDisplays(player);
                continue;
            }
            ClaimProfile activeProfile = claimManager.getActiveProfile(player);
            if (activeProfile != null && profileOrPlayerId.equals(activeProfile.getProfileId())) {
                redrawDisplays(player);
            }
        }
    }

    private boolean isVisualizing(UUID playerId) {
        return visualizationActive.containsKey(playerId) || temporaryTimers.containsKey(playerId);
    }

    private String resolveVisMode(Player player, ClaimProfile profile) {
        if (org.ayosynk.landClaimPlugin.util.GeyserHelper.isBedrockPlayer(player)) {
            return "PARTICLE";
        }
        String visMode = profile != null ? profile.getVisualizationMode() : "DISPLAY_ENTITY";
        return visMode != null ? visMode : "DISPLAY_ENTITY";
    }

    private void redrawDisplays(Player player) {
        if (player == null || !player.isOnline())
            return;
        UUID playerId = player.getUniqueId();

        long gen = nextGeneration(playerId);
        removeTrackedDisplays(playerId);

        ClaimProfile ownProfile = claimManager.getActiveProfile(player);
        String visMode = resolveVisMode(player, ownProfile);
        if ("OFF".equalsIgnoreCase(visMode)) {
            lastVisualizedChunk.remove(playerId);
            lastVisualizedYBand.remove(playerId);
            return;
        }

        Runnable work = () -> {
            if (!player.isOnline() || isGenerationStale(playerId, gen) || !isVisualizing(playerId)) {
                return;
            }

            ChunkPosition currentChunk = new ChunkPosition(player.getLocation().getChunk());
            ClaimProfile currentProfile = claimManager.getProfileAt(currentChunk);

            Set<ChunkPosition> claims = new HashSet<>();
            if (ownProfile != null) {
                claims.addAll(ownProfile.getOwnedChunks());
            }
            if (currentProfile != null) {
                claims.addAll(currentProfile.getOwnedChunks());
            }
            if (claims.isEmpty()) {
                lastVisualizedChunk.remove(playerId);
                lastVisualizedYBand.remove(playerId);
                return;
            }

            Set<Edge> edges = getMergedEdges(ownProfile, currentProfile, player.getWorld().getName(), claims);
            if (edges.isEmpty()) {
                lastVisualizedChunk.remove(playerId);
                lastVisualizedYBand.remove(playerId);
                return;
            }

            lastVisualizedChunk.put(playerId, currentChunk);
            lastVisualizedYBand.put(playerId, yBandFor(player.getLocation().getY()));
            if ("PARTICLE".equals(visMode)) {
                return;
            }

            Material material = resolveMaterial(ownProfile, playerId);
            spawnDisplays(player, edges, material, gen);
        };

        if (!FoliaScheduler.isFolia() && Bukkit.isPrimaryThread()) {
            work.run();
        } else {
            FoliaScheduler.runForPlayer(plugin, player, work);
        }
    }

    /**
     * Resolve the opaque concrete Material from the profile's claim color.
     */
    private Material resolveMaterial(ClaimProfile profile, UUID playerId) {
        if (temporaryTimers.containsKey(playerId)) {
            return Material.ORANGE_CONCRETE;
        }
        return profile != null ? resolveConcreteMaterial(profile.getClaimColor()) : Material.LIME_CONCRETE;
    }

    /**
     * Resolve the exact or nearest concrete Material for a hex claim color.
     */
    static Material resolveConcreteMaterial(String hex) {
        if (hex == null) {
            return Material.LIME_CONCRETE;
        }
        Material mapped = HEX_TO_CONCRETE.get(hex.toUpperCase(Locale.ROOT));
        if (mapped != null) {
            return mapped;
        }
        Color target = hexToColor(hex);
        if (target == null) {
            return Material.LIME_CONCRETE;
        }

        double bestDistance = Double.MAX_VALUE;
        Material bestMaterial = Material.LIME_CONCRETE;
        for (Map.Entry<String, Material> entry : HEX_TO_CONCRETE.entrySet()) {
            Color c = hexToColor(entry.getKey());
            if (c == null) {
                continue;
            }
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
    private void spawnParticles(Player player, ClaimProfile profile, ClaimProfile currentProfile) {
        UUID playerId = player.getUniqueId();

        Set<ChunkPosition> claims = new HashSet<>();
        if (profile != null) {
            claims.addAll(profile.getOwnedChunks());
        }
        if (currentProfile != null) {
            claims.addAll(currentProfile.getOwnedChunks());
        }
        if (claims.isEmpty())
            return;

        Set<Edge> edges = getMergedEdges(profile, currentProfile, player.getWorld().getName(), claims);
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

    /**
     * Spawn an opaque concrete wireframe of vertical corner pillars and horizontal rails
     * so translucent terrain (water, ice, glass, portals) behind claim borders never gets
     * depth-culled by full-wall translucent BlockDisplays.
     */
    private void spawnDisplays(Player player, Set<Edge> edges, Material material, long gen) {
        UUID playerId = player.getUniqueId();
        World world = player.getWorld();
        double px = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double pz = player.getLocation().getZ();

        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();
        float pillarScaleY = (float) (maxHeight - minHeight);
        float pillarTransY = (float) (minHeight - playerY);
        int baseY = yBandFor(playerY) * Y_BAND_STEP;

        org.bukkit.block.data.BlockData blockData = Bukkit.createBlockData(material);
        org.joml.AxisAngle4f emptyRotation = new org.joml.AxisAngle4f();

        List<BlockDisplay> displays = Collections.synchronizedList(new ArrayList<>());
        activeDisplays.put(playerId, displays);
        Set<Long> spawnedCorners = new HashSet<>();

        for (Edge edge : edges) {
            if (isGenerationStale(playerId, gen) || !player.isOnline()) {
                break;
            }
            if (!isEdgeNearPlayer(edge, px, pz, DISPLAY_RANGE)) {
                continue;
            }

            // Deduplicate shared chunk-corner posts across adjacent edges
            int[][] corners = { { edge.x1(), edge.z1() }, { edge.x2(), edge.z2() } };
            for (int[] corner : corners) {
                int vx = corner[0];
                int vz = corner[1];
                if (!spawnedCorners.add(packCorner(vx, vz))) {
                    continue;
                }
                int cChunkX = vx >> 4;
                int cChunkZ = vz >> 4;
                if (!world.isChunkLoaded(cChunkX, cChunkZ)) {
                    continue;
                }
                Location pillarLoc = new Location(world, vx, playerY, vz);
                float halfPillar = -PILLAR_THICKNESS / 2f;
                dispatchAtLocation(player, world, pillarLoc, cChunkX, cChunkZ, gen,
                        () -> spawnBlockBeam(player, world, displays, pillarLoc, blockData, emptyRotation,
                                halfPillar, pillarTransY, halfPillar,
                                PILLAR_THICKNESS, pillarScaleY, PILLAR_THICKNESS, gen));
            }

            double minX = Math.min(edge.x1(), edge.x2());
            double minZ = Math.min(edge.z1(), edge.z2());
            int chunkX = ((int) Math.floor(minX)) >> 4;
            int chunkZ = ((int) Math.floor(minZ)) >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                continue;
            }

            double lengthX = Math.abs(edge.x2() - edge.x1());
            double lengthZ = Math.abs(edge.z2() - edge.z1());
            float scaleX = (float) (lengthX == 0 ? RAIL_THICKNESS : lengthX);
            float scaleZ = (float) (lengthZ == 0 ? RAIL_THICKNESS : lengthZ);
            float transX = lengthX == 0 ? -RAIL_THICKNESS / 2f : 0f;
            float transZ = lengthZ == 0 ? -RAIL_THICKNESS / 2f : 0f;
            float halfRail = -RAIL_THICKNESS / 2f;

            Location edgeLoc = new Location(world, minX, playerY, minZ);
            dispatchAtLocation(player, world, edgeLoc, chunkX, chunkZ, gen, () -> {
                for (int offset : HORIZONTAL_BAND_OFFSETS) {
                    int beamY = baseY + offset;
                    if (beamY < minHeight || beamY >= maxHeight) {
                        continue;
                    }
                    Location beamLoc = new Location(world, minX, beamY, minZ);
                    spawnBlockBeam(player, world, displays, beamLoc, blockData, emptyRotation,
                            transX, halfRail, transZ,
                            scaleX, RAIL_THICKNESS, scaleZ, gen);
                }
            });
        }
    }

    private void dispatchAtLocation(Player player, World world, Location loc, int chunkX, int chunkZ, long gen,
            Runnable spawnAction) {
        UUID playerId = player.getUniqueId();
        Runnable guarded = () -> {
            if (isGenerationStale(playerId, gen) || !player.isOnline() || !isVisualizing(playerId)) {
                return;
            }
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                return;
            }
            spawnAction.run();
        };
        if (!FoliaScheduler.isFolia() || Bukkit.isOwnedByCurrentRegion(loc)) {
            guarded.run();
        } else {
            FoliaScheduler.runAtLocation(plugin, loc, guarded);
        }
    }

    private void spawnBlockBeam(Player player, World world, List<BlockDisplay> displays, Location loc,
            org.bukkit.block.data.BlockData blockData, org.joml.AxisAngle4f emptyRotation,
            float transX, float transY, float transZ,
            float scaleX, float scaleY, float scaleZ, long gen) {
        UUID playerId = player.getUniqueId();
        BlockDisplay display = world.spawn(loc, BlockDisplay.class, e -> {
            e.setPersistent(false);
            e.setVisibleByDefault(false);
            e.setBlock(blockData);
            e.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
            e.setGravity(false);
            e.setShadowRadius(0f);
            e.setShadowStrength(0f);
            e.setViewRange(1.5f);
            e.setTransformation(new Transformation(
                    new Vector3f(transX, transY, transZ),
                    emptyRotation,
                    new Vector3f(scaleX, scaleY, scaleZ),
                    emptyRotation));
        });

        boolean keep;
        synchronized (displays) {
            keep = !isGenerationStale(playerId, gen) && player.isOnline() && isVisualizing(playerId);
            if (keep) {
                displays.add(display);
            }
        }
        if (!keep) {
            display.remove();
        } else if (FoliaScheduler.isFolia()) {
            FoliaScheduler.runForPlayer(plugin, player, () -> {
                if (!isGenerationStale(playerId, gen) && player.isOnline() && display.isValid()) {
                    player.showEntity(plugin, display);
                }
            });
        } else {
            player.showEntity(plugin, display);
        }
    }

    static long packCorner(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    static int yBandFor(double y) {
        return Math.floorDiv((int) Math.floor(y), Y_BAND_STEP);
    }

    private static boolean isEdgeNearPlayer(Edge edge, double px, double pz, int maxDist) {
        double cx = Math.max(edge.x1(), Math.min(px, edge.x2()));
        double cz = Math.max(edge.z1(), Math.min(pz, edge.z2()));
        double dx = px - cx;
        double dz = pz - cz;
        return (dx * dx + dz * dz) <= (double) maxDist * maxDist;
    }

    private long nextGeneration(UUID playerId) {
        return displayGenerations.computeIfAbsent(playerId, k -> new AtomicLong()).incrementAndGet();
    }

    private boolean isGenerationStale(UUID playerId, long gen) {
        AtomicLong current = displayGenerations.get(playerId);
        return current == null || current.get() != gen;
    }

    private void clearDisplays(UUID playerId) {
        nextGeneration(playerId);
        lastVisualizedChunk.remove(playerId);
        lastVisualizedYBand.remove(playerId);
        removeTrackedDisplays(playerId);
    }

    private void removeTrackedDisplays(UUID playerId) {
        List<BlockDisplay> displays = activeDisplays.remove(playerId);
        if (displays == null || displays.isEmpty()) {
            return;
        }
        List<BlockDisplay> snapshot;
        synchronized (displays) {
            snapshot = new ArrayList<>(displays);
            displays.clear();
        }
        if (snapshot.isEmpty()) {
            return;
        }

        if (!plugin.isEnabled()) {
            for (BlockDisplay display : snapshot) {
                if (!FoliaScheduler.isFolia() || Bukkit.isOwnedByCurrentRegion(display)) {
                    removeDisplaySafely(display);
                }
            }
            return;
        }

        if (!FoliaScheduler.isFolia()) {
            if (Bukkit.isPrimaryThread()) {
                for (BlockDisplay display : snapshot) {
                    removeDisplaySafely(display);
                }
            } else {
                FoliaScheduler.runTask(plugin, () -> {
                    for (BlockDisplay display : snapshot) {
                        removeDisplaySafely(display);
                    }
                });
            }
        } else {
            for (BlockDisplay display : snapshot) {
                FoliaScheduler.runForEntity(plugin, display, () -> removeDisplaySafely(display));
            }
        }
    }

    private static void removeDisplaySafely(BlockDisplay display) {
        try {
            if (!display.isDead()) {
                display.remove();
            }
        } catch (Throwable ignored) {
        }
    }

    // --- Caching and Edge Merging ---

    private Set<Edge> getMergedEdges(ClaimProfile ownProfile, ClaimProfile currentProfile, String worldName,
            Set<ChunkPosition> claims) {
        String cacheKey = (ownProfile != null ? ownProfile.getProfileId().toString() : "none")
                + ":" + (currentProfile != null ? currentProfile.getProfileId().toString() : "none");

        Map<String, Set<Edge>> worldCache = mergedEdgesCache.get(cacheKey);
        if (worldCache != null && worldCache.containsKey(worldName)) {
            return worldCache.get(worldName);
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

        cacheEdges(cacheKey, worldName, uniqueEdges);
        return uniqueEdges;
    }

    private void cacheEdges(String cacheKey, String worldName, Set<Edge> edges) {
        if (!mergedEdgesCache.containsKey(cacheKey) && cacheSize.get() >= MAX_CACHE_ENTRIES) {
            evictOldestCacheEntry();
        }

        boolean isNewEntry = !mergedEdgesCache.containsKey(cacheKey);
        mergedEdgesCache.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>()).put(worldName, edges);

        if (isNewEntry) {
            cacheSize.incrementAndGet();
        }
    }

    private void evictOldestCacheEntry() {
        Iterator<String> iterator = mergedEdgesCache.keySet().iterator();
        if (iterator.hasNext()) {
            String toRemove = iterator.next();
            if (mergedEdgesCache.remove(toRemove) != null) {
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
        displayGenerations.remove(playerId);
        String idToken = playerId.toString();
        for (Iterator<String> it = mergedEdgesCache.keySet().iterator(); it.hasNext();) {
            if (it.next().contains(idToken)) {
                it.remove();
                cacheSize.decrementAndGet();
            }
        }
    }

    public void cleanupLocalDisplays() {
        for (UUID playerId : new ArrayList<>(activeDisplays.keySet())) {
            clearDisplays(playerId);
        }
    }
}
