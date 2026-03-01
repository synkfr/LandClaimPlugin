package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;

/**
 * Handles: /claim trust list/add/remove
 */
public class TrustCommand implements LandClaimCommand {

    private final ConfigManager configManager;

    public TrustCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        Command.Builder<PlayerSource> trustBuilder = claimBuilder.literal("trust");

        // /claim trust list
        manager.command(trustBuilder.literal("list")
                .handler(context -> {
                    Player player = context.sender().source();
                    player.sendMessage(configManager.getMessage("trust-list-stub"));
                }));

        // /claim trust add <player>
        manager.command(trustBuilder.literal("add")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage(configManager.getMessage("trust-added", "<player>", targetName));
                }));

        // /claim trust remove <player>
        manager.command(trustBuilder.literal("remove")
                .required("player", StringParser.stringParser())
                .handler(context -> {
                    Player player = context.sender().source();
                    String targetName = context.get("player");
                    player.sendMessage(configManager.getMessage("trust-removed", "<player>", targetName));
                }));
    }
}
