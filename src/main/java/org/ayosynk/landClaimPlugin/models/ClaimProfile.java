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
    // Use UUID based on namespace to guarantee no collision with real player UUIDs
    public static final UUID ADMIN_PROFILE_ID = UUID.nameUUIDFromBytes("landclaim.admin.profile".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    private final UUID profileId; // The unique ID of the profile (maps to owner_id in DB for backwards compatibility)
    private UUID realOwnerId; // The actual player UUID who owns the profile
    private String name;
    private String ownerAlias;

    private final Set<ChunkPosition> ownedChunks = new HashSet<>();
    private final Set<String> visitorFlags = new HashSet<>();
    private final Map<UUID, Set<String>> trustedPlayerFlags = new HashMap<>();
    private final Map<String, Role> roles = new HashMap<>();
    private final Map<UUID, String> memberRoles = new HashMap<>();
    private final Map<UUID, Set<String>> allyFlags = new HashMap<>();
    private final Map<String, Warp> warps = new HashMap<>();
    private String claimColor; // Hex color string, e.g. "#00FF00", nullable (falls back to default)
    private String visualizationMode = "DISPLAY_ENTITY"; // "DISPLAY_ENTITY" or "PARTICLE"

    // Title settings
    private boolean enterTitleEnabled = false;
    private String enterTitle = "<gold>Entering <owner>'s Claim";
    private String enterTitleMode = "TITLE"; // "TITLE" or "SUBTITLE"
    private String leaveTitle = "<yellow>Leaving <owner>'s Claim";
    private String leaveTitleMode = "SUBTITLE"; // "TITLE" or "SUBTITLE"

    // PvP settings
    private boolean pvpEnabled = false;
    private long pvpTimerEnd = 0L;

    public ClaimProfile(UUID profileId, UUID realOwnerId, String name) {
        this.profileId = profileId;
        this.realOwnerId = realOwnerId;
        this.name = name;
        setupDefaultRoles();
    }

    public ClaimProfile(UUID ownerId, String name) {
        // Backwards compatibility for old constructor
        this.profileId = ownerId;
        this.realOwnerId = ownerId;
        this.name = name;
        setupDefaultRoles();
    }

    private void setupDefaultRoles() {
        // Default Member Role (Basic Interact)
        Role memberRole = new Role(UUID.randomUUID(), this.profileId, "Member", 100);
        memberRole.addFlag("USE_DOORS");
        memberRole.addFlag("USE_TRAPDOORS");
        memberRole.addFlag("USE_FENCE_GATES");
        memberRole.addFlag("USE_CONTAINERS");
        memberRole.addFlag("USE_WORKSTATIONS");
        memberRole.addFlag("USE_BEDS");
        memberRole.addFlag("USE_REDSTONE");
        this.roles.put(memberRole.getName().toLowerCase(), memberRole);

        // Default CoOwner Role (All Permissions)
        Role coOwnerRole = new Role(UUID.randomUUID(), this.profileId, "CoOwner", 10);
        coOwnerRole.addFlag("USE_DOORS");
        coOwnerRole.addFlag("USE_TRAPDOORS");
        coOwnerRole.addFlag("USE_FENCE_GATES");
        coOwnerRole.addFlag("USE_CONTAINERS");
        coOwnerRole.addFlag("USE_WORKSTATIONS");
        coOwnerRole.addFlag("USE_BEDS");
        coOwnerRole.addFlag("USE_REDSTONE");
        coOwnerRole.addFlag("MANAGE_MEMBERS");
        coOwnerRole.addFlag("MANAGE_ROLES");
        coOwnerRole.addFlag("MANAGE_SETTINGS");
        coOwnerRole.addFlag("ADMIN_MENU");
        coOwnerRole.addFlag("USE_BUCKETS");
        coOwnerRole.addFlag("TRAMPLE_CROPS");
        coOwnerRole.addFlag("BLOCK_BREAK");
        coOwnerRole.addFlag("BLOCK_PLACE");
        coOwnerRole.addFlag("BLOCK_IGNITE");
        coOwnerRole.addFlag("INTERACT_ENTITIES");
        coOwnerRole.addFlag("HARM_ENTITIES");
        coOwnerRole.addFlag("MANAGE_VEHICLES");
        coOwnerRole.addFlag("WARP_MANAGE");
        coOwnerRole.addFlag("MODIFY_SIGNS");
        coOwnerRole.addFlag("USE_FERTILIZER");
        coOwnerRole.addFlag("USE_LEASHES");
        coOwnerRole.addFlag("INTERACT_VILLAGERS");
        this.roles.put(coOwnerRole.getName().toLowerCase(), coOwnerRole);
    }

    // --- Owner ---
    
    public boolean canManage(org.bukkit.entity.Player player) {
        if (player.hasPermission("landclaim.admin")) return true;
        return isOwner(player.getUniqueId());
    }

    public UUID getProfileId() {
        return profileId;
    }

    public UUID getOwnerId() {
        return realOwnerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.realOwnerId = ownerId;
    }

    public boolean isOwner(UUID playerId) {
        return realOwnerId.equals(playerId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerAlias() {
        return ownerAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
        this.ownerAlias = ownerAlias;
    }

    public String getDisplayOwnerName() {
        if (ownerAlias != null && !ownerAlias.isEmpty()) {
            return ownerAlias;
        }
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(realOwnerId);
        return op.getName() != null ? op.getName() : realOwnerId.toString();
    }

    // --- PvP Settings ---

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public long getPvpTimerEnd() {
        return pvpTimerEnd;
    }

    public void setPvpTimerEnd(long pvpTimerEnd) {
        this.pvpTimerEnd = pvpTimerEnd;
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

    public String getColoredName() {
        if (claimColor == null || claimColor.isEmpty()) {
            return name;
        }
        return "<" + claimColor + ">" + name + "</" + claimColor + ">";
    }

    public String getColoredOwnerName() {
        String ownerName = getDisplayOwnerName();
        if (claimColor == null || claimColor.isEmpty()) {
            return ownerName;
        }
        return "<" + claimColor + ">" + ownerName + "</" + claimColor + ">";
    }

    public String getLegacyColoredName() {
        if (claimColor == null || claimColor.isEmpty()) {
            return name;
        }
        return "&" + claimColor + name;
    }

    public String getLegacyColoredOwnerName() {
        String ownerName = getDisplayOwnerName();
        if (claimColor == null || claimColor.isEmpty()) {
            return ownerName;
        }
        return "&" + claimColor + ownerName;
    }

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

    // --- Title Settings ---

    public boolean isEnterTitleEnabled() {
        return enterTitleEnabled;
    }

    public void setEnterTitleEnabled(boolean enterTitleEnabled) {
        this.enterTitleEnabled = enterTitleEnabled;
    }

    public String getEnterTitle() {
        return enterTitle;
    }

    public void setEnterTitle(String enterTitle) {
        this.enterTitle = enterTitle;
    }

    public String getLeaveTitle() {
        return leaveTitle;
    }

    public void setLeaveTitle(String leaveTitle) {
        this.leaveTitle = leaveTitle;
    }

    public String getEnterTitleMode() {
        return enterTitleMode;
    }

    public void setEnterTitleMode(String enterTitleMode) {
        this.enterTitleMode = enterTitleMode;
    }

    public String getLeaveTitleMode() {
        return leaveTitleMode;
    }

    public void setLeaveTitleMode(String leaveTitleMode) {
        this.leaveTitleMode = leaveTitleMode;
    }
}
