package org.ayosynk.landClaimPlugin.models;

import java.util.Objects;

public class Edge {
    public final int x1, z1, x2, z2;

    public Edge(int x1, int z1, int x2, int z2) {
        if (x1 == x2) {
            this.x1 = x1;
            this.x2 = x2;
            this.z1 = Math.min(z1, z2);
            this.z2 = Math.max(z1, z2);
        } else if (z1 == z2) {
            this.z1 = z1;
            this.z2 = z2;
            this.x1 = Math.min(x1, x2);
            this.x2 = Math.max(x1, x2);
        } else {
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return x1 == edge.x1 && z1 == edge.z1 && x2 == edge.x2 && z2 == edge.z2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, z1, x2, z2);
    }

    public boolean isVertical() {
        return x1 == x2;
    }

    public boolean isHorizontal() {
        return z1 == z2;
    }
}