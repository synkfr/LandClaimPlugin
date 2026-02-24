package org.ayosynk.landClaimPlugin.models;

import java.util.HashSet;
import java.util.Set;

public class ChunkSelection {
    private ChunkPosition pos1;
    private ChunkPosition pos2;

    public ChunkSelection() {
    }

    public ChunkPosition getPos1() {
        return pos1;
    }

    public void setPos1(ChunkPosition pos1) {
        this.pos1 = pos1;
    }

    public ChunkPosition getPos2() {
        return pos2;
    }

    public void setPos2(ChunkPosition pos2) {
        this.pos2 = pos2;
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public Set<ChunkPosition> getSelectedChunks() {
        Set<ChunkPosition> chunks = new HashSet<>();
        if (!isComplete())
            return chunks;

        if (!pos1.world().equals(pos2.world()))
            return chunks;

        int minX = Math.min(pos1.x(), pos2.x());
        int maxX = Math.max(pos1.x(), pos2.x());
        int minZ = Math.min(pos1.z(), pos2.z());
        int maxZ = Math.max(pos1.z(), pos2.z());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                chunks.add(new ChunkPosition(pos1.world(), x, z));
            }
        }
        return chunks;
    }
}
