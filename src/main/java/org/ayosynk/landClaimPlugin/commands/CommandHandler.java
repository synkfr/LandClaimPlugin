package org.ayosynk.landClaimPlugin.commands;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.exceptions.CombatBlockedException;
import org.ayosynk.landClaimPlugin.gui.*;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.managers.ConfigManager;

import org.ayosynk.landClaimPlugin.managers.VisualizationManager;
import org.ayosynk.landClaimPlugin.managers.WarpManager;
import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
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
 * <p>Uses a custom {@link ExecutionCoordinator} with per-phase thread isolation:</p>
 * <ul>
 *   <li>Parsing: non-scheduling executor (calling thread) — lightweight tokenization</li>
 *   <li>Suggestions: non-scheduling executor (calling thread) — prevents deadlocks</li>
 *   <li>Execution:
 *     <ul>
 *       <li><b>Paper</b>: dedicated 4-thread daemon pool (off main thread for DB/GUI work)</li>
 *       <li><b>Folia</b>: {@code simpleCoordinator()} so handlers run on the calling
 *           thread, which is the player's region thread. The dedicated pool is unsafe on
 *           Folia because pool threads are not region threads, and any
 *           {@code player.getLocation() / new ChunkPosition(...)} call would fail the
 *           Folia main-thread check.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * Each command group is a separate {@link LandClaimCommand} implementation registered
 * modularly.
 */
public class CommandHandler {

    private final ClaimCommand claimCommand;
    private final ExecutorService commandExecutor;

    public CommandHandler(LandClaimPlugin plugin, ClaimManager claimManager,
            ConfigManager configManager,
            VisualizationManager visualizationManager, WarpManager warpManager) {

        ExecutionCoordinator<Source> coordinator;
        if (FoliaScheduler.isFolia()) {
            // Folia: let handlers run on the calling thread (the player's region thread).
            // Command handlers that need to do region-mutating work must explicitly route
            // via FoliaScheduler.runForPlayer / runAtLocation.
            coordinator = ExecutionCoordinator.simpleCoordinator();
            commandExecutor = null;
        } else {
            // Paper: dedicated thread pool for off-main-thread command execution.
            commandExecutor = Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "LandClaim-Command-Worker");
                t.setDaemon(true);
                return t;
            });

            coordinator = ExecutionCoordinator.<Source>builder()
                    .parsingExecutor(ExecutionCoordinator.nonSchedulingExecutor())
                    .suggestionsExecutor(ExecutionCoordinator.nonSchedulingExecutor())
                    .executionSchedulingExecutor(commandExecutor)
                    .build();
        }

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

        // Instantiate modular command groups
        claimCommand = new ClaimCommand(plugin, claimManager, configManager, visualizationManager);

        List<LandClaimCommand> commands = List.of(
                claimCommand,
                new UnclaimCommand(plugin, claimManager, configManager),
                new AdminCommand(plugin, claimManager, configManager),
                new MemberCommand(plugin, claimManager, configManager),
                new TrustCommand(plugin, claimManager, configManager),
                new AllyCommand(plugin, claimManager, configManager),
                new AbandonCommand(plugin, claimManager, configManager),
                new UnstuckCommand(plugin, claimManager, configManager));

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
     * No-op on Folia (no dedicated pool was created).
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