package org.ayosynk.landClaimPlugin.models;

/**
 * Maps Minecraft blocks to their corresponding claim protection flags.
 */
public enum BlockPermission {
    CONTAINERS("USE_CONTAINERS"),
    DOORS("USE_DOORS"),
    TRAPDOORS("USE_TRAPDOORS"),
    FENCE_GATES("USE_FENCE_GATES"),
    REDSTONE("USE_REDSTONE"),
    BEDS("USE_BEDS"),
    WORKSTATIONS("USE_WORKSTATIONS"),
    LECTERNS("USE_LECTERNS"),
    BELLS("USE_BELLS");

    private final String flag;

    BlockPermission(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }
}
