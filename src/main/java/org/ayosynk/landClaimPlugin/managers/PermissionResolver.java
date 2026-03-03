package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;

import java.util.Set;
import java.util.UUID;

/**
 * Central permission resolver implementing the 4-tier priority chain:
 * Owner > Role (Member/CoOwner/Custom) > Trusted > Visitor
 *
 * No merging — the first matching tier decides.
 */
public class PermissionResolver {

    /**
     * Check if a player has a specific permission flag in a profile.
     */
    public static boolean hasPermission(ClaimProfile profile, UUID playerId, String flag) {
        if (profile == null)
            return true; // unclaimed land — allow
        if (playerId == null)
            return false;

        // 1. Owner always passes
        if (profile.isOwner(playerId))
            return true;

        // 2. Check if player has a role (Member/CoOwner/Custom)
        String roleName = profile.getMemberRole(playerId);
        if (roleName != null) {
            Role role = profile.getRoleByName(roleName);
            return role != null && role.hasFlag(flag);
        }

        // 3. Check if player is trusted (per-player override)
        if (profile.isTrusted(playerId)) {
            Set<String> trustedFlags = profile.getTrustedFlags(playerId);
            return trustedFlags != null && trustedFlags.contains(flag.toUpperCase());
        }

        // 4. Fall back to visitor flags
        return profile.hasVisitorFlag(flag);
    }

    /**
     * Determine the player's status in a profile for display purposes.
     */
    public static String getPlayerStatus(ClaimProfile profile, UUID playerId) {
        if (profile == null)
            return "wilderness";
        if (profile.isOwner(playerId))
            return "owner";
        if (profile.getMemberRole(playerId) != null)
            return "member";
        if (profile.isTrusted(playerId))
            return "trusted";
        return "visitor";
    }
}
