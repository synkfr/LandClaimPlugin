package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.exceptions.CombatBlockedException;
import org.ayosynk.landClaimPlugin.gui.*;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;

import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.managers.WarpManager;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.exception.handling.ExceptionContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Central command registry and coordinator.
 *
 * Uses a custom ExecutionCoordinator.builder() with per-phase thread isolation:
 * - Parsing: nonSchedulingExecutor (calling thread) — lightweight tokenization
 * - Suggestions: nonSchedulingExecutor (calling thread) — prevents deadlocks
 * - Execution: dedicated daemon thread pool — heavy I/O, DB, GUI building off
 * main thread
 *
 * Each command group is a separate LandClaimCommand implementation registered
 * modularly.
 */
public class CommandHandler {

    private final ClaimCommand claimCommand;
    private final ExecutorService commandExecutor;

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager,
            VisualizationManager visualizationManager, WarpManager warpManager) {

        // Dedicated thread pool for async command execution (4 daemon threads)
        commandExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "LandClaim-Command-Worker");
            t.setDaemon(true);
            return t;
        });

        // Custom ExecutionCoordinator — the critical optimization
        // Parsing & suggestions stay on calling thread (prevents deadlocks)
        // Execution routes to our dedicated worker pool (off main thread)
        ExecutionCoordinator<Source> coordinator = ExecutionCoordinator.<Source>builder()
                .parsingExecutor(ExecutionCoordinator.nonSchedulingExecutor())
                .suggestionsExecutor(ExecutionCoordinator.nonSchedulingExecutor())
                .executionSchedulingExecutor(commandExecutor)
                .build();

        PaperCommandManager<Source> commandManager;
        try {
            commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                    .executionCoordinator(coordinator)
                    .buildOnEnable(plugin);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Cloud Command Manager: " + e.getMessage());
            claimCommand = null;
            throw new RuntimeException("Failed to initialize Cloud Command Manager", e);
        }

        // Combat preprocessor — shared across all commands
        commandManager.registerCommandPreProcessor(ctx -> {
            if (ctx.commandContext().sender() instanceof PlayerSource source) {
                Player player = source.source();
                if (plugin.getCombatManager().isInCombat(player)) {
                    throw new CombatBlockedException();
                }
            }
        });

        // Combat exception handler
        commandManager.exceptionController().registerHandler(CombatBlockedException.class,
                (ExceptionContext<Source, CombatBlockedException> ctx) -> {
                    if (ctx.context().sender() instanceof PlayerSource source) {
                        Player player = source.source();
                        player.sendMessage(configManager.getMessage("in-combat"));
                    }
                });

        // Eager class preloading — force JVM ClassLoader to resolve heavy classes at
        // startup,
        // eliminating first-execution lag spikes when a player runs a command for the
        // first time
        try {
            // Force JVM to resolve all GUI classes at startup — eliminates first-execution
            // lag
            Class.forName(GuiHelper.class.getName());
            Class.forName(MainMenuGUI.class.getName());
            Class.forName(ClaimSettingsGUI.class.getName());
            Class.forName(ClaimMapGUI.class.getName());
            Class.forName(ClaimMapInfoGUI.class.getName());
            Class.forName(AllyControlPanelGUI.class.getName());
            Class.forName(AllyManagementGUI.class.getName());
            Class.forName(AllyPremissionsGUI.class.getName());
            Class.forName(ChangeClaimColorGUI.class.getName());
            Class.forName(MemberManagementGUI.class.getName());
            Class.forName(PlayerControlPanelGUI.class.getName());
            Class.forName(PlayerTrustPermissionGUI.class.getName());
            Class.forName(RenameClaimGUI.class.getName());
            Class.forName(RoleManagementGUI.class.getName());
            Class.forName(RoleSelectionGUI.class.getName());
            Class.forName(RoleSetupGUI.class.getName());
            Class.forName(TitleToggleGUI.class.getName());
            Class.forName(TrustManagementGUI.class.getName());
            Class.forName(VisitorSettingsGUI.class.getName());
            Class.forName(WarpChangeIconGUI.class.getName());
            Class.forName(WarpControlPanelGUI.class.getName());
            Class.forName(WarpManagementGUI.class.getName());
        } catch (ClassNotFoundException ignored) {
            // Should never happen — classes are in same JAR
        }

        // Instantiate modular command groups
        claimCommand = new ClaimCommand(plugin, claimManager, configManager, visualizationManager);

        List<LandClaimCommand> commands = List.of(
                claimCommand,
                new UnclaimCommand(plugin, claimManager, configManager),
                new AdminCommand(plugin, claimManager, configManager),
                new MemberCommand(plugin, claimManager, configManager),
                new TrustCommand(configManager));

        // Register all commands via the shared /claim builder
        Command.Builder<PlayerSource> claimBuilder = commandManager.commandBuilder("claim", "c")
                .senderType(PlayerSource.class);

        for (LandClaimCommand cmd : commands) {
            cmd.register(commandManager, claimBuilder);
        }
    }

    // --- Delegates to ClaimCommand for auto-claim state ---

    public boolean isAutoClaimEnabled(UUID playerId) {
        return claimCommand.isAutoClaimEnabled(playerId);
    }

    public boolean isAutoUnclaimEnabled(UUID playerId) {
        return claimCommand.isAutoUnclaimEnabled(playerId);
    }

    public void cleanupPlayer(UUID playerId) {
        claimCommand.cleanupPlayer(playerId);
    }

    /**
     * Gracefully shuts down the command executor thread pool.
     * Call this from LandClaimPlugin.onDisable().
     */
    public void shutdown() {
        if (commandExecutor != null && !commandExecutor.isShutdown()) {
            commandExecutor.shutdown();
            try {
                if (!commandExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    commandExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                commandExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}