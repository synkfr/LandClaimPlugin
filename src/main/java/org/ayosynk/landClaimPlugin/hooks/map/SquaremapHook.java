package org.ayosynk.landClaimPlugin.hooks.map;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import xyz.jpenilla.squaremap.api.Key;

import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
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

    public SquaremapHook(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.layerKey = Key.of("landclaims");

        if (Bukkit.getPluginManager().getPlugin("squaremap") != null) {
            active = true;
            plugin.getLogger().info("Squaremap integration enabled.");
            update();
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

            for (UUID playerId : getAllPlayerIds()) {
                String playerName = Bukkit.getOfflinePlayer(playerId).getName();
                if (playerName == null)
                    playerName = "Unknown";

                org.ayosynk.landClaimPlugin.models.ClaimProfile profile = claimManager.getProfile(playerId);
                if (profile == null)
                    continue;
                Set<ChunkPosition> claims = profile.getOwnedChunks().stream()
                        .filter(pos -> pos.world().equals(mapWorld.identifier().asString()))
                        .collect(java.util.stream.Collectors.toSet());

                if (claims.isEmpty())
                    continue;

                Random rnd = new Random(playerId.getMostSignificantBits());
                int r = rnd.nextInt(200) + 55;
                int g = rnd.nextInt(200) + 55;
                int b = rnd.nextInt(200) + 55;
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
                }
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
}
