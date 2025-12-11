package io.th0rgal.oraxen.utils.scheduler;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.utils.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Scheduler utility that provides Folia-compatible task scheduling.
 * Automatically detects Folia and uses region-based scheduling when available.
 */
public class TaskScheduler {

    private static final boolean IS_FOLIA = VersionUtil.isFoliaServer();
    private static final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();

    private TaskScheduler() {
    }

    /**
     * Schedules a task to run synchronously on the next tick.
     *
     * @param task the task to run
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTask(@NotNull Runnable task) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTask(task);
        }
        return BUKKIT_SCHEDULER.runTask(OraxenPlugin.get(), task);
    }

    /**
     * Schedules a task to run synchronously on the next tick for the given entity's region.
     *
     * @param entity the entity to schedule the task for
     * @param task   the task to run
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTask(@NotNull Entity entity, @NotNull Runnable task) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTask(entity, task);
        }
        return BUKKIT_SCHEDULER.runTask(OraxenPlugin.get(), task);
    }

    /**
     * Schedules a task to run synchronously on the next tick for the given location's region.
     *
     * @param location the location to schedule the task for
     * @param task     the task to run
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTask(@NotNull Location location, @NotNull Runnable task) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTask(location, task);
        }
        return BUKKIT_SCHEDULER.runTask(OraxenPlugin.get(), task);
    }

    /**
     * Schedules a task to run synchronously after the specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskLater(@NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskLater(task, delay);
        }
        return BUKKIT_SCHEDULER.runTaskLater(OraxenPlugin.get(), task, delay);
    }

    /**
     * Schedules a task to run synchronously after the specified delay for the given entity's region.
     *
     * @param entity the entity to schedule the task for
     * @param task   the task to run
     * @param delay  the delay in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskLater(@NotNull Entity entity, @NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskLater(entity, task, delay);
        }
        return BUKKIT_SCHEDULER.runTaskLater(OraxenPlugin.get(), task, delay);
    }

    /**
     * Schedules a task to run synchronously after the specified delay for the given location's region.
     *
     * @param location the location to schedule the task for
     * @param task     the task to run
     * @param delay    the delay in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskLater(@NotNull Location location, @NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskLater(location, task, delay);
        }
        return BUKKIT_SCHEDULER.runTaskLater(OraxenPlugin.get(), task, delay);
    }

    /**
     * Schedules a repeating task to run synchronously.
     *
     * @param task   the task to run
     * @param delay  the delay before first execution in ticks
     * @param period the period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskTimer(@NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskTimer(task, delay, period);
        }
        return BUKKIT_SCHEDULER.runTaskTimer(OraxenPlugin.get(), task, delay, period);
    }

    /**
     * Schedules a repeating task to run synchronously for the given entity's region.
     *
     * @param entity the entity to schedule the task for
     * @param task   the task to run
     * @param delay  the delay before first execution in ticks
     * @param period the period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskTimer(@NotNull Entity entity, @NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskTimer(entity, task, delay, period);
        }
        return BUKKIT_SCHEDULER.runTaskTimer(OraxenPlugin.get(), task, delay, period);
    }

    /**
     * Schedules a repeating task to run synchronously for the given location's region.
     *
     * @param location the location to schedule the task for
     * @param task     the task to run
     * @param delay    the delay before first execution in ticks
     * @param period   the period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskTimer(@NotNull Location location, @NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskTimer(location, task, delay, period);
        }
        return BUKKIT_SCHEDULER.runTaskTimer(OraxenPlugin.get(), task, delay, period);
    }

    /**
     * Schedules a repeating task using a consumer that receives the BukkitTask.
     *
     * @param task   the task consumer to run
     * @param delay  the delay before first execution in ticks
     * @param period the period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskTimer(@NotNull Consumer<BukkitTask> task, long delay, long period) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskTimer(task, delay, period);
        }
        java.util.concurrent.atomic.AtomicReference<BukkitTask> taskRef = new java.util.concurrent.atomic.AtomicReference<>();
        org.bukkit.scheduler.BukkitRunnable runnable = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                BukkitTask currentTask = taskRef.get();
                if (currentTask != null) {
                    task.accept(currentTask);
                }
            }
        };
        BukkitTask scheduledTask = runnable.runTaskTimer(OraxenPlugin.get(), delay, period);
        taskRef.set(scheduledTask);
        return scheduledTask;
    }

    /**
     * Schedules a task to run asynchronously.
     *
     * @param task the task to run
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskAsynchronously(@NotNull Runnable task) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskAsynchronously(task);
        }
        return BUKKIT_SCHEDULER.runTaskAsynchronously(OraxenPlugin.get(), task);
    }

    /**
     * Schedules a task to run asynchronously after the specified delay.
     *
     * @param task  the task to run
     * @param delay the delay in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskLaterAsynchronously(@NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskLaterAsynchronously(task, delay);
        }
        return BUKKIT_SCHEDULER.runTaskLaterAsynchronously(OraxenPlugin.get(), task, delay);
    }

    /**
     * Schedules a repeating task to run asynchronously.
     *
     * @param task   the task to run
     * @param delay  the delay before first execution in ticks
     * @param period the period between executions in ticks
     * @return the scheduled task
     */
    @NotNull
    public static BukkitTask runTaskTimerAsynchronously(@NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            return FoliaScheduler.runTaskTimerAsynchronously(task, delay, period);
        }
        return BUKKIT_SCHEDULER.runTaskTimerAsynchronously(OraxenPlugin.get(), task, delay, period);
    }

    /**
     * Checks if the current thread is the primary server thread.
     * On Folia, this always returns false as there is no primary thread.
     *
     * @return true if on the primary thread, false otherwise
     */
    public static boolean isPrimaryThread() {
        if (IS_FOLIA) {
            return false;
        }
        return Bukkit.isPrimaryThread();
    }

    /**
     * Checks if Folia is being used.
     *
     * @return true if Folia is detected, false otherwise
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }
}

