package org.ayosynk.landClaimPlugin.hooks;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.*;

public class DynmapHook {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private MarkerAPI markerAPI;
    private MarkerSet markerSet;
    private boolean active = false;

    public DynmapHook(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;

        try {
            DynmapCommonAPI dynmapAPI = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
            if (dynmapAPI == null) {
                plugin.getLogger().warning("Dynmap plugin not found.");
                return;
            }

            markerAPI = dynmapAPI.getMarkerAPI();
            if (markerAPI == null) {
                plugin.getLogger().warning("Dynmap Marker API not available.");
                return;
            }

            markerSet = markerAPI.getMarkerSet("landclaims");
            if (markerSet == null) {
                markerSet = markerAPI.createMarkerSet("landclaims", "Land Claims", null, false);
            }
            if (markerSet == null) {
                plugin.getLogger().warning("Failed to create Dynmap marker set.");
                return;
            }

            markerSet.setHideByDefault(false);
            markerSet.setLayerPriority(10);
            markerSet.setMinZoom(0);

            active = true;
            plugin.getLogger().info("Dynmap integration enabled.");
            update();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize Dynmap integration: " + e.getMessage());
        }
    }

    public void update() {
        if (!active || markerSet == null)
            return;

        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            marker.deleteMarker();
        }

        String fillColorHex = plugin.getConfig().getString("dynmap.fill-color", "3366FF");
        double fillOpacity = plugin.getConfig().getDouble("dynmap.fill-opacity", 0.3);
        String borderColorHex = plugin.getConfig().getString("dynmap.border-color", "3366FF");
        double borderOpacity = plugin.getConfig().getDouble("dynmap.border-opacity", 0.8);

        int fillColor = parseHexColor(fillColorHex);
        int borderColor = parseHexColor(borderColorHex);

        for (UUID playerId : getAllPlayerIds()) {
            String playerName = Bukkit.getOfflinePlayer(playerId).getName();
            if (playerName == null)
                playerName = "Unknown";

            Set<ChunkPosition> claims = claimManager.getPlayerClaims(playerId);

            Map<String, Set<ChunkPosition>> claimsByWorld = new HashMap<>();
            for (ChunkPosition pos : claims) {
                claimsByWorld.computeIfAbsent(pos.world(), k -> new HashSet<>()).add(pos);
            }

            int i = 0;
            for (Map.Entry<String, Set<ChunkPosition>> worldEntry : claimsByWorld.entrySet()) {
                String worldName = worldEntry.getKey();
                List<double[][]> polygons = createPolygons(worldEntry.getValue());

                for (double[][] polygon : polygons) {
                    if (polygon[0].length < 3)
                        continue;

                    String markerId = "lc_" + playerId.toString() + "_" + i;
                    String label = playerName + "'s Claim";

                    double[] xCorners = polygon[0];
                    double[] zCorners = polygon[1];

                    AreaMarker marker = markerSet.createAreaMarker(
                            markerId, label, false,
                            worldName,
                            xCorners, zCorners, false);

                    if (marker != null) {
                        marker.setFillStyle(fillOpacity, fillColor);
                        marker.setLineStyle(2, borderOpacity, borderColor);
                        marker.setDescription("<b>" + playerName + "'s Claim</b>");
                    }

                    i++;
                }
            }
        }
    }

    private record Point(int x, int z) {
    }

    private record Edge(Point from, Point to) {
    }

    private List<double[][]> createPolygons(Set<ChunkPosition> chunks) {
        Set<Edge> edges = new HashSet<>();
        for (ChunkPosition chunk : chunks) {
            int cx = chunk.x();
            int cz = chunk.z();

            Point p00 = new Point(cx, cz);
            Point p10 = new Point(cx + 1, cz);
            Point p11 = new Point(cx + 1, cz + 1);
            Point p01 = new Point(cx, cz + 1);

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

        Map<Point, List<Edge>> adjacency = new HashMap<>();
        for (Edge e : edges) {
            adjacency.computeIfAbsent(e.from(), k -> new ArrayList<>()).add(e);
        }

        List<double[][]> polygons = new ArrayList<>();

        while (!adjacency.isEmpty()) {
            Point start = adjacency.keySet().iterator().next();
            List<Double> xPts = new ArrayList<>();
            List<Double> zPts = new ArrayList<>();
            Point current = start;

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

    private int parseHexColor(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0x3366FF;
        }
    }

    private Set<UUID> getAllPlayerIds() {
        Set<UUID> playerIds = new HashSet<>();
        for (var offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.hasPlayedBefore()) {
                UUID playerId = offlinePlayer.getUniqueId();
                if (!claimManager.getPlayerClaims(playerId).isEmpty()) {
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
