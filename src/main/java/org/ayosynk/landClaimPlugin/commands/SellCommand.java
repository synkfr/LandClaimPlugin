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

public class SellCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public SellCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        manager.command(claimBuilder.literal("sell")
                .required("profile", org.incendo.cloud.parser.standard.StringParser.stringParser())
                .required("price", org.incendo.cloud.parser.standard.DoubleParser.doubleParser(0.01))
                .handler(context -> {
                    Player player = context.sender().source();
                    String profileName = context.get("profile");
                    double price = context.get("price");

                    // 1. Find profile by name and check ownership
                    ClaimProfile targetProfile = null;
                    for (ClaimProfile cp : claimManager.getAllProfiles()) {
                        if (cp.isOwner(player.getUniqueId()) && cp.getName().equalsIgnoreCase(profileName)) {
                            targetProfile = cp;
                            break;
                        }
                    }

                    if (targetProfile == null) {
                        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                                .deserialize("<red>Could not find a claim profile owned by you named: " + profileName));
                        return;
                    }

                    ClaimProfile activeProfile = claimManager.getActiveProfile(player);
                    boolean isActive = activeProfile != null && activeProfile.getProfileId().equals(targetProfile.getProfileId());

                    final ClaimProfile profileToSell = targetProfile;

                    if (isActive) {
                        // Prompt for confirmation using ConfirmationGUI
                        org.ayosynk.landClaimPlugin.gui.ConfirmationGUI.open(player, "§cSell Active Profile?",
                                () -> executeSell(player, profileToSell, price),
                                () -> player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<gray>Listing cancelled."))
                        );
                    } else {
                        executeSell(player, profileToSell, price);
                    }
                }));
    }

    private void executeSell(Player player, ClaimProfile profile, double price) {
        try {
            Plugin ecoPlugin = Bukkit.getPluginManager().getPlugin("LandClaimPlugin-Economy");
            if (ecoPlugin == null || !ecoPlugin.isEnabled()) {
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>Economy features are disabled."));
                return;
            }

            Object marketManager = ecoPlugin.getClass().getMethod("getMarketManager").invoke(ecoPlugin);
            Method listMethod = marketManager.getClass().getMethod("list", Player.class, ClaimProfile.class, double.class);
            listMethod.invoke(marketManager, player, profile, price);
        } catch (Exception e) {
            plugin.getLogger().severe("Error listing profile for sale: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>An error occurred while listing the claim."));
        }
    }
}
