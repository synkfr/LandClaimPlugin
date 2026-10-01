package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;
import org.ayosynk.landClaimPlugin.models.ChunkPosition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.StringParser;
import org.ayosynk.landClaimPlugin.models.ClaimPlayer;
import org.ayosynk.landClaimPlugin.gui.MainMenuGUI;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.command.ProxiedCommandSender;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles: /claim admin check, /claim admin unclaim, /claim admin add chunk
 * Permission-gated: landclaim.admin
 */
public class AdminCommand implements LandClaimCommand {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;

    public AdminCommand(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
    }

    @Override
    public void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder) {
        register(manager, manager.commandBuilder("claim", "c", "landclaim"), claimBuilder);
    }

    public void register(PaperCommandManager<Source> manager, Command.Builder<Source> claimRoot,
            Command.Builder<PlayerSource> playerClaimBuilder) {
        Command.Builder<Source> adminBase = claimRoot.literal("admin")
                .permission("landclaim.admin");
        Command.Builder<PlayerSource> playerAdminBase = playerClaimBuilder.literal("admin")
                .permission("landclaim.admin");

        // /claim admin check (Player only - requires player location)
        manager.command(playerAdminBase.literal("check")
                .handler(context -> {
                    Player player = context.sender().source();
                    sendAdminClaimInfo(player);
                }));

        // /claim admin unclaim (Player only - requires player location)
        manager.command(playerAdminBase.literal("unclaim")
                .handler(context -> {
                    Player player = context.sender().source();
                    adminUnclaimCurrentChunk(player);
                }));

        // /claim admin add chunk <arg1> [arg2] (Console + Player)
        // Accepts:
        //   /claim admin add chunk <player> <amount> (e.g. @p 5, Notch 5)
        //   /claim admin add chunk <amount> <player> (e.g. 5 @p, 5 Notch)
        //   /claim admin add chunk <amount> (self-grant for in-game players, e.g. 5)
        //   /claim admin add chunk <player> (defaults amount to 1, e.g. @p, Notch)
        // /claim admin add chunk <arg1> [arg2] (Console + Player)
        // Accepts:
        //   /claim admin add chunk <player> <amount> (e.g. @p 5, Notch 5)
        //   /claim admin add chunk <amount> <player> (e.g. 5 @p, 5 Notch)
        //   /claim admin add chunk <amount> (self-grant for in-game players, e.g. 5)
        //   /claim admin add chunk <player> (defaults amount to 1, e.g. @p, Notch)
        manager.command(adminBase.literal("add").literal("chunk")
                .optional("input", StringParser.greedyStringParser(), OfflinePlayerSuggestions.adminChunk())
                .handler(context -> {
                    Source source = context.sender();
                    String input = context.getOrDefault("input", null);
                    handleAdminAddChunk(source, input);
                }));

        manager.command(adminBase.literal("decay").literal("run")
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    plugin.getClaimDecayManager().runDecayCheck(sender);
                }));

        manager.command(adminBase.literal("decay").literal("exempt")
                .optional("input", StringParser.greedyStringParser(), OfflinePlayerSuggestions.decayExempt())
                .handler(context -> {
                    Source source = context.sender();
                    String input = context.getOrDefault("input", null);
                    handleAdminDecayExempt(source, input);
                }));

        manager.command(adminBase.literal("decay").literal("list")
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    adminDecayList(sender);
                }));

        // /claim admin edit <owner> (Player only - opens GUI)
        manager.command(playerAdminBase.literal("edit")
                .required("owner", StringParser.greedyStringParser(), OfflinePlayerSuggestions.withSelectors())
                .handler(context -> {
                    PlayerSource sender = context.sender();
                    String ownerName = context.get("owner");
                    adminEditProfile(sender, ownerName);
                }));

        // /claim admin trust list <owner> (Console + Player)
        manager.command(adminBase.literal("trust").literal("list")
                .required("owner", StringParser.greedyStringParser(), OfflinePlayerSuggestions.withSelectors())
                .handler(context -> {
                    Source sender = context.sender();
                    String ownerName = context.get("owner");
                    adminTrustList(sender, ownerName);
                }));

        // /claim admin trust who <player> (Console + Player)
        manager.command(adminBase.literal("trust").literal("who")
                .required("player", StringParser.greedyStringParser(), OfflinePlayerSuggestions.withSelectors())
                .handler(context -> {
                    Source sender = context.sender();
                    String playerName = context.get("player");
                    adminTrustWho(sender, playerName);
                }));

        // /claim admin setalias <claimName/ownerName> <alias> (Console + Player)
        manager.command(adminBase.literal("setalias")
                .required("claim", StringParser.stringParser())
                .required("alias", StringParser.greedyStringParser())
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    String claimName = context.get("claim");
                    String alias = context.get("alias");
                    adminSetAlias(sender, claimName, alias);
                }));

        // /claim admin claim (Player only - requires player location)
        manager.command(playerAdminBase.literal("claim")
                .handler(context -> {
                    Player player = context.sender().source();
                    org.ayosynk.landClaimPlugin.models.ChunkPosition pos = new org.ayosynk.landClaimPlugin.models.ChunkPosition(player.getLocation());

                    if (claimManager.isChunkClaimed(pos)) {
                        UUID ownerId = claimManager.getChunkOwner(pos);
                        ClaimProfile existingProfile = claimManager.getProfile(ownerId);
                        String ownerName = existingProfile != null ? existingProfile.getDisplayOwnerName() : Bukkit.getOfflinePlayer(ownerId).getName();
                        player.sendMessage(configManager.getMessage("admin-already-claimed", "<owner>", ownerName != null ? ownerName : "Unknown"));
                        return;
                    }

                    // Get or Create Admin Profile
                    ClaimProfile adminProfile = claimManager.getProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID);
                    if (adminProfile == null) {
                        adminProfile = new ClaimProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, "Admin");
                        plugin.getCacheManager().getProfileCache().put(adminProfile.getProfileId(), adminProfile);
                    }

                    adminProfile.addChunk(pos);
                    claimManager.addToSpatialIndex(pos, adminProfile);
                    claimManager.saveAndSync(adminProfile);

                    plugin.getVisualizationManager().invalidateCache(adminProfile.getProfileId());
                    plugin.getHookManager().refreshMapHooks();

                    player.sendMessage(configManager.getMessage("admin-chunk-claimed"));
                }));

        // /claim admin menu (Player only - opens GUI)
        manager.command(playerAdminBase.literal("menu")
                .handler(context -> {
                    Player player = context.sender().source();
                    ClaimProfile adminProfile = claimManager.getProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID);

                    if (adminProfile == null) {
                        adminProfile = new ClaimProfile(org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, org.ayosynk.landClaimPlugin.models.ClaimProfile.ADMIN_PROFILE_ID, "Admin");
                        plugin.getCacheManager().getProfileCache().put(adminProfile.getProfileId(), adminProfile);
                        claimManager.saveAndSync(adminProfile);
                    }

                    final ClaimProfile finalProfile = adminProfile;
                    FoliaScheduler.runForPlayer(plugin, player, () -> MainMenuGUI.open(player, finalProfile, plugin));
                }));

        // /claim admin reload (Console + Player)
        manager.command(adminBase.literal("reload")
                .handler(context -> {
                    CommandSender sender = context.sender().source();
                    plugin.reloadPlugin();
                    sender.sendMessage(configManager.getMessage("reloaded"));
                }));
    }

    private void handleAdminAddChunk(Source source, String input) {
        CommandSender sender = source.source();
        List<String> tokens = tokenize(input);

        if (tokens.isEmpty()) {
            sender.sendMessage(configManager.getMessage("invalid-command"));
            return;
        }

        String targetInput;
        int amount;

        if (tokens.size() == 1) {
            String token = tokens.get(0);
            Integer parsedAmount = tryParsePositiveInt(token);
            if (parsedAmount != null) {
                amount = parsedAmount;
                if (sender instanceof Player player) {
                    targetInput = player.getName();
                } else {
                    sender.sendMessage(configManager.getMessage("player-not-found"));
                    return;
                }
            } else {
                targetInput = token;
                amount = 1;
            }
        } else if (tokens.size() == 2) {
            String token1 = tokens.get(0);
            String token2 = tokens.get(1);
            Integer amountFromToken1 = tryParsePositiveInt(token1);
            Integer amountFromToken2 = tryParsePositiveInt(token2);

            if (amountFromToken2 != null && amountFromToken1 == null) {
                targetInput = token1;
                amount = amountFromToken2;
            } else if (amountFromToken1 != null && amountFromToken2 == null) {
                targetInput = token2;
                amount = amountFromToken1;
            } else if (amountFromToken1 != null && amountFromToken2 != null) {
                targetInput = token1;
                amount = amountFromToken2;
            } else {
                sender.sendMessage(configManager.getMessage("invalid-command"));
                return;
            }
        } else {
            sender.sendMessage(configManager.getMessage("invalid-command"));
            return;
        }

        List<OfflinePlayer> targets = resolveTargets(source, targetInput);
        if (targets.isEmpty()) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        adminAddChunk(sender, amount, targets);
    }

    private void handleAdminDecayExempt(Source source, String input) {
        CommandSender sender = source.source();
        List<String> tokens = tokenize(input);

        if (tokens.isEmpty()) {
            sender.sendMessage(configManager.getMessage("invalid-command"));
            return;
        }

        String target = tokens.get(0);
        Boolean state = null;

        if (tokens.size() == 2) {
            String stateStr = tokens.get(1);
            if (stateStr.equalsIgnoreCase("true")) {
                state = Boolean.TRUE;
            } else if (stateStr.equalsIgnoreCase("false")) {
                state = Boolean.FALSE;
            } else {
                sender.sendMessage(configManager.getMessage("invalid-command"));
                return;
            }
        } else if (tokens.size() > 2) {
            sender.sendMessage(configManager.getMessage("invalid-command"));
            return;
        }

        adminDecayExempt(source, target, state);
    }

    private List<String> tokenize(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inBracket = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '[') {
                inBracket = true;
                current.append(c);
            } else if (c == ']') {
                inBracket = false;
                current.append(c);
            } else if (Character.isWhitespace(c) && !inBracket) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private List<OfflinePlayer> resolveTargets(Source source, String input) {
        CommandSender sender = source != null ? source.source() : null;
        return resolveTargets(source, sender, input);
    }

    private List<OfflinePlayer> resolveTargets(CommandSender sender, String input) {
        return resolveTargets(null, sender, input);
    }

    private List<OfflinePlayer> resolveTargets(Source source, CommandSender sender, String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }

        CommandSourceStack stack = source != null ? source.stack() : null;
        Location location = stack != null ? stack.getLocation() : null;
        Entity executor = stack != null ? stack.getExecutor() : null;

        if (input.startsWith("@")) {
            CommandSender effectiveSender = sender;
            if (executor instanceof Player playerExecutor) {
                effectiveSender = playerExecutor;
            } else if (sender instanceof ProxiedCommandSender pcs && pcs.getCallee() instanceof Player playerCallee) {
                effectiveSender = playerCallee;
            }

            List<OfflinePlayer> selected = selectEntitiesSafely(effectiveSender, input);
            if (!selected.isEmpty()) {
                return selected;
            }

            if (input.equalsIgnoreCase("@s")) {
                if (executor instanceof Player playerExecutor) {
                    return List.of(playerExecutor);
                }
                if (sender instanceof Player p) {
                    return List.of(p);
                }
                if (sender instanceof ProxiedCommandSender pcs && pcs.getCallee() instanceof Player p) {
                    return List.of(p);
                }
                if (location != null && location.getWorld() != null) {
                    Player nearest = findNearestPlayer(location);
                    if (nearest != null) {
                        return List.of(nearest);
                    }
                }
                Player first = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
                return first != null ? List.of(first) : List.of();
            } else if (input.equalsIgnoreCase("@p") || input.equalsIgnoreCase("@nearest")) {
                if (location != null && location.getWorld() != null) {
                    Player nearest = findNearestPlayer(location);
                    if (nearest != null) {
                        return List.of(nearest);
                    }
                }
                if (sender instanceof Player p) {
                    return List.of(p);
                }
                Player first = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
                return first != null ? List.of(first) : List.of();
            } else if (input.equalsIgnoreCase("@a")) {
                return new ArrayList<>(Bukkit.getOnlinePlayers());
            } else if (input.equalsIgnoreCase("@r")) {
                var online = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (!online.isEmpty()) {
                    return List.of(online.get(new Random().nextInt(online.size())));
                }
                return List.of();
            }
        }

        Player onlinePlayer = Bukkit.getPlayerExact(input);
        if (onlinePlayer != null) {
            return List.of(onlinePlayer);
        }

        try {
            UUID uuid = UUID.fromString(input);
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (op.hasPlayedBefore() || op.isOnline()) {
                return List.of(op);
            }
        } catch (IllegalArgumentException ignored) {}

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if (offlinePlayer != null && (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline())) {
            return List.of(offlinePlayer);
        }

        return List.of();
    }

    private List<OfflinePlayer> selectEntitiesSafely(CommandSender sender, String selector) {
        if (sender == null) {
            return List.of();
        }
        if (Bukkit.isPrimaryThread()) {
            return doSelectEntities(sender, selector);
        }
        CompletableFuture<List<OfflinePlayer>> future = new CompletableFuture<>();
        FoliaScheduler.runTask(plugin, () -> {
            try {
                future.complete(doSelectEntities(sender, selector));
            } catch (Throwable t) {
                future.complete(List.of());
            }
        });
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<OfflinePlayer> doSelectEntities(CommandSender sender, String selector) {
        try {
            List<Entity> entities = Bukkit.selectEntities(sender, selector);
            List<OfflinePlayer> players = new ArrayList<>();
            for (Entity entity : entities) {
                if (entity instanceof Player player) {
                    players.add(player);
                }
            }
            return players;
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    private Player findNearestPlayer(Location location) {
        if (location == null || location.getWorld() == null) {
            return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        }
        Player nearest = null;
        double minDistanceSq = Double.MAX_VALUE;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                double distSq = player.getLocation().distanceSquared(location);
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    nearest = player;
                }
            }
        }
        if (nearest != null) {
            return nearest;
        }
        return Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
    }

    private OfflinePlayer resolveSinglePlayer(Source source, String input) {
        List<OfflinePlayer> targets = resolveTargets(source, input);
        return targets.isEmpty() ? null : targets.get(0);
    }

    private OfflinePlayer resolveSinglePlayer(CommandSender sender, String input) {
        List<OfflinePlayer> targets = resolveTargets(sender, input);
        return targets.isEmpty() ? null : targets.get(0);
    }

    private Integer tryParsePositiveInt(String input) {
        if (input == null) return null;
        try {
            int val = Integer.parseInt(input);
            return val > 0 ? val : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void adminAddChunk(CommandSender sender, int amount, List<OfflinePlayer> targets) {
        FoliaScheduler.runAsync(plugin, () -> {
            for (OfflinePlayer target : targets) {
                if (target == null || target.getUniqueId() == null) continue;

                UUID targetId = target.getUniqueId();
                ClaimPlayer claimPlayer = plugin.getCacheManager().getPlayerCache().getIfPresent(targetId);
                boolean inCache = claimPlayer != null;

                if (!inCache) {
                    try {
                        claimPlayer = plugin.getDatabaseManager().getPlayerDao().getPlayer(targetId).join();
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to load player data for admin add chunk: " + e.getMessage());
                        sender.sendMessage(configManager.getMessage("player-not-found"));
                        continue;
                    }
                }

                if (claimPlayer == null) {
                    claimPlayer = new ClaimPlayer(targetId);
                }

                claimPlayer.setBonusClaimBlocks(claimPlayer.getBonusClaimBlocks() + amount);

                // Save to DB
                plugin.getDatabaseManager().getPlayerDao().savePlayer(claimPlayer).join();

                if (inCache) {
                    plugin.getCacheManager().getPlayerCache().put(targetId, claimPlayer);
                }

                sender.sendMessage(configManager.getMessage("admin-add-chunk-success", "<amount>", String.valueOf(amount), "<player>", target.getName() != null ? target.getName() : targetId.toString()));
            }
        });
    }

    private void sendAdminClaimInfo(Player player) {
        ChunkPosition pos = new ChunkPosition(player.getLocation().getChunk());
        ClaimProfile profile = claimManager.getProfileAt(pos);

        if (profile == null) {
            player.sendMessage(configManager.getMessage("not-in-claim"));
            return;
        }
        String ownerName = profile.getDisplayOwnerName();

        player.sendMessage(
                configManager.getMessage("admin-claim-info-owned-by", "<owner>", ownerName, "<uuid>",
                        profile.getProfileId().toString()));
        player.sendMessage(configManager.getMessage("admin-claim-info-id", "<id>", profile.getName()));
    }

    private void adminUnclaimCurrentChunk(Player player) {
        FoliaScheduler.runForPlayer(plugin, player, () -> {
            Chunk chunk = player.getLocation().getChunk();
            ChunkPosition pos = new ChunkPosition(chunk);

            ClaimProfile profile = claimManager.getProfileAt(pos);
            if (profile == null) {
                player.sendMessage(configManager.getMessage("not-in-claim"));
                return;
            }

            if (claimManager.unclaimChunk(chunk)) {
                player.sendMessage(configManager.getMessage("admin-bypassed-unclaim"));
            }
        });
    }

    private void adminEditProfile(Source source, String ownerName) {
        CommandSender sender = source.source();
        if (!(sender instanceof Player player)) {
            return;
        }
        OfflinePlayer target = resolveSinglePlayer(source, ownerName);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        ClaimProfile profile = claimManager.getProfile(target.getUniqueId());
        if (profile == null) {
            player.sendMessage(configManager.getMessage("no-profile-found"));
            return;
        }

        MainMenuGUI.open(player, profile, plugin);
        player.sendMessage(configManager.getMessage("admin-editing-profile", "<player>", target.getName()));
    }

    private void adminEditProfile(Player sender, String ownerName) {
        OfflinePlayer target = resolveSinglePlayer(sender, ownerName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        ClaimProfile profile = claimManager.getProfile(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage(configManager.getMessage("no-profile-found"));
            return;
        }

        MainMenuGUI.open(sender, profile, plugin);
        sender.sendMessage(configManager.getMessage("admin-editing-profile", "<player>", target.getName()));
    }

    private void adminTrustList(Source source, String ownerName) {
        CommandSender sender = source.source();
        OfflinePlayer owner = resolveSinglePlayer(source, ownerName);
        if (owner == null || owner.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        FoliaScheduler.runAsync(plugin, () -> {
            ClaimProfile profile = claimManager.getProfile(owner.getUniqueId());
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            var trusted = profile.getTrustedPlayerFlags();
            if (trusted.isEmpty()) {
                sender.sendMessage(configManager.getMessage("trust-list-empty"));
                return;
            }

            String safeOwnerName = escapeMiniMessage(owner.getName() != null ? owner.getName() : ownerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Trusted players for " + safeOwnerName + ":"));
            for (UUID trustedId : trusted.keySet()) {
                String name = Bukkit.getOfflinePlayer(trustedId).getName();
                if (name == null) name = trustedId.toString();
                String safeName = escapeMiniMessage(name);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gray>- <gold>" + safeName));
            }
        });
    }

    private void adminTrustList(CommandSender sender, String ownerName) {
        OfflinePlayer owner = resolveSinglePlayer(sender, ownerName);
        if (owner == null || owner.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        FoliaScheduler.runAsync(plugin, () -> {
            ClaimProfile profile = claimManager.getProfile(owner.getUniqueId());
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            var trusted = profile.getTrustedPlayerFlags();
            if (trusted.isEmpty()) {
                sender.sendMessage(configManager.getMessage("trust-list-empty"));
                return;
            }

            String safeOwnerName = escapeMiniMessage(owner.getName() != null ? owner.getName() : ownerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Trusted players for " + safeOwnerName + ":"));
            for (UUID trustedId : trusted.keySet()) {
                String name = Bukkit.getOfflinePlayer(trustedId).getName();
                if (name == null) name = trustedId.toString();
                String safeName = escapeMiniMessage(name);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gray>- <gold>" + safeName));
            }
        });
    }

    private void adminTrustWho(Source source, String playerName) {
        CommandSender sender = source.source();
        OfflinePlayer target = resolveSinglePlayer(source, playerName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        UUID targetId = target.getUniqueId();
        FoliaScheduler.runAsync(plugin, () -> {
            boolean foundAny = false;
            String safeTargetName = escapeMiniMessage(target.getName() != null ? target.getName() : playerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Claims where " + safeTargetName + " is trusted:"));

            for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
                if (profile.isTrusted(targetId)) {
                    foundAny = true;
                    String ownerName = profile.getDisplayOwnerName();
                    String safeOwnerName = escapeMiniMessage(ownerName);
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<gray>- <gold>" + safeOwnerName));
                }
            }

            if (!foundAny) {
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>This player is not trusted in any claims."));
            }
        });
    }

    private void adminTrustWho(CommandSender sender, String playerName) {
        OfflinePlayer target = resolveSinglePlayer(sender, playerName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        UUID targetId = target.getUniqueId();
        FoliaScheduler.runAsync(plugin, () -> {
            boolean foundAny = false;
            String safeTargetName = escapeMiniMessage(target.getName() != null ? target.getName() : playerName);
            sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<gold>Claims where " + safeTargetName + " is trusted:"));

            for (ClaimProfile profile : plugin.getCacheManager().getProfileCache().asMap().values()) {
                if (profile.isTrusted(targetId)) {
                    foundAny = true;
                    String ownerName = profile.getDisplayOwnerName();
                    String safeOwnerName = escapeMiniMessage(ownerName);
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<gray>- <gold>" + safeOwnerName));
                }
            }

            if (!foundAny) {
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>This player is not trusted in any claims."));
            }
        });
    }

    private void adminDecayExempt(Source source, String playerName, Boolean explicitState) {
        CommandSender sender = source.source();
        List<OfflinePlayer> targets = resolveTargets(source, playerName);
        if (targets.isEmpty()) {
            sender.sendMessage(configManager.getMessage("player-not-found"));
            return;
        }

        for (OfflinePlayer target : targets) {
            if (target == null || target.getUniqueId() == null) continue;
            UUID targetId = target.getUniqueId();
            FoliaScheduler.runAsync(plugin, () -> {
                boolean currentState = plugin.getClaimDecayManager().isExempt(targetId, null);
                boolean newState = explicitState != null ? explicitState : !currentState;
                plugin.getClaimDecayManager().setExempt(targetId, newState);

                String targetDisplayName = target.getName() != null ? target.getName() : targetId.toString();
                String statusStr = newState ? "EXEMPT" : "NOT EXEMPT";
                sender.sendMessage(configManager.getMessage("admin-decay-exempt-set",
                        "<player>", targetDisplayName,
                        "<status>", statusStr));
            });
        }
    }

    private void adminDecayList(CommandSender sender) {
        FoliaScheduler.runAsync(plugin, () -> {
            try {
                List<UUID> exemptIds = plugin.getDatabaseManager().getPlayerDao().getExemptPlayerIds().join();
                if (exemptIds.isEmpty()) {
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<yellow>No players are currently manually exempt from claim decay."));
                    return;
                }

                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<gold>Decay-Exempt Players (" + exemptIds.size() + "):"));
                for (UUID id : exemptIds) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                    String name = op.getName() != null ? op.getName() : id.toString();
                    String safeName = escapeMiniMessage(name);
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize("<gray>- <gold>" + safeName + " <dark_gray>(" + id + ")"));
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to retrieve exempt players list: " + e.getMessage());
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>Failed to retrieve exempt players list."));
            }
        });
    }

    private void adminSetAlias(CommandSender sender, String claimName, String alias) {
        FoliaScheduler.runAsync(plugin, () -> {
            ClaimProfile profile = claimManager.getProfileByNameOrOwner(claimName);
            if (profile == null) {
                sender.sendMessage(configManager.getMessage("no-profile-found"));
                return;
            }

            String safeClaimName = escapeMiniMessage(profile.getName());
            if (alias.equalsIgnoreCase("reset") || alias.equalsIgnoreCase("none")) {
                profile.setOwnerAlias(null);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<green>Owner alias for claim '<white>" + safeClaimName + "<green>' has been reset."));
            } else {
                profile.setOwnerAlias(alias);
                String safeAlias = escapeMiniMessage(alias);
                sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<green>Owner alias for claim '<white>" + safeClaimName + "<green>' set to: <white>" + safeAlias));
            }

            plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
        });
    }

    /**
     * Escape MiniMessage special characters to prevent injection attacks.
     */
    private String escapeMiniMessage(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("<", "\\<")
                    .replace(">", "\\>");
    }
}
