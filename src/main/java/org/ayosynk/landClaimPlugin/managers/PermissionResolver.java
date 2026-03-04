package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.Map;

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
            if (trustedFlags != null && trustedFlags.contains(flag.toUpperCase())) {
                return true;
            }
        }

        // 4. Check if player belongs to an allied claim
        LandClaimPlugin plugin = LandClaimPlugin.getInstance();
        if (plugin != null && plugin.getClaimManager() != null) {
            // Does this player own any claim?
            ClaimProfile playerProfile = plugin.getClaimManager().getProfile(playerId);
            if (playerProfile != null) {
                // Is player's claim an ally of the target profile?
                if (profile.hasAlly(playerProfile.getOwnerId())) {
                    Set<String> allyFlags = profile.getAllyFlags(playerProfile.getOwnerId());
                    if (allyFlags != null && allyFlags.contains(flag.toLowerCase())) {
                        return true;
                    }
                }
            } else {
                // If the player doesn't own a claim, maybe they are a member of an allied
                // claim?
                // Iterating through all allies of the target profile to see if the player is a
                // member.
                for (UUID allyOwnerId : profile.getAllyFlags().keySet()) {
                    ClaimProfile allyProfile = plugin.getClaimManager().getProfile(allyOwnerId);
                    if (allyProfile != null
                            && (allyProfile.isOwner(playerId) || allyProfile.getMemberRole(playerId) != null)) {
                        Set<String> allyFlags = profile.getAllyFlags(allyOwnerId);
                        if (allyFlags != null && allyFlags.contains(flag.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
        }

        // 5. Fall back to visitor flags
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
