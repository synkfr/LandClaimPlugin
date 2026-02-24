package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.managers.HomeManager;
import org.ayosynk.landClaimPlugin.managers.TrustManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.managers.VisualizationManager.VisualizationMode;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.Claim;
import org.ayosynk.landClaimPlugin.gui.MainMenuGUI;
import org.ayosynk.landClaimPlugin.gui.MemberListGUI;
import org.ayosynk.landClaimPlugin.gui.RoleSelectorGUI;
import org.ayosynk.landClaimPlugin.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private final HomeManager homeManager;
    private final Map<UUID, Boolean> autoClaimPlayers = new HashMap<>();
    private final Map<UUID, Boolean> autoUnclaimPlayers = new HashMap<>();
    private final Map<UUID, Long> unstuckCooldowns = new HashMap<>();

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager,
            TrustManager trustManager, ConfigManager configManager,
            VisualizationManager visualizationManager, HomeManager homeManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.visualizationManager = visualizationManager;
        this.homeManager = homeManager;

        loadPlayerData();

        if (plugin.getCommand("claim") != null) {
            plugin.getCommand("claim").setExecutor(this);
        }
        if (plugin.getCommand("unclaim") != null) {
            plugin.getCommand("unclaim").setExecutor(this);
        }
        if (plugin.getCommand("unclaimall") != null) {
            plugin.getCommand("unclaimall").setExecutor(this);
        }

        if (plugin.getCommand("c") != null) {
            plugin.getCommand("c").setExecutor(this);
        }
        if (plugin.getCommand("uc") != null) {
            plugin.getCommand("uc").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        if (cmd.equals("claim") || cmd.equals("c")) {
            handleClaimCommand(player, args);
        } else if (cmd.equals("unclaim") || cmd.equals("unclaimall") || cmd.equals("uc")) {
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
                case "list":
                    handleListCommand(player);
                    break;
                case "sethome":
                    handleSetHomeCommand(player, args);
                    break;
                case "delhome":
                    handleDelHomeCommand(player, args);
                    break;
                case "home":
                    handleHomeCommand(player, args);
                    break;
                case "homes":
                    handleHomesCommand(player);
                    break;
                case "wand":
                    giveWand(player);
                    break;
                case "zone":
                case "subclaim":
                    createSubClaim(player);
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

        Claim claim = claimManager.getClaimAt(pos);
        if (claim == null) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        UUID owner = claim.getOwnerId();
        if (player.getUniqueId().equals(owner)) {
            sendMessage(player, "cannot-unstuck-here");
            return;
        }

        if (trustManager.hasPermission(claim, player.getUniqueId(), "TELEPORT")) {
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
        int startX = origin.getBlockX() >> 4;
        int startZ = origin.getBlockZ() >> 4;

        for (int radius = 1; radius <= 50; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius)
                        continue;

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

        Claim claim = claimManager.getClaimAt(pos);
        if (claim == null) {
            sendMessage(admin, "not-owner");
            return;
        }

        UUID ownerId = claim.getOwnerId();
        String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
        if (ownerName == null)
            ownerName = "Unknown";

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
            sendMessage(admin, "no-claims-found");
        }
    }

    public void showTrustList(Player player) {
        // Trust list is now per-claim, so this command needs to be claim-contextual or
        // removed
        sendMessage(player, "invalid-command");
    }

    private void openTrustMenu(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            sendMessage(player, "not-in-claim");
            return;
        }

        if (!trustManager.canManageTrust(claim, player)) {
            sendMessage(player, "access-denied");
            return;
        }

        MemberListGUI.open(player, claim, plugin);
    }

    private void openVisitorMenu(Player player) {
        // Obsolete command with role system, redirecting to Main Menu
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            sendMessage(player, "not-in-claim");
            return;
        }

        if (!trustManager.canManageTrust(claim, player)) {
            sendMessage(player, "access-denied");
            return;
        }

        MainMenuGUI.open(player, claim, plugin);
    }

    private void showClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        Claim claim = claimManager.getClaimAt(pos);
        if (claim == null) {
            sendMessage(player, "claim-info-not-claimed");
            return;
        }

        UUID ownerId = claim.getOwnerId();
        String ownerName = Bukkit.getOfflinePlayer(ownerId).getName();
        if (ownerName == null)
            ownerName = "Unknown";

        sendMessage(player, "claim-info-owner", "{owner}", ownerName);

        // Replace trust query with check into claim.getPlayerRoles()
        if (!claim.getPlayerRoles().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (Map.Entry<UUID, String> entry : claim.getPlayerRoles().entrySet()) {
                String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                if (name != null) {
                    names.add(name + " (" + entry.getValue() + ")");
                }
            }
            player.sendMessage(configManager.getMessage("claim-info-trusted", "{players}", String.join(", ", names)));
        }

        if (trustManager.canManageTrust(claim, player) || player.hasPermission("landclaim.admin")) {
            player.sendMessage(configManager.getMessage("claim-info-manage"));
        }
    }

    private void handleMemberCommand(Player player, String[] args) {
        openTrustMenu(player);
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
        org.ayosynk.landClaimPlugin.models.ChunkSelection selection = claimManager.getSelection(player.getUniqueId());
        Set<ChunkPosition> chunksToClaim = selection.getSelectedChunks();

        // If they have no valid selection, just claim the chunk they're standing in
        if (chunksToClaim.isEmpty()) {
            Chunk chunk = player.getLocation().getChunk();
            if (claimManager.claimChunk(player, chunk)) {
                sendMessage(player, "chunk-claimed");
            }
        } else {
            // They have a selection. Try to claim all of them as one claim!
            int claimed = plugin.getClaimManager().claimChunks(player, chunksToClaim);
            if (claimed > 0) {
                player.sendMessage(ChatUtils.parse("<green>Successfully claimed " + claimed + " chunks!"));
                claimManager.clearSelection(player.getUniqueId());
            } else {
                player.sendMessage(ChatUtils.parse("<red>Failed to claim chunks. Limit reached or already claimed."));
            }
        }
    }

    private void giveWand(Player player) {
        String materialName = configManager.getPluginConfig().claimWandItem;
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            material = Material.GOLDEN_SHOVEL;

        org.bukkit.inventory.ItemStack wand = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(net.md_5.bungee.api.ChatColor.GOLD + "Claim Wand");
            meta.setLore(List.of(
                    net.md_5.bungee.api.ChatColor.GRAY + "Left-Click to set Position 1",
                    net.md_5.bungee.api.ChatColor.GRAY + "Right-Click to set Position 2"));
            wand.setItemMeta(meta);
        }

        player.getInventory().addItem(wand);
        player.sendMessage(ChatUtils.parse(configManager.getMessage("wandGiven")));
    }

    private void createSubClaim(Player player) {
        org.ayosynk.landClaimPlugin.models.ChunkSelection selection = claimManager.getSelection(player.getUniqueId());
        Set<ChunkPosition> chunksToClaim = selection.getSelectedChunks();

        if (chunksToClaim.isEmpty()) {
            player.sendMessage(ChatUtils.parse("<red>You must make a selection first using the Claim Wand."));
            return;
        }

        Claim parentClaim = null;
        for (ChunkPosition pos : chunksToClaim) {
            Claim claim = claimManager.getClaimAt(pos);
            if (claim == null || !claim.getOwnerId().equals(player.getUniqueId())) {
                player.sendMessage(ChatUtils.parse("<red>All chunks in a sub-claim must be within your own claim."));
                return;
            }
            if (claimManager.getSubClaimAt(pos) != null) {
                player.sendMessage(ChatUtils.parse("<red>A sub-claim already exists here."));
                return;
            }
            if (parentClaim == null) {
                parentClaim = claim;
            } else if (!parentClaim.getId().equals(claim.getId())) {
                player.sendMessage(ChatUtils.parse("<red>A sub-claim cannot overlap multiple different claims."));
                return;
            }
        }

        if (parentClaim == null)
            return;

        Claim subClaim = new Claim(UUID.randomUUID(), player.getUniqueId());
        subClaim.setParentClaimId(parentClaim.getId());

        for (ChunkPosition pos : chunksToClaim) {
            subClaim.addChunk(pos);
        }

        subClaim.setExpireAt(parentClaim.getExpireAt());

        plugin.getCacheManager().getClaimCache().put(subClaim.getId(), subClaim);

        plugin.getDatabaseManager().getClaimDao().saveClaim(subClaim).thenRun(() -> {
            if (plugin.getRedisManager() != null) {
                plugin.getRedisManager().publishUpdate("INVALIDATE_CLAIM", subClaim.getId());
            }
        });

        plugin.getVisualizationManager().invalidateCache(player.getUniqueId());
        plugin.refreshMapHooks();

        player.sendMessage(ChatUtils
                .parse("<green>Successfully created sub-claim/zone with " + chunksToClaim.size() + " chunks!"));
        claimManager.clearSelection(player.getUniqueId());
    }

    private void unclaimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        ChunkPosition pos = new ChunkPosition(chunk);

        Claim claim = claimManager.getSubClaimAt(pos);
        if (claim == null) {
            claim = claimManager.getClaimAt(pos);
        }

        if (claim == null) {
            sendMessage(player, "not-owner");
            return;
        }

        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            sendMessage(player, "not-owner");
            return;
        }

        if (claimManager.unclaimChunk(chunk)) {
            sendMessage(player, "chunk-unclaimed");
        }
    }

    private void toggleAutoClaim(Player player) {
        boolean current = autoClaimPlayers.getOrDefault(player.getUniqueId(),
                configManager.getPluginConfig().autoClaimDefault);
        boolean newValue = !current;
        autoClaimPlayers.put(player.getUniqueId(), newValue);
        sendMessage(player, newValue ? "auto-claim-enabled" : "auto-claim-disabled");
    }

    private void toggleAutoUnclaim(Player player) {
        boolean current = autoUnclaimPlayers.getOrDefault(player.getUniqueId(),
                configManager.getPluginConfig().autoUnclaimDefault);
        boolean newValue = !current;
        autoUnclaimPlayers.put(player.getUniqueId(), newValue);
        sendMessage(player, newValue ? "auto-unclaim-enabled" : "auto-unclaim-disabled");
    }

    private void trustPlayer(Player player, String targetName) {
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            sendMessage(player, "not-in-claim");
            return;
        }

        if (!trustManager.canManageTrust(claim, player)) {
            sendMessage(player, "access-denied");
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sendMessage(player, "player-not-found");
            return;
        }

        RoleSelectorGUI.open(player, claim, target.getUniqueId(), plugin);
    }

    private void untrustPlayer(Player player, String targetName) {
        ChunkPosition pos = new ChunkPosition(player.getLocation());
        Claim claim = claimManager.getClaimAt(pos);

        if (claim == null) {
            sendMessage(player, "not-in-claim");
            return;
        }

        if (!trustManager.canManageTrust(claim, player)) {
            sendMessage(player, "access-denied");
            return;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (trustManager.removeRoleFromPlayer(claim, target.getUniqueId())) {
            sendMessage(player, "role-removed");
        } else {
            sendMessage(player, "member-not-found");
        }
    }

    private void unclaimAll(Player player) {
        UUID playerId = player.getUniqueId();
        int count = claimManager.unclaimAll(playerId);
        if (count > 0) {
            sendMessage(player, "unclaimed-all", "{count}", String.valueOf(count));
            visualizationManager.invalidateCache(playerId);
        } else {
            sendMessage(player, "no-claims-found");
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
                "help-member",
                "help-list",
                "help-sethome",
                "help-delhome",
                "help-home",
                "help-homes"
        };

        for (String key : helpKeys) {
            player.sendMessage(configManager.getRawMessage(key));
        }
    }

    private void handleListCommand(Player player) {
        if (!player.hasPermission("landclaim.list")) {
            sendMessage(player, "access-denied");
            return;
        }

        UUID playerId = player.getUniqueId();
        Set<org.ayosynk.landClaimPlugin.models.Claim> claimObjects = claimManager.getPlayerClaims(playerId);
        Set<ChunkPosition> claims = claimObjects.stream()
                .flatMap(claim -> claim.getChunks().stream())
                .collect(java.util.stream.Collectors.toSet());

        if (claims.isEmpty()) {
            sendMessage(player, "claim-list-empty");
            return;
        }

        Map<String, List<ChunkPosition>> byWorld = new TreeMap<>();
        for (ChunkPosition pos : claims) {
            byWorld.computeIfAbsent(pos.world(), k -> new ArrayList<>()).add(pos);
        }

        sendMessage(player, "claim-list-header");
        for (Map.Entry<String, List<ChunkPosition>> entry : byWorld.entrySet()) {
            player.sendMessage(configManager.getMessage("claim-list-world", "{world}", entry.getKey()));
            for (ChunkPosition pos : entry.getValue()) {
                player.sendMessage(configManager.getMessage("claim-list-entry",
                        "{x}", String.valueOf(pos.x()),
                        "{z}", String.valueOf(pos.z())));
            }
        }
        player.sendMessage(configManager.getMessage("claim-list-total", "{count}", String.valueOf(claims.size())));
    }

    private void handleSetHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("landclaim.sethome")) {
            sendMessage(player, "access-denied");
            return;
        }

        if (args.length < 2) {
            sendMessage(player, "home-usage");
            return;
        }

        String name = args[1];

        if (!name.matches("^[\\p{L}0-9_]{1,16}$")) {
            sendMessage(player, "home-name-invalid");
            return;
        }

        ChunkPosition pos = new ChunkPosition(player.getLocation());
        if (!claimManager.isChunkClaimed(pos) || !claimManager.getChunkOwner(pos).equals(player.getUniqueId())) {
            sendMessage(player, "home-must-be-in-own-claim");
            return;
        }

        UUID playerId = player.getUniqueId();

        boolean isUpdate = homeManager.getHome(playerId, name) != null;

        if (!isUpdate) {
            int limit = homeManager.getHomeLimit(player);
            int current = homeManager.getHomeCount(playerId);
            if (current >= limit) {
                sendMessage(player, "home-limit-reached", "{limit}", String.valueOf(limit));
                return;
            }
        }

        homeManager.setHome(playerId, name, player.getLocation());
        if (plugin.getSaveManager() != null) {
            plugin.getSaveManager().markHomesDirty();
        }
        sendMessage(player, "home-set", "{name}", name);
    }

    private void handleDelHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("landclaim.delhome")) {
            sendMessage(player, "access-denied");
            return;
        }

        if (args.length < 2) {
            sendMessage(player, "home-usage");
            return;
        }

        String name = args[1];
        UUID playerId = player.getUniqueId();

        if (homeManager.deleteHome(playerId, name)) {
            if (plugin.getSaveManager() != null) {
                plugin.getSaveManager().markHomesDirty();
            }
            sendMessage(player, "home-deleted", "{name}", name);
        } else {
            sendMessage(player, "home-not-found", "{name}", name);
        }
    }

    private void handleHomeCommand(Player player, String[] args) {
        if (!player.hasPermission("landclaim.home")) {
            sendMessage(player, "access-denied");
            return;
        }

        if (args.length < 2) {
            sendMessage(player, "home-usage");
            return;
        }

        String name = args[1];
        UUID playerId = player.getUniqueId();
        Location home = homeManager.getHome(playerId, name);

        if (home == null) {
            sendMessage(player, "home-not-found", "{name}", name);
            return;
        }

        player.teleport(home);
        sendMessage(player, "home-teleported", "{name}", name);
    }

    private void handleHomesCommand(Player player) {
        if (!player.hasPermission("landclaim.homes")) {
            sendMessage(player, "access-denied");
            return;
        }

        UUID playerId = player.getUniqueId();
        Map<String, Location> homes = homeManager.getHomes(playerId);

        if (homes.isEmpty()) {
            sendMessage(player, "home-list-empty");
            return;
        }

        sendMessage(player, "home-list-header");
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            Location loc = entry.getValue();
            player.sendMessage(configManager.getMessage("home-list-entry",
                    "{name}", entry.getKey(),
                    "{world}", loc.getWorld().getName(),
                    "{x}", String.valueOf(loc.getBlockX()),
                    "{y}", String.valueOf(loc.getBlockY()),
                    "{z}", String.valueOf(loc.getBlockZ())));
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
                configManager.getPluginConfig().autoClaimDefault);
    }

    public boolean isAutoUnclaimEnabled(UUID playerId) {
        return autoUnclaimPlayers.getOrDefault(playerId,
                configManager.getPluginConfig().autoUnclaimDefault);
    }

    public void cleanupPlayer(UUID playerId) {
        savePlayerState(playerId);
        autoClaimPlayers.remove(playerId);
        autoUnclaimPlayers.remove(playerId);
        unstuckCooldowns.remove(playerId);
    }

    private void loadPlayerData() {
        var playerDataConfig = configManager.getPlayerDataConfig();
        var autoClaimSection = playerDataConfig.getConfigurationSection("auto-claim");
        var autoUnclaimSection = playerDataConfig.getConfigurationSection("auto-unclaim");

        if (autoClaimSection != null) {
            for (String uuidStr : autoClaimSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    autoClaimPlayers.put(playerId, autoClaimSection.getBoolean(uuidStr));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        if (autoUnclaimSection != null) {
            for (String uuidStr : autoUnclaimSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    autoUnclaimPlayers.put(playerId, autoUnclaimSection.getBoolean(uuidStr));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * Save a single player's state to playerdata.yml
     */
    private void savePlayerState(UUID playerId) {
        var playerDataConfig = configManager.getPlayerDataConfig();
        String uuidStr = playerId.toString();

        if (autoClaimPlayers.containsKey(playerId)) {
            playerDataConfig.set("auto-claim." + uuidStr, autoClaimPlayers.get(playerId));
        }
        if (autoUnclaimPlayers.containsKey(playerId)) {
            playerDataConfig.set("auto-unclaim." + uuidStr, autoUnclaimPlayers.get(playerId));
        }

        configManager.savePlayerData();
    }

    public void saveAllPlayerData() {
        var playerDataConfig = configManager.getPlayerDataConfig();

        for (Map.Entry<UUID, Boolean> entry : autoClaimPlayers.entrySet()) {
            playerDataConfig.set("auto-claim." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<UUID, Boolean> entry : autoUnclaimPlayers.entrySet()) {
            playerDataConfig.set("auto-unclaim." + entry.getKey().toString(), entry.getValue());
        }

        configManager.savePlayerData();
    }
}