package org.ayosynk.landClaimPlugin.hooks.map;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;

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
            // Build world -> (profileId -> chunks) index directly from all cached profiles
            Map<String, Map<UUID, Set<ChunkPosition>>> worldProfileClaims = new HashMap<>();
            // Keep a profile lookup by profileId for later use
            Map<UUID, ClaimProfile> profileLookup = new HashMap<>();

            Collection<ClaimProfile> allProfiles = claimManager.getAllProfiles();
            int totalChunks = 0;

            for (ClaimProfile profile : allProfiles) {
                Set<ChunkPosition> chunks = profile.getOwnedChunks();
                if (chunks.isEmpty()) continue;

                UUID profileId = profile.getProfileId();
                profileLookup.put(profileId, profile);

                for (ChunkPosition pos : chunks) {
                    worldProfileClaims
                            .computeIfAbsent(pos.world(), k -> new HashMap<>())
                            .computeIfAbsent(profileId, k -> new HashSet<>())
                            .add(pos);
                    totalChunks++;
                }
            }

            plugin.getLogger().info("[BlueMap] Updating markers: " + allProfiles.size() + " profiles, " + totalChunks + " total chunks across " + worldProfileClaims.size() + " worlds.");

            double fillOpacity = plugin.getConfigManager().getPluginConfig().bluemap.fillOpacity;
            double borderOpacity = plugin.getConfigManager().getPluginConfig().bluemap.borderOpacity;

            int totalMarkers = 0;

            for (BlueMapMap map : api.getMaps()) {
                String worldId = map.getWorld().getId();

                MarkerSet markerSet = MarkerSet.builder()
                        .label("LandClaims")
                        .defaultHidden(false)
                        .build();

                Map<UUID, Set<ChunkPosition>> profileClaimsInWorld = null;
                String matchedWorldName = null;
                for (Map.Entry<String, Map<UUID, Set<ChunkPosition>>> entry : worldProfileClaims.entrySet()) {
                    String worldName = entry.getKey();
                    if (matchesBlueMapWorld(worldId, worldName)) {
                        profileClaimsInWorld = entry.getValue();
                        matchedWorldName = worldName;
                        break;
                    }
                }

                if (profileClaimsInWorld == null) {
                    plugin.getLogger().info("[BlueMap] No claims matched for map world '" + worldId + "'. Known claim worlds: " + worldProfileClaims.keySet());
                    map.getMarkerSets().put("landclaims", markerSet);
                    continue;
                }

                plugin.getLogger().info("[BlueMap] Matched world '" + matchedWorldName + "' -> BlueMap world '" + worldId + "' with " + profileClaimsInWorld.size() + " profiles.");

                for (Map.Entry<UUID, Set<ChunkPosition>> entry : profileClaimsInWorld.entrySet()) {
                    UUID profileId = entry.getKey();
                    ClaimProfile profile = profileLookup.get(profileId);
                    if (profile == null) continue;

                    String playerName = profile.getDisplayOwnerName();
                    int r, g, b;
                    if (profile.getClaimColor() != null
                            && profile.getClaimColor().length() >= 7) {
                        try {
                            String hex = profile.getClaimColor().startsWith("#")
                                    ? profile.getClaimColor().substring(1)
                                    : profile.getClaimColor();
                            r = Integer.parseInt(hex.substring(0, 2), 16);
                            g = Integer.parseInt(hex.substring(2, 4), 16);
                            b = Integer.parseInt(hex.substring(4, 6), 16);
                        } catch (NumberFormatException ex) {
                            Random rnd = new Random(profileId.getMostSignificantBits());
                            r = rnd.nextInt(200) + 55;
                            g = rnd.nextInt(200) + 55;
                            b = rnd.nextInt(200) + 55;
                        }
                    } else {
                        Random rnd = new Random(profileId.getMostSignificantBits());
                        r = rnd.nextInt(200) + 55;
                        g = rnd.nextInt(200) + 55;
                        b = rnd.nextInt(200) + 55;
                    }
                    Color pFill = new Color(r, g, b, (float) fillOpacity);
                    Color pBorder = new Color(r, g, b, (float) borderOpacity);

                    Set<ChunkPosition> chunks = entry.getValue();
                    List<double[][]> polygons = createPolygons(chunks);
                    int i = 0;
                    for (double[][] polygon : polygons) {
                        if (polygon[0].length < 3)
                            continue;

                        Shape.Builder shapeBuilder = Shape.builder();
                        for (int j = 0; j < polygon[0].length; j++) {
                            shapeBuilder.addPoint(new Vector2d(polygon[0][j], polygon[1][j]));
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

                        String markerId = profileId.toString() + "_" + i;
                        markerSet.getMarkers().put(markerId, marker);
                        i++;
                        totalMarkers++;
                    }
                }

                map.getMarkerSets().put("landclaims", markerSet);
            }

            plugin.getLogger().info("[BlueMap] Marker update complete. Total markers created: " + totalMarkers);
        });
    }

    /**
     * Check whether a BlueMap world ID corresponds to a Bukkit world name.
     * <p>
     * BlueMap world IDs use several formats depending on version and server type:
     * <ul>
     *   <li>{@code "worldName#minecraft:overworld"} (common modern format)</li>
     *   <li>{@code "path/to/worldName"} or {@code "namespace:worldName"}</li>
     *   <li>Spaces in folder names may be kept or replaced with underscores</li>
     * </ul>
     * Vanilla Bukkit maps {@code "world"} → overworld, {@code "world_nether"} → the_nether,
     * {@code "world_the_end"} → the_end.
     */
    private static boolean matchesBlueMapWorld(String blueMapWorldId, String bukkitWorldName) {
        // Normalize: BlueMap may replace spaces with underscores
        String normalizedBukkit = bukkitWorldName.replace(" ", "_");
        String normalizedBlueMap = blueMapWorldId.replace(" ", "_");

        // Split on '#' — BlueMap uses "worldName#minecraft:dimension"
        String blueMapBase = normalizedBlueMap.contains("#")
                ? normalizedBlueMap.substring(0, normalizedBlueMap.indexOf('#'))
                : normalizedBlueMap;
        String blueMapDimension = normalizedBlueMap.contains("#")
                ? normalizedBlueMap.substring(normalizedBlueMap.indexOf('#') + 1)
                : "";

        // Extract the last path segment if the base contains path separators
        String blueMapBaseName = blueMapBase;
        int lastSep = Math.max(blueMapBase.lastIndexOf('/'), blueMapBase.lastIndexOf('\\'));
        if (lastSep >= 0) {
            blueMapBaseName = blueMapBase.substring(lastSep + 1);
        }

        // Direct name match (with or without path)
        if (blueMapBase.equals(normalizedBukkit) || blueMapBaseName.equals(normalizedBukkit)) {
            // For custom world names, the base name match is sufficient.
            // For vanilla "world" names, also check the dimension suffix.
            if (normalizedBukkit.equals("world")) {
                return blueMapDimension.isEmpty() || blueMapDimension.contains("overworld");
            }
            if (normalizedBukkit.equals("world_nether")) {
                return blueMapDimension.isEmpty() || blueMapDimension.contains("the_nether");
            }
            if (normalizedBukkit.equals("world_the_end")) {
                return blueMapDimension.isEmpty() || blueMapDimension.contains("the_end");
            }
            return true;
        }

        // Vanilla world name mappings: Bukkit uses "world" for the overworld folder,
        // but the dimension in the BlueMap ID might differ from the folder name.
        if (normalizedBukkit.equals("world") && blueMapDimension.contains("overworld")) {
            return true;
        }
        if (normalizedBukkit.equals("world_nether") && blueMapDimension.contains("the_nether")) {
            return true;
        }
        if (normalizedBukkit.equals("world_the_end") && blueMapDimension.contains("the_end")) {
            return true;
        }

        // Colon-separated format: "namespace:worldName"
        if (blueMapBase.endsWith(":" + normalizedBukkit)) {
            return true;
        }

        return false;
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

    public boolean isActive() {
        return active;
    }
}
