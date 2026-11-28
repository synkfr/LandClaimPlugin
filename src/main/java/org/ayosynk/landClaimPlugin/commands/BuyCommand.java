package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

import java.lang.reflect.Method;

public class BuyCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public BuyCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> buyBuilder = claimBuilder.literal("buy");

        // /claim buy claim [amount]
        manager.command(buyBuilder.literal("claim")
                .optional("amount", org.incendo.cloud.parser.standard.IntegerParser.integerParser(1))
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!isLimitPurchasesEnabled()) {
                        player.sendMessage(configManager.getMessage("limit-purchases-disabled"));
                        return;
                    }
                    int amount = context.getOrDefault("amount", 1);
                    if (amount <= 0) {
                        player.sendMessage(configManager.getMessage("economy.invalid-amount"));
                        return;
                    }
                    org.ayosynk.landClaimPlugin.models.ClaimPlayer cp = plugin.getCacheManager().getPlayerCache().getIfPresent(player.getUniqueId());
                    int currentBonus = cp != null ? cp.getBonusClaimBlocks() : 0;
                    int maxBonus = plugin.getConfigManager().getPluginConfig().limits.maxBonusClaimBlocks;
                    if (maxBonus > 0 && (long) currentBonus + amount > maxBonus) {
                        int remaining = Math.max(0, maxBonus - currentBonus);
                        player.sendMessage(configManager.getMessage("max-bonus-claim-blocks-reached",
                                "<amount>", String.valueOf(amount),
                                "<max>", String.valueOf(maxBonus),
                                "<remaining>", String.valueOf(remaining)));
                        return;
                    }
                    int maxTotal = plugin.getConfigManager().getPluginConfig().limits.maxTotalClaimLimit;
                    if (maxTotal > 0) {
                        int currentLimit = plugin.getClaimManager().getClaimLimit(player);
                        if ((long) currentLimit + amount > maxTotal) {
                            int remaining = Math.max(0, maxTotal - currentLimit);
                            player.sendMessage(configManager.getMessage("max-total-claim-limit-reached",
                                    "<amount>", String.valueOf(amount),
                                    "<max>", String.valueOf(maxTotal),
                                    "<remaining>", String.valueOf(remaining)));
                            return;
                        }
                    }
                    double costPer = getCost("claimBlockCost", 50.0);
                    double totalCost = costPer * amount;
                    if (!chargePlayer(player, totalCost, "BUY_CLAIM_BLOCKS")) {
                        return;
                    }

                    plugin.addBonusBlocks(player.getUniqueId(), amount).thenAccept(newTotal -> {
                        player.sendMessage(configManager.getMessage("economy.bought-claim-blocks",
                                "<amount>", String.valueOf(amount),
                                "<limit>", String.valueOf(plugin.getClaimManager().getClaimLimit(player))));
                    });
                }));

        // /claim buy role
        manager.command(buyBuilder.literal("role")
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!isLimitPurchasesEnabled()) {
                        player.sendMessage(configManager.getMessage("limit-purchases-disabled"));
                        return;
                    }
                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }
                    double cost = getCost("roleSlotCost", 150.0);
                    if (!chargePlayer(player, cost, "BUY_ROLE_SLOT")) {
                        return;
                    }
                    profile.setBonusRoleSlots(profile.getBonusRoleSlots() + 1);
                    claimManager.saveAndSync(profile);
                    player.sendMessage(configManager.getMessage("economy.bought-role-slot",
                            "<total>", String.valueOf(profile.getBonusRoleSlots())));
                }));

        // /claim buy member
        manager.command(buyBuilder.literal("member")
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!isLimitPurchasesEnabled()) {
                        player.sendMessage(configManager.getMessage("limit-purchases-disabled"));
                        return;
                    }
                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }
                    double cost = getCost("memberSlotCost", 50.0);
                    if (!chargePlayer(player, cost, "BUY_MEMBER_SLOT")) {
                        return;
                    }
                    profile.setBonusMemberSlots(profile.getBonusMemberSlots() + 1);
                    claimManager.saveAndSync(profile);
                    player.sendMessage(configManager.getMessage("economy.bought-member-slot",
                            "<total>", String.valueOf(profile.getBonusMemberSlots())));
                }));

        // /claim buy warp
        manager.command(buyBuilder.literal("warp")
                .handler(context -> {
                    Player player = context.sender().source();
                    if (!isLimitPurchasesEnabled()) {
                        player.sendMessage(configManager.getMessage("limit-purchases-disabled"));
                        return;
                    }
                    ClaimProfile profile = claimManager.getActiveProfile(player);
                    if (profile == null) {
                        player.sendMessage(configManager.getMessage("no-profile"));
                        return;
                    }
                    if (!profile.isOwner(player.getUniqueId())) {
                        player.sendMessage(configManager.getMessage("not-owner"));
                        return;
                    }
                    double cost = getCost("warpSlotCost", 75.0);
                    if (!chargePlayer(player, cost, "BUY_WARP_SLOT")) {
                        return;
                    }
                    profile.setBonusWarpSlots(profile.getBonusWarpSlots() + 1);
                    claimManager.saveAndSync(profile);
                    player.sendMessage(configManager.getMessage("economy.bought-warp-slot",
                            "<total>", String.valueOf(profile.getBonusWarpSlots())));
                }));
    }

    private boolean isLimitPurchasesEnabled() {
        try {
            Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
            if (ecoPlugin == null || !ecoPlugin.isEnabled()) {
                return false;
            }
            Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
            Object limitPurchases = config.getClass().getField("limitPurchases").get(config);
            return (boolean) limitPurchases.getClass().getField("enabled").get(limitPurchases);
        } catch (Exception e) {
            return false;
        }
    }

    private double getCost(String fieldName, double defaultValue) {
        try {
            Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
            if (ecoPlugin != null && ecoPlugin.isEnabled()) {
                Object config = ecoPlugin.getClass().getMethod("getEconomyConfig").invoke(ecoPlugin);
                return (double) config.getClass().getField(fieldName).get(config);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resolve economy cost setting " + fieldName + ": " + e.getMessage());
        }
        return defaultValue;
    }

    private boolean chargePlayer(Player player, double cost, String actionType) {
        try {
            Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
            if (ecoPlugin == null || !ecoPlugin.isEnabled()) {
                player.sendMessage(configManager.getMessage("economy.disabled"));
                return false;
            }
            Class<?> ecoHookClass = Class.forName("org.ayosynk.landclaimeconomy.util.EconomyHook");
            Method formatMethod = ecoHookClass.getMethod("format", double.class);

            // Check balance
            double balance = (double) ecoHookClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class).invoke(null, player);
            if (balance < cost) {
                String costStr = (String) formatMethod.invoke(null, cost);
                String balStr = (String) formatMethod.invoke(null, balance);
                player.sendMessage(configManager.getMessage("economy.insufficient-funds",
                        "<cost>", costStr,
                        "<balance>", balStr));
                return false;
            }

            // Withdraw
            boolean success = (boolean) ecoHookClass.getMethod("withdraw", org.bukkit.OfflinePlayer.class, double.class).invoke(null, player, cost);
            if (!success) {
                player.sendMessage(configManager.getMessage("economy.transaction-failed"));
                return false;
            }

            // Log transaction in database
            Object database = ecoPlugin.getClass().getMethod("getDatabase").invoke(ecoPlugin);
            Method logMethod = database.getClass().getMethod("logTransaction", String.class, String.class, String.class, double.class, String.class);
            logMethod.invoke(database, player.getUniqueId().toString(), null, actionType, cost, null);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error charging player: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(configManager.getMessage("economy.purchase-error"));
            return false;
        }
    }
}
