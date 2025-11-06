package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.gui.TrustListGUI;
import org.ayosynk.landClaimPlugin.gui.TrustMenuGUI;
import org.ayosynk.landClaimPlugin.gui.VisitorMenuGUI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager.VisualizationMode;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final VisualizationManager visualizationManager;
    private final Map<UUID, Boolean> autoClaimPlayers = new HashMap<>();
    private final Map<UUID, Boolean> autoUnclaimPlayers = new HashMap<>();
    private final Map<UUID, Long> unstuckCooldowns = new HashMap<>();

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager,
                          TrustManager trustManager, ConfigManager configManager,
                          VisualizationManager visualizationManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.visualizationManager = visualizationManager;

        // Safe command registration
        if (plugin.getCommand("claim") != null) {
            plugin.getCommand("claim").setExecutor(this);
        }
        if (plugin.getCommand("unclaim") != null) {
            plugin.getCommand("unclaim").setExecutor(this);
        }
        if (plugin.getCommand("unclaimall") != null) {
            plugin.getCommand("unclaimall").setExecutor(this);
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
        } else if (cmd.equals("unclaim") || cmd.equals("unclaimall")) {
            handleUnclaimCommand(player, args, cmd);
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
                    handleTrustCommand(player, args);
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
                case "visible":
                    handleVisibleCommand(player, args);
                    break;
                case "help":
                    showHelp(player);
                    break;
                case "reload":
                    reloadConfig(player);
                    break;
                case "admin":
                    handleAdminCommand(player, args);
                    break;
                case "trustlist":
                    showTrustList(player);
                    break;
                case "info":
                    showClaimInfo(player);
                    break;
                case "visitor":
                    handleVisitorCommand(player, args);
                    break;
                case "member":
                    handleMemberCommand(player, args);
                    break;
                default:
                    sendMessage(player, "invalid-command");
            }
        }
    }

    private void handleTrustCommand(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "trust-usage");
            return;
        }

        if (args[1].equalsIgnoreCase("menu")) {
            openTrustMenu(player);
        } else {
            trustPlayer(player, args[1]);
        }
    }

    private void handleVisitorCommand(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("menu")) {
            openVisitorMenu(player);
        } else {
            sendMessage(player, "invalid-command");
        }
    }

    private void handleVisibleCommand(Player player, String[] args) {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("always")) {
                visualizationManager.setVisualizationMode(player.getUniqueId(), VisualizationMode.ALWAYS);
                sendMessage(player, "visible-enabled-always");
            } else if (args[1].equalsIgnoreCase("off")) {
                visualizationManager.setVisualizationMode(player.getUniqueId(), null);
                sendMessage(player, "visible-disabled");
            } else {
                sendMessage(player, "invalid-command");
            }
        } else {
            visualizationManager.showTemporary(player);
            sendMessage(player, "visible-enabled-temporary");
        }
    }

    private void handleUnstuckCommand(Player player) {
        UUID playerId = player.getUniqueId();
        int cooldown = configManager.getUnstuckCooldown();
        if (unstuckCooldowns.containsKey(playerId)) {
            long lastUsed = unstuckCooldowns.get(playerId);
            long secondsLeft = cooldown - ((System.currentTimeMillis() - lastUsed) / 1000);

            if (secondsLeft > 0) {
                sendMessage(player, "unstuck-cooldown", "{seconds}", String.valueOf(secondsLeft));
                return;
            }
        }

        Location location = player.getLocation();
        ChunkPosition pos = new ChunkPosition(location);

        if (!claimManager.isChunkClaimed(pos)) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        UUID owner = claimManager.getChunkOwner(pos);
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

        for (int radius = 1; radius <= 50; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) continue;

                    int chunkX = startX + x;
                    int chunkZ = startZ + z;

                    ChunkPosition pos = new ChunkPosition(world.getName(), chunkX, chunkZ);

                    if (!claimManager.isChunkClaimed(pos)) {
                        return findSafeLocation(world, chunkX, chunkZ);
                    }
                }
            }
        }
        return null;
    }

    private Location findSafeLocation(World world, int chunkX, int chunkZ) {
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;

        int y = world.getHighestBlockYAt(centerX, centerZ);
        Location location = new Location(world, centerX, y + 1, centerZ);

        if (location.getBlock().isLiquid()) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Location testLoc = location.clone().add(x, 0, z);
                    int testY = world.getHighestBlockYAt(testLoc);
                    testLoc.setY(testY + 1);

                    if (!testLoc.getBlock().isLiquid()) {
                        return testLoc;
                    }
                }
            }
        }

        return location;
    }

    private void handleAdminCommand(Player player, String[] args) {
        if (!player.hasPermission("landclaim.admin")) {
            sendMessage(player, "access-denied");
            return;
        }

        if (args.length < 2) {
            sendMessage(player, "invalid-command");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "unclaim":
                adminUnclaim(player);
                break;
            case "unclaimall":
                if (args.length < 3) {
                    sendMessage(player, "invalid-command");
                    return;
                }
                adminUnclaimAll(player, args[2]);
                break;
            default:
                sendMessage(player, "invalid-command");
        }
    }

    private void adminUnclaim(Player admin) {
        Chunk chunk = admin.getLocation().getChunk();
        ChunkPosition pos = new ChunkPosition(chunk);

        if (!claimManager.isChunkClaimed(pos)) {
            sendMessage(admin, "not-owner");
            return;
        }

        UUID ownerId = claimManager.getChunkOwner(pos);
        String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
        if (ownerName == null) ownerName = "Unknown";

        if (claimManager.unclaimChunk(chunk)) {
            sendMessage(admin, "admin-unclaimed", "{owner}", ownerName);
            visualizationManager.invalidateCache(ownerId);
        }
    }

    private void adminUnclaimAll(Player admin, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || !target.hasPlayedBefore()) {
            sendMessage(admin, "player-not-found");
            return;
        }

        UUID targetId = target.getUniqueId();
        int count = claimManager.unclaimAll(targetId);
        if (count > 0) {
            sendMessage(admin, "admin-unclaimed-all", "{player}", targetName, "{count}", String.valueOf(count));
            visualizationManager.invalidateCache(targetId);
        } else {
            sendMessage(admin, "&cThat player has no claims.");
        }
    }

    public void showTrustList(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> trusted = trustManager.getTrustedPlayers(playerId);

        if (trusted.isEmpty()) {
            sendMessage(player, "no-trusted-players");
            return;
        }

        sendMessage(player, "trust-list-header");
        for (UUID id : trusted) {
            String name = Bukkit.getOfflinePlayer(id).getName();
            if (name != null) {
                // Create clickable trust entry
                player.spigot().sendMessage(ChatMessageType.CHAT,
                        TextComponent.fromLegacyText(configManager.getMessage(
                                "trust-list-item", "{player}", name
                        )));
            }
        }
        sendMessage(player, "click-to-manage");
    }

    private void openTrustMenu(Player player) {
        TrustListGUI.open(player, trustManager);
    }

    private void openVisitorMenu(Player player) {
        VisitorMenuGUI.open(player, trustManager);
    }

    private void showClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        if (!claimManager.isChunkClaimed(pos)) {
            sendMessage(player, "claim-info-not-claimed");
            return;
        }

        UUID ownerId = claimManager.getChunkOwner(pos);
        String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
        if (ownerName == null) ownerName = "Unknown";

        sendMessage(player, "claim-info-owner", "{owner}", ownerName);

        Set<UUID> trusted = trustManager.getTrustedPlayers(ownerId);
        if (!trusted.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (UUID id : trusted) {
                String name = Bukkit.getOfflinePlayer(id).getName();
                if (name != null) names.add(name);
            }
            player.sendMessage(configManager.getMessage("claim-info-trusted", "{players}", String.join(", ", names)));
        }

        Set<UUID> members = trustManager.getMembers(ownerId);
        if (!members.isEmpty()) {
            List<String> memberNames = new ArrayList<>();
            for (UUID id : members) {
                String name = Bukkit.getOfflinePlayer(id).getName();
                if (name != null) memberNames.add(name);
            }
            player.sendMessage(configManager.getMessage("claim-info-members", "{members}", String.join(", ", memberNames)));
        }
    }

    private void handleMemberCommand(Player player, String[] args) {
        if (args.length < 3) {
            sendMessage(player, "invalid-command");
            return;
        }

        ChunkPosition pos = new ChunkPosition(player.getLocation());
        if (!claimManager.isChunkClaimed(pos)) {
            sendMessage(player, "claim-info-not-claimed");
            return;
        }

        UUID ownerId = claimManager.getChunkOwner(pos);

        if (!player.getUniqueId().equals(ownerId)) {
            sendMessage(player, "only-owner-can-manage");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        if (target == null || !target.hasPlayedBefore()) {
            sendMessage(player, "player-not-found");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                if (trustManager.addMember(ownerId, target)) {
                    sendMessage(player, "member-added", "{player}", target.getName());
                    trustManager.savePermissionsAndMembers();
                }
                break;
            case "remove":
                if (trustManager.removeMember(ownerId, target)) {
                    sendMessage(player, "member-removed", "{player}", target.getName());
                    trustManager.savePermissionsAndMembers();
                } else {
                    sendMessage(player, "not-a-member");
                }
                break;
            default:
                sendMessage(player, "invalid-command");
        }
    }

    private void handleUnclaimCommand(Player player, String[] args, String cmd) {
        if (cmd.equals("unclaimall") || (args.length > 0 && args[0].equalsIgnoreCase("all"))) {
            if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                unclaimAll(player);
            } else {
                sendMessage(player, "confirm-unclaimall");
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("auto")) {
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
        if (player.getName().equalsIgnoreCase(targetName)) {
            sendMessage(player, "cannot-trust-self");
            return;
        }

        if (trustManager.addTrustedPlayer(player, targetName)) {
            sendMessage(player, "player-trusted-all", "{player}", targetName);
            trustManager.saveTrustedPlayers();
        } else {
            sendMessage(player, "player-not-found");
        }
    }

    private void untrustPlayer(Player player, String targetName) {
        if (trustManager.removeTrustedPlayer(player, targetName)) {
            sendMessage(player, "player-untrusted-all", "{player}", targetName);
            trustManager.saveTrustedPlayers();
        } else {
            sendMessage(player, "player-not-trusted");
        }
    }

    private void unclaimAll(Player player) {
        UUID playerId = player.getUniqueId();
        int count = claimManager.unclaimAll(playerId);
        if (count > 0) {
            sendMessage(player, "unclaimed-all", "{count}", String.valueOf(count));
            visualizationManager.invalidateCache(playerId);
        } else {
            sendMessage(player, "&cYou don't have any claims!");
        }
    }

    public void showHelp(Player player) {
        String[] helpKeys = {
                "help-header",
                "help-claim",
                "help-unclaim",
                "help-claim-auto",
                "help-unclaim-auto",
                "help-trust",
                "help-untrust",
                "help-unstuck",
                "help-visible",
                "help-trustlist",
                "help-info",
                "help-admin",
                "help-unclaimall",
                "help-trust-menu",
                "help-visitor-menu",
                "help-member"
        };

        for (String key : helpKeys) {
            player.sendMessage(configManager.getMessage(key));
        }
    }

    private void reloadConfig(Player player) {
        if (!player.hasPermission("landclaim.admin")) {
            sendMessage(player, "access-denied");
            return;
        }

        configManager.reloadMainConfig();
        plugin.reloadConfiguration();
        sendMessage(player, "reloaded");
    }

    private void sendMessage(Player player, String key, String... replacements) {
        player.sendMessage(configManager.getMessage(key, replacements));
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