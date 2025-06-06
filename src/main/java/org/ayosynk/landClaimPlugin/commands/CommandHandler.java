package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final Map<UUID, Boolean> autoClaimPlayers = new HashMap<>();
    private final Map<UUID, Boolean> autoUnclaimPlayers = new HashMap<>();
    private final Map<UUID, Long> unstuckCooldowns = new HashMap<>();

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager, TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;

        // Safe command registration
        if (plugin.getCommand("claim") != null) {
            plugin.getCommand("claim").setExecutor(this);
        }
        if (plugin.getCommand("unclaim") != null) {
            plugin.getCommand("unclaim").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("claim")) {
            handleClaimCommand(player, args);
        } else if (cmd.equals("unclaim")) {
            handleUnclaimCommand(player, args);
        }

        return true;
    }

    private void handleClaimCommand(Player player, String[] args) {
        if (args.length == 0) {
            claimCurrentChunk(player);
        } else {
            switch (args[0].toLowerCase()) {
                case "auto":
                    toggleAutoClaim(player);
                    break;
                case "trust":
                    if (args.length < 2) {
                        sendMessage(player, "trust-usage");
                        break;
                    }
                    trustPlayer(player, args[1]);
                    break;
                case "untrust":
                    if (args.length < 2) {
                        sendMessage(player, "untrust-usage");
                        break;
                    }
                    untrustPlayer(player, args[1]);
                    break;
                case "unstuck":
                    handleUnstuckCommand(player);
                    break;
                case "help":
                    showHelp(player);
                    break;
                case "reload":
                    reloadConfig(player);
                    break;
                default:
                    sendMessage(player, "invalid-command");
            }
        }
    }

    private void handleUnstuckCommand(Player player) {
        UUID playerId = player.getUniqueId();
        int cooldown = configManager.getUnstuckCooldown();

        // Check cooldown
        if (unstuckCooldowns.containsKey(playerId)) {
            long lastUsed = unstuckCooldowns.get(playerId);
            long secondsLeft = cooldown - ((System.currentTimeMillis() - lastUsed) / 1000);

            if (secondsLeft > 0) {
                sendMessage(player, "unstuck-cooldown", "{seconds}", String.valueOf(secondsLeft));
                return;
            }
        }

        Location location = player.getLocation();
        ChunkPosition currentPos = new ChunkPosition(location);

        if (!claimManager.isChunkClaimed(currentPos)) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        UUID owner = claimManager.getChunkOwner(currentPos);
        if (player.getUniqueId().equals(owner)) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        if (trustManager.isTrusted(owner, player)) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        Location safeLocation = findNearestUnclaimed(location);

        if (safeLocation == null) {
            safeLocation = player.getWorld().getSpawnLocation();
        }

        unstuckCooldowns.put(playerId, System.currentTimeMillis());

        player.teleport(safeLocation);
        sendMessage(player, "unstuck-success");
    }

    private Location findNearestUnclaimed(Location origin) {
        World world = origin.getWorld();
        int startX = origin.getBlockX() >> 4; // Convert to chunk coordinates
        int startZ = origin.getBlockZ() >> 4;

        // Search in expanding rings
        for (int radius = 1; radius <= 50; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // Only check perimeter of the ring
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;

                    int chunkX = startX + x;
                    int chunkZ = startZ + z;

                    // Create chunk position
                    ChunkPosition pos = new ChunkPosition(world.getName(), chunkX, chunkZ);

                    // Check if chunk is unclaimed
                    if (!claimManager.isChunkClaimed(pos)) {
                        // Find safe location in chunk
                        return findSafeLocation(world, chunkX, chunkZ);
                    }
                }
            }
        }
        return null;
    }

    private Location findSafeLocation(World world, int chunkX, int chunkZ) {
        // Center of chunk
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;

        // Get the highest safe block
        int y = world.getHighestBlockYAt(centerX, centerZ);
        Location location = new Location(world, centerX, y + 1, centerZ);

        // Check if location is safe (not in water or lava)
        Material blockType = location.getBlock().getType();
        if (blockType == Material.WATER || blockType == Material.LAVA) {
            // Find nearby safe block
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Location testLoc = location.clone().add(x, 0, z);
                    int testY = world.getHighestBlockYAt(testLoc);
                    testLoc.setY(testY + 1);

                    Material testType = testLoc.getBlock().getType();
                    if (testType != Material.WATER && testType != Material.LAVA) {
                        return testLoc;
                    }
                }
            }
        }

        return location;
    }

    private void handleUnclaimCommand(Player player, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("auto")) {
            toggleAutoUnclaim(player);
        } else {
            unclaimCurrentChunk(player);
        }
    }

    private void claimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        if (claimManager.claimChunk(player, chunk)) {
            sendMessage(player, "chunk-claimed");
        }
    }

    private void unclaimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        ChunkPosition pos = new ChunkPosition(chunk);

        if (!claimManager.isChunkClaimed(pos)) {
            sendMessage(player, "not-owner");
            return;
        }

        if (!claimManager.getChunkOwner(pos).equals(player.getUniqueId())) {
            sendMessage(player, "not-owner");
            return;
        }

        if (claimManager.unclaimChunk(chunk)) {
            sendMessage(player, "chunk-unclaimed");
        }
    }

    private void toggleAutoClaim(Player player) {
        boolean current = autoClaimPlayers.getOrDefault(player.getUniqueId(),
                configManager.getConfig().getBoolean("auto-claim-default", false));
        boolean newValue = !current;
        autoClaimPlayers.put(player.getUniqueId(), newValue);
        sendMessage(player, newValue ? "auto-claim-enabled" : "auto-claim-disabled");
    }

    private void toggleAutoUnclaim(Player player) {
        boolean current = autoUnclaimPlayers.getOrDefault(player.getUniqueId(),
                configManager.getConfig().getBoolean("auto-unclaim-default", false));
        boolean newValue = !current;
        autoUnclaimPlayers.put(player.getUniqueId(), newValue);
        sendMessage(player, newValue ? "auto-unclaim-enabled" : "auto-unclaim-disabled");
    }

    private void trustPlayer(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sendMessage(player, "cannot-trust-self");
            return;
        }

        if (trustManager.addTrustedPlayer(player, target)) {
            sendMessage(player, "player-trusted-all", "{player}", target.getName());
            trustManager.saveTrustedPlayers();
        }
    }

    private void untrustPlayer(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }

        if (trustManager.removeTrustedPlayer(player, target)) {
            sendMessage(player, "player-untrusted-all", "{player}", target.getName());
            trustManager.saveTrustedPlayers();
        } else {
            sendMessage(player, "player-not-trusted");
        }
    }

    private void showHelp(Player player) {
        FileConfiguration config = configManager.getConfig();
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-header")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-claim")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-unclaim")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-claim-auto")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-unclaim-auto")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-trust")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-untrust")));
        player.sendMessage(ChatUtils.colorize(config.getString("messages.help-unstuck")));
    }

    private void reloadConfig(Player player) {
        if (!player.hasPermission("landclaim.admin")) {
            sendMessage(player, "access-denied");
            return;
        }

        // Reload main config
        configManager.reloadMainConfig();
        sendMessage(player, "reloaded");
    }

    private void sendMessage(Player player, String key, String... replacements) {
        String message = configManager.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i+1]);
        }
        player.sendMessage(ChatUtils.colorize(message));
    }

    public boolean isAutoClaimEnabled(UUID playerId) {
        return autoClaimPlayers.getOrDefault(playerId,
                configManager.getConfig().getBoolean("auto-claim-default", false));
    }

    public boolean isAutoUnclaimEnabled(UUID playerId) {
        return autoUnclaimPlayers.getOrDefault(playerId,
                configManager.getConfig().getBoolean("auto-unclaim-default", false));
    }
}