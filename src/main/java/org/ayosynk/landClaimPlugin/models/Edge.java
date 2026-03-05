package org.ayosynk.landClaimPlugin.models;

public record Edge(int x1, int z1, int x2, int z2) {
    public Edge {
        if (x1 == x2) {
            int tz1 = Math.min(z1, z2);
            int tz2 = Math.max(z1, z2);
            z1 = tz1;
            z2 = tz2;
        } else if (z1 == z2) {
            int tx1 = Math.min(x1, x2);
            int tx2 = Math.max(x1, x2);
            x1 = tx1;
            x2 = tx2;
        }
    }

    public boolean isVertical() {
        return x1 == x2;
    }

    public boolean isHorizontal() {
        return z1 == z2;
    }
}