package org.ayosynk.landClaimPlugin.models;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable chunk coordinate identified by world name and chunk X/Z.
 * <p>
 * Used as the primary key for claimed chunks and for adjacency checks.
 * Stored in the database as {@code "world:x:z"}.
 *
 * @param world the world name
 * @param x     chunk X coordinate
 * @param z     chunk Z coordinate
 */
public record ChunkPosition(String world, int x, int z) {

    public ChunkPosition(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public ChunkPosition(Block block) {
        this(block.getChunk());
    }

    public ChunkPosition(Location location) {
        this(location.getChunk());
    }

    public static ChunkPosition of(String world, int x, int z) {
        return new ChunkPosition(world, x, z);
    }

    public static ChunkPosition of(Chunk chunk) {
        return new ChunkPosition(chunk);
    }

    /**
     * Get all adjacent chunk positions.
     *
     * @param includeDiagonals if true, include diagonal neighbors (8 total vs 4)
     * @return list of neighboring chunk positions
     */
    public List<ChunkPosition> getNeighbors(boolean includeDiagonals) {
        List<ChunkPosition> neighbors = new ArrayList<>();
        neighbors.add(new ChunkPosition(world, x + 1, z));
        neighbors.add(new ChunkPosition(world, x - 1, z));
        neighbors.add(new ChunkPosition(world, x, z + 1));
        neighbors.add(new ChunkPosition(world, x, z - 1));
        if (includeDiagonals) {
            neighbors.add(new ChunkPosition(world, x + 1, z + 1));
            neighbors.add(new ChunkPosition(world, x + 1, z - 1));
            neighbors.add(new ChunkPosition(world, x - 1, z + 1));
            neighbors.add(new ChunkPosition(world, x - 1, z - 1));
        }
        return neighbors;
    }

    @Override
    public String toString() {
        return world + "," + x + "," + z;
    }
}