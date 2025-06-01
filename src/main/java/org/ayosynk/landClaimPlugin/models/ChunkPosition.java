package org.ayosynk.landClaimPlugin.models;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChunkPosition {
    private final String world;
    private final int x;
    private final int z;

    public ChunkPosition(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public ChunkPosition(Block block) {
        this(block.getChunk());
    }

    public ChunkPosition(Location location) {
        this(location.getChunk());
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

        // Orthogonal neighbors
        neighbors.add(new ChunkPosition(world, x + 1, z));
        neighbors.add(new ChunkPosition(world, x - 1, z));
        neighbors.add(new ChunkPosition(world, x, z + 1));
        neighbors.add(new ChunkPosition(world, x, z - 1));

        // Diagonal neighbors
        if (includeDiagonals) {
            neighbors.add(new ChunkPosition(world, x + 1, z + 1));
            neighbors.add(new ChunkPosition(world, x + 1, z - 1));
            neighbors.add(new ChunkPosition(world, x - 1, z + 1));
            neighbors.add(new ChunkPosition(world, x - 1, z - 1));
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
        return world + "," + x + "," + z;
    }
}