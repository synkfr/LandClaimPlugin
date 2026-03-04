package org.ayosynk.landClaimPlugin.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A single global claim profile per player.
 * All claimed land, permissions, roles, trusted players, and visitor flags
 * are attached to this profile. A player may own at most one ClaimProfile.
 */
public class ClaimProfile {

    private UUID ownerId;
    private String name;

    private final Set<ChunkPosition> ownedChunks = new HashSet<>();
    private final Set<String> visitorFlags = new HashSet<>();
    private final Map<UUID, Set<String>> trustedPlayerFlags = new HashMap<>();
    private final Map<String, Role> roles = new HashMap<>();
    private final Map<UUID, String> memberRoles = new HashMap<>();
    private final Map<UUID, Set<String>> allyFlags = new HashMap<>();
    private final Map<String, Warp> warps = new HashMap<>();
    private String claimColor; // Hex color string, e.g. "#00FF00", nullable (falls back to default)
    private String visualizationMode = "DISPLAY_ENTITY"; // "DISPLAY_ENTITY" or "PARTICLE"

    public ClaimProfile(UUID ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
        setupDefaultRoles();
    }

    private void setupDefaultRoles() {
        // Default Member Role (Basic Interact)
        Role memberRole = new Role(UUID.randomUUID(), this.ownerId, "Member", 100);
        memberRole.addFlag("USE_DOORS");
        memberRole.addFlag("USE_TRAPDOORS");
        memberRole.addFlag("USE_FENCE_GATES");
        memberRole.addFlag("USE_CONTAINERS");
        memberRole.addFlag("USE_WORKSTATIONS");
        memberRole.addFlag("USE_BEDS");
        memberRole.addFlag("USE_REDSTONE");
        this.roles.put(memberRole.getId().toString(), memberRole);

        // Default CoOwner Role (All Permissions)
        Role coOwnerRole = new Role(UUID.randomUUID(), this.ownerId, "CoOwner", 10);
        String[] allFlags = {
                "USE_DOORS", "USE_TRAPDOORS", "USE_FENCE_GATES", "USE_CONTAINERS",
                "USE_WORKSTATIONS", "USE_BEDS", "USE_REDSTONE", "USE_LECTERNS", "USE_BELLS",
                "DAMAGE_ANIMALS", "DAMAGE_MONSTERS", "BREED_ANIMALS", "SHEAR_ENTITIES",
                "TRADE_VILLAGERS", "FEED_ANIMALS", "LEASH_ENTITIES", "MODIFY_ARMOR_STANDS",
                "MODIFY_ITEM_FRAMES", "RIDE_VEHICLES", "PLACE_VEHICLES", "DESTROY_VEHICLES",
                "USE_ENDER_PEARLS", "USE_CHORUS_FRUIT", "PICKUP_ITEMS", "DROP_ITEMS"
        };
        for (String flag : allFlags) {
            coOwnerRole.addFlag(flag);
        }
        this.roles.put(coOwnerRole.getId().toString(), coOwnerRole);
    }

    // --- Owner ---

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwner(UUID playerId) {
        return ownerId.equals(playerId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Chunks ---

    public Set<ChunkPosition> getOwnedChunks() {
        return ownedChunks;
    }

    public void addChunk(ChunkPosition pos) {
        ownedChunks.add(pos);
    }

    public void removeChunk(ChunkPosition pos) {
        ownedChunks.remove(pos);
    }

    public boolean ownsChunk(ChunkPosition pos) {
        return ownedChunks.contains(pos);
    }

    // --- Visitor Flags (base permission layer) ---

    public Set<String> getVisitorFlags() {
        return visitorFlags;
    }

    public boolean hasVisitorFlag(String flag) {
        return visitorFlags.contains(flag.toUpperCase());
    }

    public void addVisitorFlag(String flag) {
        visitorFlags.add(flag.toUpperCase());
    }

    public void removeVisitorFlag(String flag) {
        visitorFlags.remove(flag.toUpperCase());
    }

    // --- Trusted Players (per-player permission overrides) ---

    public Map<UUID, Set<String>> getTrustedPlayerFlags() {
        return trustedPlayerFlags;
    }

    public boolean isTrusted(UUID playerId) {
        return trustedPlayerFlags.containsKey(playerId);
    }

    public Set<String> getTrustedFlags(UUID playerId) {
        return trustedPlayerFlags.get(playerId);
    }

    public void setTrustedFlags(UUID playerId, Set<String> flags) {
        trustedPlayerFlags.put(playerId, flags);
    }

    public void addTrustedPlayer(UUID playerId) {
        trustedPlayerFlags.putIfAbsent(playerId, new HashSet<>());
    }

    public void removeTrustedPlayer(UUID playerId) {
        trustedPlayerFlags.remove(playerId);
    }

    // --- Roles (per-profile role definitions) ---

    public Map<String, Role> getRoles() {
        return roles;
    }

    public Role getRoleByName(String name) {
        return roles.get(name.toLowerCase());
    }

    public void addRole(Role role) {
        roles.put(role.getName().toLowerCase(), role);
    }

    public void removeRole(String name) {
        roles.remove(name.toLowerCase());
    }

    // --- Member Roles (player → role assignment) ---

    public Map<UUID, String> getMemberRoles() {
        return memberRoles;
    }

    public boolean isMember(UUID playerId) {
        return memberRoles.containsKey(playerId);
    }

    public String getMemberRole(UUID playerId) {
        return memberRoles.get(playerId);
    }

    public void setMemberRole(UUID playerId, String roleName) {
        if (roleName == null) {
            memberRoles.remove(playerId);
        } else {
            memberRoles.put(playerId, roleName);
        }
    }

    public void removeMember(UUID playerId) {
        memberRoles.remove(playerId);
    }

    // --- Allies (inter-claim permissions) ---

    public Map<UUID, Set<String>> getAllyFlags() {
        return allyFlags;
    }

    public boolean hasAlly(UUID allyOwnerId) {
        return allyFlags.containsKey(allyOwnerId);
    }

    public Set<String> getAllyFlags(UUID allyOwnerId) {
        return allyFlags.get(allyOwnerId);
    }

    public void setAllyFlags(UUID allyOwnerId, Set<String> flags) {
        allyFlags.put(allyOwnerId, flags);
    }

    public void addAlly(UUID allyOwnerId) {
        allyFlags.putIfAbsent(allyOwnerId, new HashSet<>());
    }

    public void removeAlly(UUID allyOwnerId) {
        allyFlags.remove(allyOwnerId);
    }

    // --- Warps ---

    public Map<String, Warp> getWarps() {
        return warps;
    }

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public void addWarp(Warp warp) {
        warps.put(warp.getName().toLowerCase(), warp);
    }

    public void removeWarp(String name) {
        warps.remove(name.toLowerCase());
    }

    // --- Claim Color ---

    public String getClaimColor() {
        return claimColor;
    }

    public void setClaimColor(String claimColor) {
        this.claimColor = claimColor;
    }

    // --- Visualization Mode ---

    public String getVisualizationMode() {
        return visualizationMode;
    }

    public void setVisualizationMode(String visualizationMode) {
        this.visualizationMode = visualizationMode;
    }
}
