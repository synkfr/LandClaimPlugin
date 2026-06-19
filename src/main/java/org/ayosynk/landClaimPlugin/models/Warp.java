package org.ayosynk.landClaimPlugin.models;

import org.bukkit.Location;
import org.bukkit.Material;

public class Warp {
    private String name;
    private Location location;
    private Material icon;
    private boolean isPublic;

    public Warp(String name, Location location, Material icon) {
        this(name, location, icon, false);
    }

    public Warp(String name, Location location, Material icon, boolean isPublic) {
        this.name = name;
        this.location = location;
        this.icon = icon;
        this.isPublic = isPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    /**
     * @return {@code true} if this warp is listed in the server-wide
     *         public warps GUI and any player with {@code landclaim.warp}
     *         can teleport to it. {@code false} if the warp is private
     *         (only the owner and claim members can use it).
     */
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
