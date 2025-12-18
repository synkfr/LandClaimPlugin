package org.ayosynk.landClaimPlugin.models;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkPosition {
    // Cache for frequently accessed chunk positions (flyweight pattern)
    private static final int MAX_CACHE_SIZE = 1000;
    private static final Map<String, ChunkPosition> cache = new ConcurrentHashMap<>();
    
    private final String world;
    private final int x;
    private final int z;
    private final String cacheKey; // Pre-computed for faster lookups

    public ChunkPosition(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.cacheKey = world + "," + x + "," + z;
    }

    public ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.cacheKey = world + "," + x + "," + z;
    }

    public ChunkPosition(Block block) {
        this(block.getChunk());
    }

    public ChunkPosition(Location location) {
        this(location.getChunk());
    }
    
    /**
     * Get a cached ChunkPosition instance (flyweight pattern)
     * Reduces object creation in hot paths
     */
    public static ChunkPosition of(String world, int x, int z) {
        String key = world + "," + x + "," + z;
        ChunkPosition cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        
        // Evict if cache is too large
        if (cache.size() >= MAX_CACHE_SIZE) {
            // Simple eviction: clear half the cache
            int toRemove = MAX_CACHE_SIZE / 2;
            var iterator = cache.keySet().iterator();
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }
        
        ChunkPosition pos = new ChunkPosition(world, x, z);
        cache.put(key, pos);
        return pos;
    }
    
    /**
     * Get a cached ChunkPosition from a Chunk
     */
    public static ChunkPosition of(Chunk chunk) {
        return of(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public List<ChunkPosition> getNeighbors(boolean includeDiagonals) {
        List<ChunkPosition> neighbors = new ArrayList<>();

        // Orthogonal neighbors - use cached instances
        neighbors.add(ChunkPosition.of(world, x + 1, z));
        neighbors.add(ChunkPosition.of(world, x - 1, z));
        neighbors.add(ChunkPosition.of(world, x, z + 1));
        neighbors.add(ChunkPosition.of(world, x, z - 1));

        // Diagonal neighbors
        if (includeDiagonals) {
            neighbors.add(ChunkPosition.of(world, x + 1, z + 1));
            neighbors.add(ChunkPosition.of(world, x + 1, z - 1));
            neighbors.add(ChunkPosition.of(world, x - 1, z + 1));
            neighbors.add(ChunkPosition.of(world, x - 1, z - 1));
        }

        return neighbors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return cacheKey;
    }
}