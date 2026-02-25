package org.ayosynk.landClaimPlugin.hooks;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;

import java.util.*;

public class BlueMapHook {
    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private boolean active = false;

    public BlueMapHook(LandClaimPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;

        BlueMapAPI.onEnable(api -> {
            active = true;
            plugin.getLogger().info("BlueMap integration enabled.");
            update();
        });

        BlueMapAPI.onDisable(api -> {
            active = false;
            plugin.getLogger().info("BlueMap integration disabled.");
        });
    }

    public void update() {
        BlueMapAPI.getInstance().ifPresent(api -> {
            Map<String, Map<UUID, Set<ChunkPosition>>> worldPlayerClaims = new HashMap<>();

            for (UUID playerId : getAllPlayerIds()) {
                Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = claimManager.getPlayerClaims(playerId);
                Set<ChunkPosition> claims = claimObjects.stream()
                        .flatMap(claim -> claim.getChunks().stream())
                        .collect(java.util.stream.Collectors.toSet());
                for (ChunkPosition pos : claims) {
                    worldPlayerClaims
                            .computeIfAbsent(pos.world(), k -> new HashMap<>())
                            .computeIfAbsent(playerId, k -> new HashSet<>())
                            .add(pos);
                }
            }

            double fillOpacity = plugin.getConfigManager().getPluginConfig().bluemap.fillOpacity;
            double borderOpacity = plugin.getConfigManager().getPluginConfig().bluemap.borderOpacity;

            for (BlueMapMap map : api.getMaps()) {
                String worldId = map.getWorld().getId();

                MarkerSet markerSet = MarkerSet.builder()
                        .label("LandClaims")
                        .defaultHidden(false)
                        .build();

                Map<UUID, Set<ChunkPosition>> playerClaimsInWorld = null;
                for (Map.Entry<String, Map<UUID, Set<ChunkPosition>>> entry : worldPlayerClaims.entrySet()) {
                    String worldName = entry.getKey();
                    if (worldId.equals(worldName)
                            || worldId.endsWith("/" + worldName)
                            || worldId.endsWith("\\" + worldName)
                            || worldId.endsWith(":" + worldName)
                            || (worldName.equals("world") && worldId.contains("overworld"))
                            || (worldName.equals("world_nether") && worldId.contains("the_nether"))
                            || (worldName.equals("world_the_end") && worldId.contains("the_end"))) {
                        playerClaimsInWorld = entry.getValue();
                        break;
                    }
                }

                if (playerClaimsInWorld != null) {
                    for (Map.Entry<UUID, Set<ChunkPosition>> entry : playerClaimsInWorld.entrySet()) {
                        UUID playerId = entry.getKey();
                        String playerName = Bukkit.getOfflinePlayer(playerId).getName();
                        if (playerName == null)
                            playerName = "Unknown";

                        Random rnd = new Random(playerId.getMostSignificantBits());
                        int r = rnd.nextInt(200) + 55;
                        int g = rnd.nextInt(200) + 55;
                        int b = rnd.nextInt(200) + 55;
                        Color pFill = new Color(r, g, b, (float) fillOpacity);
                        Color pBorder = new Color(r, g, b, (float) borderOpacity);

                        Set<ChunkPosition> chunks = entry.getValue();
                        List<double[][]> polygons = createPolygons(chunks);
                        int i = 0;
                        for (double[][] polygon : polygons) {
                            if (polygon[0].length < 3)
                                continue;

                            Shape.Builder shapeBuilder = Shape.builder();

                            try {
                                Class<?> vectorClass = Class.forName("com.flowpowered.math.vector.Vector2d", true,
                                        BlueMapAPI.class.getClassLoader());
                                java.lang.reflect.Constructor<?> vectorConstructor = vectorClass
                                        .getConstructor(double.class, double.class);
                                java.lang.reflect.Method addPointMethod = shapeBuilder.getClass().getMethod("addPoint",
                                        vectorClass);

                                for (int j = 0; j < polygon[0].length; j++) {
                                    Object vec = vectorConstructor.newInstance(polygon[0][j], polygon[1][j]);
                                    addPointMethod.invoke(shapeBuilder, vec);
                                }
                            } catch (Exception ex) {
                                plugin.getLogger().warning("Failed to create BlueMap shape: " + ex.getMessage());
                                continue;
                            }

                            Shape shape = shapeBuilder.build();

                            ShapeMarker marker = ShapeMarker.builder()
                                    .label(playerName + "'s Claim")
                                    .shape(shape, 64)
                                    .fillColor(pFill)
                                    .lineColor(pBorder)
                                    .lineWidth(2)
                                    .depthTestEnabled(false)
                                    .build();

                            String markerId = playerId.toString() + "_" + i;
                            markerSet.getMarkers().put(markerId, marker);
                            i++;
                        }
                    }
                }

                map.getMarkerSets().put("landclaims", markerSet);
            }
        });
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
