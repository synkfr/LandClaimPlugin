package org.ayosynk.landClaimPlugin.hooks.map;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Fill;
import net.pl3x.map.core.markers.option.Stroke;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.markers.layer.SimpleLayer;

import java.util.*;

public class Pl3xMapHook {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private boolean active = false;

    public Pl3xMapHook(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;

        if (Bukkit.getPluginManager().getPlugin("Pl3xMap") != null) {
            active = true;
            plugin.getLogger().info("Pl3xMap integration enabled.");
            update();
        }
    }

    public void update() {
        if (!active)
            return;

        double fillOpacity = plugin.getConfigManager().getPluginConfig().pl3xmap.fillOpacity;
        double borderOpacity = plugin.getConfigManager().getPluginConfig().pl3xmap.borderOpacity;

        Pl3xMap.api().getWorldRegistry().forEach(world -> {
            String layerKey = "landclaims";
            SimpleLayer layer;
            if (world.getLayerRegistry().has(layerKey)) {
                layer = (SimpleLayer) world.getLayerRegistry().get(layerKey);
                layer.clearMarkers();
            } else {
                layer = new SimpleLayer(layerKey, () -> "Land Claims") {
                };
                world.getLayerRegistry().register(layer);
            }

            for (UUID playerId : getAllPlayerIds()) {
                String playerName = Bukkit.getOfflinePlayer(playerId).getName();
                if (playerName == null)
                    playerName = "Unknown";

                org.ayosynk.landClaimPlugin.models.ClaimProfile profile = claimManager.getProfile(playerId);
                if (profile == null)
                    continue;
                Set<ChunkPosition> claims = profile.getOwnedChunks().stream()
                        .filter(pos -> pos.world().equals(world.getName()))
                        .collect(java.util.stream.Collectors.toSet());

                if (claims.isEmpty())
                    continue;

                Random rnd = new Random(playerId.getMostSignificantBits());
                int r = rnd.nextInt(200) + 55;
                int g = rnd.nextInt(200) + 55;
                int b = rnd.nextInt(200) + 55;
                int pColorRGB = (r << 16) | (g << 8) | b;

                int argbFill = ((int) (fillOpacity * 255) << 24) | pColorRGB;
                int argbBorder = ((int) (borderOpacity * 255) << 24) | pColorRGB;

                Options options = Options.builder()
                        .fill(new Fill(argbFill))
                        .stroke(new Stroke(2, argbBorder))
                        .tooltip(new Tooltip(playerName + "'s Claim"))
                        .build();

                List<double[][]> polygons = createPolygons(claims);
                int i = 0;
                for (double[][] polygon : polygons) {
                    if (polygon[0].length < 3)
                        continue;

                    List<Point> points = new ArrayList<>();
                    for (int j = 0; j < polygon[0].length; j++) {
                        points.add(Point.of(polygon[0][j], polygon[1][j]));
                    }

                    Polygon marker = Marker.polygon(playerId.toString() + "_" + i,
                            Marker.polyline(playerId.toString() + "_line_" + i, points));
                    marker.setOptions(options);

                    layer.addMarker(marker);
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
