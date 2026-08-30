package org.ayosynk.landClaimPlugin.map;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryMapRenderer extends MapRenderer {

    public enum ZoomLevel {
        OVERVIEW(16, 8),
        DETAIL(8, 16);

        public final int chunksPerAxis;
        public final int pixelsPerChunk;

        ZoomLevel(int chunksPerAxis, int pixelsPerChunk) {
            this.chunksPerAxis = chunksPerAxis;
            this.pixelsPerChunk = pixelsPerChunk;
        }

        public ZoomLevel next() {
            return this == OVERVIEW ? DETAIL : OVERVIEW;
        }
    }

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConcurrentHashMap<UUID, ZoomLevel> playerZoomMap = new ConcurrentHashMap<>();

    private static final Color COLOR_WILDERNESS = new Color(40, 44, 52);
    private static final Color COLOR_GRID = new Color(25, 27, 32);
    private static final Color COLOR_OWN = new Color(46, 204, 113);
    private static final Color COLOR_ALLY = new Color(155, 89, 182);
    private static final Color COLOR_MEMBER = new Color(52, 152, 219);
    private static final Color COLOR_OTHER = new Color(231, 76, 60);

    public TerritoryMapRenderer(LandClaimPlugin plugin) {
        super(true);
        this.plugin = plugin;
        this.claimManager = plugin.getClaimManager();
    }

    public ZoomLevel getZoom(UUID playerId) {
        return playerZoomMap.getOrDefault(playerId, ZoomLevel.OVERVIEW);
    }

    public ZoomLevel toggleZoom(UUID playerId) {
        ZoomLevel next = getZoom(playerId).next();
        playerZoomMap.put(playerId, next);
        return next;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        Location loc = player.getLocation();
        if (loc.getWorld() == null) return;

        String worldName = loc.getWorld().getName();
        int centerChunkX = loc.getBlockX() >> 4;
        int centerChunkZ = loc.getBlockZ() >> 4;

        ZoomLevel zoom = getZoom(player.getUniqueId());
        int radius = zoom.chunksPerAxis / 2;
        int pxPerChunk = zoom.pixelsPerChunk;

        ClaimProfile playerProfile = claimManager.getProfile(player.getUniqueId());

        for (int r = 0; r < zoom.chunksPerAxis; r++) {
            for (int c = 0; c < zoom.chunksPerAxis; c++) {
                int chunkX = centerChunkX + (c - radius);
                int chunkZ = centerChunkZ + (r - radius);

                ChunkPosition pos = new ChunkPosition(worldName, chunkX, chunkZ);
                ClaimProfile chunkProfile = claimManager.getProfileAt(pos);

                Color fillColor = getFillColor(chunkProfile, player.getUniqueId(), playerProfile);

                int startX = c * pxPerChunk;
                int startZ = r * pxPerChunk;

                for (int x = 0; x < pxPerChunk; x++) {
                    for (int z = 0; z < pxPerChunk; z++) {
                        int canvasX = startX + x;
                        int canvasZ = startZ + z;
                        if (canvasX >= 0 && canvasX < 128 && canvasZ >= 0 && canvasZ < 128) {
                            if (x == 0 || z == 0 || x == pxPerChunk - 1 || z == pxPerChunk - 1) {
                                canvas.setPixelColor(canvasX, canvasZ, COLOR_GRID);
                            } else {
                                canvas.setPixelColor(canvasX, canvasZ, fillColor);
                            }
                        }
                    }
                }
            }
        }

        MapCursorCollection cursors = canvas.getCursors();
        while (cursors.size() > 0) {
            cursors.removeCursor(cursors.getCursor(0));
        }

        byte cursorYaw = (byte) Math.round((loc.getYaw() * 16.0f) / 360.0f);
        if (cursorYaw < 0) cursorYaw += 16;
        if (cursorYaw > 15) cursorYaw = 0;

        cursors.addCursor(new MapCursor((byte) 0, (byte) 0, cursorYaw, MapCursor.Type.PLAYER, true));

        ClaimProfile currentProfile = claimManager.getProfileAt(new ChunkPosition(loc));
        String label = currentProfile != null ? currentProfile.getName() : "Wilderness";
        if (label.length() > 14) {
            label = label.substring(0, 14);
        }
        canvas.drawText(4, 4, MinecraftFont.Font, label);
    }

    private Color getFillColor(ClaimProfile profile, UUID viewerId, ClaimProfile viewerProfile) {
        if (profile == null) {
            return COLOR_WILDERNESS;
        }

        if (profile.getOwnerId().equals(viewerId)) {
            return COLOR_OWN;
        }

        if (profile.isMember(viewerId) || profile.isTrusted(viewerId)) {
            return COLOR_MEMBER;
        }

        if (viewerProfile != null && viewerProfile.hasAlly(profile.getProfileId())) {
            return COLOR_ALLY;
        }

        return COLOR_OTHER;
    }
}
