package org.ayosynk.landClaimPlugin.util;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Unified scheduler facade that works on both Paper and Folia.
 *
 * <p>On Paper (and Bukkit/Spigot), {@code Bukkit.getScheduler()} schedules against a single
 * main thread. On Folia, that scheduler does not exist; the world is sharded into independent
 * region threads, and code touching an entity, block, chunk, or world must run on the thread
 * that owns that region. Region-less code can run on the {@link GlobalRegionScheduler}, and
 * fully async work can run on the {@link AsyncScheduler} or any executor.</p>
 *
 * <p>The API intentionally mirrors {@code Bukkit.getScheduler()} so the migration is mostly
 * mechanical: every {@code Bukkit.getScheduler().runTask(plugin, () -&gt; ...)} becomes
 * {@code FoliaScheduler.runTask(plugin, () -&gt; ...)}; every
 * {@code runTaskAsynchronously} becomes {@code runAsync}; every {@code runTaskTimer} becomes
 * {@code runTaskTimer} (which on Folia requires a target {@link Player} or {@link Location}
 * &mdash; pass {@code null} for global-region behaviour).</p>
 *
 * <p>Folia is detected by reflection on the global region scheduler accessor. If unavailable,
 * the implementation falls back to {@code Bukkit.getScheduler()} transparently.</p>
 */
public final class FoliaScheduler {

    private static final boolean IS_FOLIA = detectFolia();
    private static final Method GLOBAL_REGION_SCHEDULER_METHOD;
    private static final Method ASYNC_SCHEDULER_METHOD;
    private static final Method REGION_SCHEDULER_METHOD;
    private static final Method PLAYER_SCHEDULER_METHOD;

    static {
        Method global = null;
        Method async = null;
        Method region = null;
        Method player = null;
        if (IS_FOLIA) {
            try {
                // Folia adds these to the Server interface (returned by Bukkit.getServer()).
                Class<?> serverClass = Bukkit.getServer().getClass();
                global = serverClass.getMethod("getGlobalRegionScheduler");
                async = serverClass.getMethod("getAsyncScheduler");
                region = serverClass.getMethod("getRegionScheduler");
                // Player#getScheduler is added by Folia directly on the Player interface.
                player = Player.class.getMethod("getScheduler");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Folia detected but scheduler methods missing", e);
            }
        }
        GLOBAL_REGION_SCHEDULER_METHOD = global;
        ASYNC_SCHEDULER_METHOD = async;
        REGION_SCHEDULER_METHOD = region;
        PLAYER_SCHEDULER_METHOD = player;
    }

    private FoliaScheduler() {}

    /** True if the running server is Folia. */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Runs the task on the global region thread (Folia) or the main thread (Paper).
     * <p>Equivalent to {@code Bukkit.getScheduler().runTask(plugin, runnable)}.</p>
     */
    public static void runTask(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                GlobalRegionScheduler scheduler = (GlobalRegionScheduler) GLOBAL_REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.run(plugin, (Consumer<ScheduledTask>) task -> runnable.run());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to run task on Folia global region", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Runs the task on the global region thread after {@code delay} ticks.
     * <p>Equivalent to {@code Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)}.</p>
     */
    public static void runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        if (IS_FOLIA) {
            try {
                GlobalRegionScheduler scheduler = (GlobalRegionScheduler) GLOBAL_REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.runDelayed(plugin, (Consumer<ScheduledTask>) task -> runnable.run(), delay);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule delayed task on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Runs the task asynchronously (no Bukkit API access allowed).
     * <p>Equivalent to {@code Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)}.</p>
     */
    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                AsyncScheduler scheduler = (AsyncScheduler) ASYNC_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.runNow(plugin, (Consumer<ScheduledTask>) task -> runnable.run());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to run async task on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    /**
     * Runs the task asynchronously after {@code delay} ticks.
     * <p>Equivalent to {@code Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay)}.</p>
     */
    public static void runAsyncLater(Plugin plugin, Runnable runnable, long delay) {
        if (IS_FOLIA) {
            try {
                AsyncScheduler scheduler = (AsyncScheduler) ASYNC_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.runDelayed(plugin, (Consumer<ScheduledTask>) task -> runnable.run(), delay * 50L, TimeUnit.MILLISECONDS);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule delayed async task on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }

    /**
     * Schedules a repeating task on the global region thread (Folia) or main thread (Paper).
     * <p>Returns a {@link ScheduledHandle} that can cancel the task. On Paper, repeating tasks
     * that block the server will cause lag &mdash; prefer {@link #runPerPlayerTimer} for
     * per-player work on Folia, where each player gets their own repeating task on their own
     * region thread.</p>
     */
    public static ScheduledHandle runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        if (IS_FOLIA) {
            try {
                GlobalRegionScheduler scheduler = (GlobalRegionScheduler) GLOBAL_REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                ScheduledTask task = scheduler.runAtFixedRate(plugin, (Consumer<ScheduledTask>) t -> runnable.run(), delay, period);
                return new ScheduledHandle(task, ScheduledHandle.Type.GLOBAL);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule repeating global task on Folia", e);
            }
        } else {
            int id = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period).getTaskId();
            return new ScheduledHandle(id, ScheduledHandle.Type.BUKKIT_ID);
        }
    }

    /**
     * Schedules a global tick that, on every fire, iterates all online players and dispatches
     * a per-player runnable on the player's region thread (Folia) or runs it inline (Paper).
     * <p>This is the Folia-safe replacement for a "global timer that does per-player work".
     * On Folia, the iteration of {@code Bukkit.getOnlinePlayers()} runs on the global region
     * (safe), but the per-player work runs on the player's own region thread (required for
     * safe access to player state, location, chunk, etc.).</p>
     */
    public static ScheduledHandle runPlayerTaskTimer(Plugin plugin, Consumer<Player> perPlayer, long delay, long period) {
        if (IS_FOLIA) {
            // On Folia, a single global-region tick that dispatches per-player work to each
            // player's region. This is the canonical pattern for action-bar / visualization
            // tasks that previously iterated online players on the main thread.
            try {
                GlobalRegionScheduler scheduler = (GlobalRegionScheduler) GLOBAL_REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                ScheduledTask task = scheduler.runAtFixedRate(plugin, (Consumer<ScheduledTask>) t -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        try {
                            Object playerScheduler = PLAYER_SCHEDULER_METHOD.invoke(p);
                            Method runMethod = playerScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
                            runMethod.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) inner -> perPlayer.accept(p), null);
                        } catch (ReflectiveOperationException e) {
                            // Best-effort: skip this player if the scheduler isn't available
                        }
                    }
                }, delay, period);
                return new ScheduledHandle(task, ScheduledHandle.Type.GLOBAL);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule player task timer on Folia", e);
            }
        } else {
            int id = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    perPlayer.accept(p);
                }
            }, delay, period).getTaskId();
            return new ScheduledHandle(id, ScheduledHandle.Type.BUKKIT_ID);
        }
    }

    /**
     * Schedules a per-player repeating task. On Folia this runs on the player's region
     * thread; on Paper it runs on the main thread (caller should guard with
     * {@code target.isOnline()} if needed).
     */
    public static ScheduledHandle runPerPlayerTimer(Plugin plugin, Player target, Runnable runnable, long delay, long period) {
        if (IS_FOLIA) {
            try {
                Object playerScheduler = PLAYER_SCHEDULER_METHOD.invoke(target);
                // PlayerScheduler#runAtFixedRate(Plugin, Consumer<ScheduledTask>, long initialDelay, long period, TimeUnit)
                Method runAtFixedRate = null;
                for (Method m : playerScheduler.getClass().getMethods()) {
                    if ("runAtFixedRate".equals(m.getName())) {
                        runAtFixedRate = m;
                        break;
                    }
                }
                if (runAtFixedRate != null) {
                    ScheduledTask task = (ScheduledTask) runAtFixedRate.invoke(playerScheduler,
                            plugin, (Consumer<ScheduledTask>) t -> runnable.run(), delay, period, TimeUnit.MILLISECONDS);
                    return new ScheduledHandle(task, ScheduledHandle.Type.PLAYER);
                } else {
                    // Fallback: chain runDelayed calls
                    schedulePerPlayerRecurring(playerScheduler, plugin, target, runnable, delay, period);
                    return new ScheduledHandle(null, ScheduledHandle.Type.PLAYER_RECURRING);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule per-player timer on Folia", e);
            }
        } else {
            int id = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (target.isOnline()) runnable.run();
            }, delay, period).getTaskId();
            return new ScheduledHandle(id, ScheduledHandle.Type.BUKKIT_ID);
        }
    }

    private static void schedulePerPlayerRecurring(Object playerScheduler, Plugin plugin, Player target,
                                                    Runnable runnable, long delay, long period) {
        try {
            // runDelayed(Plugin, Consumer<ScheduledTask>, long delay)
            Method runDelayed = playerScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            // Schedule the first execution. Inside that, schedule the next.
            runDelayed.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) task -> {
                if (!target.isOnline()) return;
                runnable.run();
                // Schedule the next iteration
                try {
                    runDelayed.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) t -> {
                        if (!target.isOnline()) return;
                        runnable.run();
                        // And the next after that
                        try {
                            runDelayed.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) t2 -> {
                                if (!target.isOnline()) return;
                                runnable.run();
                                schedulePerPlayerRecurring(playerScheduler, plugin, target, runnable, period, period);
                            }, period);
                        } catch (ReflectiveOperationException ignored) {}
                    }, period);
                } catch (ReflectiveOperationException ignored) {}
            }, delay);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to schedule recurring per-player task on Folia", e);
        }
    }

    /**
     * Schedules a task on the region's thread that owns the given location. Use this when
     * mutating world state at a known location (placing blocks, spawning entities, etc.).
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                RegionScheduler scheduler = (RegionScheduler) REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.execute(plugin, location, runnable);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to run task at location on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Schedules a delayed task on the region's thread that owns the given location.
     */
    public static void runAtLocationLater(Plugin plugin, Location location, Runnable runnable, long delay) {
        if (IS_FOLIA) {
            try {
                RegionScheduler scheduler = (RegionScheduler) REGION_SCHEDULER_METHOD.invoke(Bukkit.getServer());
                scheduler.runDelayed(plugin, location, (Consumer<ScheduledTask>) t -> runnable.run(), delay);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule delayed task at location on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Schedules a task on the thread that owns the given entity. Use this when mutating
     * entity state or performing actions on an entity the caller has a reference to.
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable runnable) {
        if (IS_FOLIA) {
            try {
                Method getEntityScheduler = entity.getClass().getMethod("getScheduler");
                Object entityScheduler = getEntityScheduler.invoke(entity);
                Method runMethod = entityScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
                runMethod.invoke(entityScheduler, plugin, (Consumer<ScheduledTask>) t -> runnable.run(), null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to run task for entity on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Convenience: ensures the runnable runs on the player's region thread, falling back to
     * scheduling on the main thread on Paper. The player may be null &mdash; in that case the
     * runnable runs on the global region / main thread.
     */
    public static void runForPlayer(Plugin plugin, Player player, Runnable runnable) {
        if (player == null) {
            runTask(plugin, runnable);
            return;
        }
        if (IS_FOLIA) {
            try {
                Object playerScheduler = PLAYER_SCHEDULER_METHOD.invoke(player);
                Method runMethod = playerScheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
                runMethod.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) t -> runnable.run(), null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to run task for player on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Schedules a delayed task on the player's region thread.
     */
    public static void runForPlayerLater(Plugin plugin, Player player, Runnable runnable, long delay) {
        if (player == null) {
            runTaskLater(plugin, runnable, delay);
            return;
        }
        if (IS_FOLIA) {
            try {
                Object playerScheduler = PLAYER_SCHEDULER_METHOD.invoke(player);
                Method runDelayed = playerScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                runDelayed.invoke(playerScheduler, plugin, (Consumer<ScheduledTask>) t -> runnable.run(), delay);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to schedule delayed task for player on Folia", e);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Schedules an open-inventory call for a player. On Paper, this runs on the main thread;
     * on Folia, it runs on the player's region thread.
     */
    public static void openInventory(Plugin plugin, Player player, org.bukkit.inventory.Inventory inventory) {
        runForPlayer(plugin, player, () -> player.openInventory(inventory));
    }

    private static boolean detectFolia() {
        try {
            // The Folia-added methods live on the Server interface returned by Bukkit.getServer().
            Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Handle to a scheduled task. Use {@link #cancel()} to stop it. The handle is opaque
     * &mdash; do not assume any particular type.
     */
    public static final class ScheduledHandle {
        enum Type { BUKKIT_ID, GLOBAL, PLAYER, PLAYER_RECURRING }
        private final Object handle;
        private final Type type;
        private boolean cancelled = false;

        ScheduledHandle(Object handle, Type type) {
            this.handle = handle;
            this.type = type;
        }

        public void cancel() {
            if (cancelled || handle == null) return;
            cancelled = true;
            try {
                switch (type) {
                    case BUKKIT_ID -> {
                        int id = (int) handle;
                        Bukkit.getScheduler().cancelTask(id);
                    }
                    case GLOBAL, PLAYER -> {
                        Method cancelMethod = handle.getClass().getMethod("cancel");
                        cancelMethod.invoke(handle);
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }
}
