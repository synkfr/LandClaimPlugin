package org.ayosynk.landClaimPlugin.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final UUID id;
    private String name;
    private int priority;
    private final Set<String> flags = new HashSet<>();

    public Role(UUID id, String name, int priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public void addFlag(String flag) {
        flags.add(flag.toLowerCase());
    }

    public void removeFlag(String flag) {
        flags.remove(flag.toLowerCase());
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag.toLowerCase());
    }
}
