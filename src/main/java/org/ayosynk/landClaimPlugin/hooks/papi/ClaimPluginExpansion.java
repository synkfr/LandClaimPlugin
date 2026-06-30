package org.ayosynk.landClaimPlugin.hooks.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimPluginExpansion extends PlaceholderExpansion {

    private final LandClaimPlugin plugin;

    public ClaimPluginExpansion(LandClaimPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "claimplugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return "synk";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";
        UUID playerId = offlinePlayer.getUniqueId();
        Player player = offlinePlayer.getPlayer();

        // 1. Claims
        if (params.equalsIgnoreCase("claims_current")) {
            return String.valueOf(plugin.getClaimManager().getTotalClaimedChunks(playerId));
        }

        if (params.equalsIgnoreCase("claims_max")) {
            if (player != null) {
                return String.valueOf(plugin.getClaimManager().getClaimLimit(player));
            }
            return String.valueOf(plugin.getClaimManager().getClaimLimit(playerId));
        }

        if (params.equalsIgnoreCase("cost_next_claim")) {
            try {
                Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
                if (ecoPlugin != null && ecoPlugin.isEnabled()) {
                    Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
                    Object claimCostFeature = config.getClass().getField("claimCost").get(config);
                    boolean claimCostEnabled = (boolean) claimCostFeature.getClass().getField("enabled").get(claimCostFeature);
                    if (claimCostEnabled) {
                        double cost;
                        boolean firstClaimFree = (boolean) config.getClass().getField("firstClaimFree").get(config);
                        int totalChunks = plugin.getClaimManager().getTotalClaimedChunks(playerId);
                        if (firstClaimFree && totalChunks == 0) {
                            cost = 0;
                        } else {
                            boolean canCreate = plugin.getClaimManager().canCreateProfile(playerId);
                            double firstChunkCost = (double) config.getClass().getField("firstChunkCost").get(config);
                            double perChunkCost = (double) config.getClass().getField("perChunkCost").get(config);
                            cost = !canCreate ? firstChunkCost : perChunkCost;
                        }
                        Class<?> ecoHookClass = Class.forName("org.ayosynk.landclaimeconomy.util.EconomyHook");
                        return (String) ecoHookClass.getMethod("format", double.class).invoke(null, cost);
                    }
                }
            } catch (Exception ignored) {}
            return "$0.00";
        }

        // 2. Roles
        if (params.equalsIgnoreCase("roles_current")) {
            if (player != null) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(player);
                if (profile != null) {
                    return String.valueOf(Math.max(0, profile.getRoles().size() - 2));
                }
            }
            return "0";
        }

        if (params.equalsIgnoreCase("roles_max")) {
            if (player != null) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(player);
                if (profile != null) {
                    int maxRoles = 2;
                    for (org.bukkit.permissions.PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                        if (pai.getPermission().startsWith("landclaim.createrole.")) {
                            try {
                                int limit = Integer.parseInt(pai.getPermission().substring("landclaim.createrole.".length()));
                                if (limit + 2 > maxRoles) {
                                    maxRoles = limit + 2;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    return String.valueOf(maxRoles - 2 + profile.getBonusRoleSlots());
                }
            }
            return "0";
        }

        if (params.equalsIgnoreCase("cost_next_role_slot")) {
            try {
                Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
                if (ecoPlugin != null && ecoPlugin.isEnabled()) {
                    Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
                    double cost = (double) config.getClass().getField("roleSlotCost").get(config);
                    Class<?> ecoHookClass = Class.forName("org.ayosynk.landclaimeconomy.util.EconomyHook");
                    return (String) ecoHookClass.getMethod("format", double.class).invoke(null, cost);
                }
            } catch (Exception ignored) {}
            return "$0.00";
        }

        // 3. Members
        if (params.equalsIgnoreCase("members_current")) {
            if (player != null) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(player);
                if (profile != null) {
                    return String.valueOf(profile.getMemberRoles().size());
                }
            }
            return "0";
        }

        if (params.equalsIgnoreCase("members_max")) {
            if (player != null) {
                ClaimProfile profile = plugin.getClaimManager().getActiveProfile(player);
                if (profile != null) {
                    return String.valueOf(plugin.getConfigManager().getPluginConfig().maxClaimMembers + profile.getBonusMemberSlots());
                }
            }
            return "0";
        }

        if (params.equalsIgnoreCase("cost_next_member_slot")) {
            try {
                Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
                if (ecoPlugin != null && ecoPlugin.isEnabled()) {
                    Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
                    double cost = (double) config.getClass().getField("memberSlotCost").get(config);
                    Class<?> ecoHookClass = Class.forName("org.ayosynk.landclaimeconomy.util.EconomyHook");
                    return (String) ecoHookClass.getMethod("format", double.class).invoke(null, cost);
                }
            } catch (Exception ignored) {}
            return "$0.00";
        }

        // 4. Warps
        if (params.equalsIgnoreCase("warps_current")) {
            return String.valueOf(plugin.getWarpManager().getWarpCount(playerId));
        }

        if (params.equalsIgnoreCase("warps_max")) {
            if (player != null) {
                return String.valueOf(plugin.getWarpManager().getWarpLimit(player));
            }
            return "0";
        }

        if (params.equalsIgnoreCase("cost_next_warp_slot")) {
            try {
                Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
                if (ecoPlugin != null && ecoPlugin.isEnabled()) {
                    Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
                    double cost = (double) config.getClass().getField("warpSlotCost").get(config);
                    Class<?> ecoHookClass = Class.forName("org.ayosynk.landclaimeconomy.util.EconomyHook");
                    return (String) ecoHookClass.getMethod("format", double.class).invoke(null, cost);
                }
            } catch (Exception ignored) {}
            return "$0.00";
        }

        return null;
    }
}
