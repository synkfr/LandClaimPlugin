package org.ayosynk.landClaimPlugin.hooks.map;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import xyz.jpenilla.squaremap.api.Key;

import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.awt.Color;
import java.util.*;

public class SquaremapHook {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final Key layerKey;
    private boolean active = false;
    private final EnableListener enableListener;

    public SquaremapHook(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.layerKey = Key.of("landclaims");
        this.enableListener = new EnableListener();

        // squaremap may load *after* LandClaimPlugin. Hook the PluginEnableEvent
        // and try to activate immediately if it's already loaded.
        Bukkit.getPluginManager().registerEvents(enableListener, plugin);
        tryActivate();
    }

    private void tryActivate() {
        if (active)
            return;
        if (Bukkit.getPluginManager().getPlugin("squaremap") == null)
            return;
        Squaremap api;
        try {
            api = SquaremapProvider.get();
        } catch (Throwable t) {
            // API not ready yet — squaremap loaded but its provider hasn't registered.
            // We'll retry on the next tick.
            FoliaScheduler.runTaskLater(plugin, this::tryActivate, 1L);
            return;
        }
        if (api == null) {
            FoliaScheduler.runTaskLater(plugin, this::tryActivate, 1L);
            return;
        }

        active = true;
        HandlerList.unregisterAll(enableListener);
        plugin.getLogger().info("Squaremap integration enabled.");
        update();
    }

    private final class EnableListener implements Listener {
        @EventHandler
        public void onPluginEnable(PluginEnableEvent event) {
            if ("squaremap".equalsIgnoreCase(event.getPlugin().getName())) {
                // squaremap is now enabled — give it a tick to register its API provider
                FoliaScheduler.runTaskLater(plugin, SquaremapHook.this::tryActivate, 1L);
            }
        }
    }

    public void update() {
        if (!active)
            return;

        double fillOpacity = plugin.getConfigManager().getPluginConfig().squaremap.fillOpacity;
        double borderOpacity = plugin.getConfigManager().getPluginConfig().squaremap.borderOpacity;

        SquaremapProvider.get().mapWorlds().forEach(mapWorld -> {
            SimpleLayerProvider provider;
            if (mapWorld.layerRegistry().hasEntry(layerKey)) {
                provider = (SimpleLayerProvider) mapWorld.layerRegistry().get(layerKey);
                provider.clearMarkers();
            } else {
                provider = SimpleLayerProvider.builder("Land Claims").showControls(true).defaultHidden(false).build();
                mapWorld.layerRegistry().register(layerKey, provider);
            }

            String mapWorldId = mapWorld.identifier().asString();
            int[] worldMarkerCount = {0};

            for (UUID playerId : getAllPlayerIds()) {
                org.ayosynk.landClaimPlugin.models.ClaimProfile profile = claimManager.getProfile(playerId);
                if (profile == null)
                    continue;

                String playerName = profile.getDisplayOwnerName();
                // SquareMap's identifier is the namespaced key (e.g. "minecraft:overworld") while
                // ChunkPosition stores the plain Bukkit *level* name (e.g. "world"). Compare both
                // forms: the chunk world as-is, the chunk world as a key, and the identifier
                // with its namespace prefix stripped.
                Set<ChunkPosition> claims = profile.getOwnedChunks().stream()
                        .filter(pos -> worldsMatch(pos.world(), mapWorldId))
                        .collect(java.util.stream.Collectors.toSet());

                if (claims.isEmpty())
                    continue;

                int r, g, b;
                if (profile.getClaimColor() != null && profile.getClaimColor().length() >= 7) {
                    try {
                        String hex = profile.getClaimColor().startsWith("#") ? profile.getClaimColor().substring(1)
                                : profile.getClaimColor();
                        r = Integer.parseInt(hex.substring(0, 2), 16);
                        g = Integer.parseInt(hex.substring(2, 4), 16);
                        b = Integer.parseInt(hex.substring(4, 6), 16);
                    } catch (NumberFormatException ex) {
                        Random rnd = new Random(playerId.getMostSignificantBits());
                        r = rnd.nextInt(200) + 55;
                        g = rnd.nextInt(200) + 55;
                        b = rnd.nextInt(200) + 55;
                    }
                } else {
                    Random rnd = new Random(playerId.getMostSignificantBits());
                    r = rnd.nextInt(200) + 55;
                    g = rnd.nextInt(200) + 55;
                    b = rnd.nextInt(200) + 55;
                }
                Color pColor = new Color(r, g, b);

                List<double[][]> polygons = createPolygons(claims);
                int i = 0;
                for (double[][] polygon : polygons) {
                    if (polygon[0].length < 3)
                        continue;

                    List<Point> points = new ArrayList<>();
                    for (int j = 0; j < polygon[0].length; j++) {
                        points.add(Point.of(polygon[0][j], polygon[1][j]));
                    }

                    MarkerOptions options = MarkerOptions.builder()
                            .fillColor(pColor)
                            .fillOpacity(fillOpacity)
                            .strokeColor(pColor)
                            .strokeOpacity(borderOpacity)
                            .strokeWeight(2)
                            .hoverTooltip(playerName + "'s Claim")
                            .build();

                    xyz.jpenilla.squaremap.api.marker.Polygon marker = Marker.polygon(points);
                    marker.markerOptions(options);

                    provider.addMarker(Key.of(playerId.toString() + "_" + i), marker);
                    i++;
                    worldMarkerCount[0]++;
                }
            }

            if (worldMarkerCount[0] > 0) {
                plugin.getLogger().info("SquareMap: rendered " + worldMarkerCount[0] + " claim marker(s) in world '" + mapWorldId + "'.");
            } else {
                plugin.getLogger().fine("SquareMap: no claims matched world '" + mapWorldId + "'.");
            }
        });
    }

    private record GridPoint(int x, int z) {
    }

    private record Edge(GridPoint from, GridPoint to) {
    }

    private List<double[][]> createPolygons(Set<ChunkPosition> chunks) {
        Set<Edge> edges = new HashSet<>();
        for (ChunkPosition chunk : chunks) {
            int cx = chunk.x();
            int cz = chunk.z();

            GridPoint p00 = new GridPoint(cx, cz);
            GridPoint p10 = new GridPoint(cx + 1, cz);
            GridPoint p11 = new GridPoint(cx + 1, cz + 1);
            GridPoint p01 = new GridPoint(cx, cz + 1);

            Edge[] chunkEdges = {
                    new Edge(p00, p10),
                    new Edge(p10, p11),
                    new Edge(p11, p01),
                    new Edge(p01, p00)
            };

            for (Edge e : chunkEdges) {
                Edge opposite = new Edge(e.to(), e.from());
                if (edges.contains(opposite)) {
                    edges.remove(opposite);
                } else {
                    edges.add(e);
                }
            }
        }

        Map<GridPoint, List<Edge>> adjacency = new HashMap<>();
        for (Edge e : edges) {
            adjacency.computeIfAbsent(e.from(), k -> new ArrayList<>()).add(e);
        }

        List<double[][]> polygons = new ArrayList<>();
        while (!adjacency.isEmpty()) {
            GridPoint start = adjacency.keySet().iterator().next();
            List<Double> xPts = new ArrayList<>();
            List<Double> zPts = new ArrayList<>();
            GridPoint current = start;

            while (true) {
                xPts.add(current.x() * 16.0);
                zPts.add(current.z() * 16.0);

                List<Edge> outEdges = adjacency.get(current);
                if (outEdges == null || outEdges.isEmpty()) {
                    adjacency.remove(current);
                    break;
                }

                Edge nextEdge = outEdges.remove(0);
                if (outEdges.isEmpty()) {
                    adjacency.remove(current);
                }

                current = nextEdge.to();
                if (current.equals(start)) {
                    break;
                }
            }
            if (xPts.size() >= 3) {
                double[] xArr = new double[xPts.size()];
                double[] zArr = new double[zPts.size()];
                for (int j = 0; j < xPts.size(); j++) {
                    xArr[j] = xPts.get(j);
                    zArr[j] = zPts.get(j);
                }
                polygons.add(new double[][] { xArr, zArr });
            }
        }
        return polygons;
    }

    private Set<UUID> getAllPlayerIds() {
        Set<UUID> playerIds = new HashSet<>();
        for (var offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.hasPlayedBefore()) {
                UUID playerId = offlinePlayer.getUniqueId();
                if (claimManager.getProfile(playerId) != null) {
                    playerIds.add(playerId);
                }
            }
        }
        return playerIds;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Compare a ChunkPosition's world name with a SquareMap world identifier.
     *
     * <p>SquareMap's {@code WorldIdentifier.asString()} always returns the namespaced
     * key (e.g. {@code minecraft:overworld}), while {@code ChunkPosition.world()}
     * stores the plain Bukkit level name (e.g. {@code world} — the name set in
     * {@code server.properties}). A straight string comparison will fail, so this
     * helper tries all reasonable interpretations:</p>
     *
     * <ol>
     *   <li>Direct case-insensitive match ({@code world} == {@code world})</li>
     *   <li>Chunk world as the namespaced key ({@code world} → {@code minecraft:world} == {@code minecraft:world})</li>
     *   <li>Identifier with the namespace prefix stripped ({@code minecraft:overworld} → {@code overworld} == {@code world})</li>
     *   <li>Direct match between the chunk's namespaced key and the identifier
     *       ({@code world} → {@code minecraft:overworld} == {@code minecraft:overworld})</li>
     * </ol>
     */
    private static boolean worldsMatch(String bukkitWorld, String mapWorldId) {
        if (bukkitWorld == null || mapWorldId == null) return false;
        if (bukkitWorld.equalsIgnoreCase(mapWorldId)) return true;

        // Strip namespace prefix from the SquareMap identifier and try again.
        String simple = mapWorldId;
        int colon = simple.indexOf(':');
        if (colon >= 0) {
            simple = simple.substring(colon + 1);
        }
        if (bukkitWorld.equalsIgnoreCase(simple)) return true;

        // Resolve the Bukkit world for the chunk and compare its namespaced key
        // directly against SquareMap's identifier. This is the case the server
        // hits: level-name "world" → key "minecraft:overworld" → SquareMap id
        // "minecraft:overworld".
        org.bukkit.World chunkWorld = org.bukkit.Bukkit.getWorld(bukkitWorld);
        if (chunkWorld != null) {
            String chunkKey = chunkWorld.getKey().toString();
            if (chunkKey.equalsIgnoreCase(mapWorldId)) return true;
            // Also try the chunk's simple key (no namespace) against the map id.
            String chunkSimple = chunkWorld.getKey().getKey();
            if (chunkSimple.equalsIgnoreCase(simple)) return true;
        }
        return false;
    }
}
