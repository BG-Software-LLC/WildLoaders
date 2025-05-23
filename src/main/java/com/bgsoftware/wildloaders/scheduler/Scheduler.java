package com.bgsoftware.wildloaders.scheduler;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class Scheduler {

    private static final ScheduledTask NULL_TASK = () -> {};

    private static final ISchedulerImplementation IMP = initializeSchedulerImplementation();

    private static ISchedulerImplementation initializeSchedulerImplementation() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
        } catch (ClassNotFoundException error) {
            return BukkitSchedulerImplementation.INSTANCE;
        }

        // Detected Folia, create its scheduler
        try {
            Class<?> foliaSchedulerClass = Class.forName("com.bgsoftware.wildloaders.scheduler.FoliaSchedulerImplementation");
            return (ISchedulerImplementation) foliaSchedulerClass.getField("INSTANCE").get(null);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private static boolean isEnabled = true;

    private Scheduler() {

    }

    public static void initialize() {
        // Do nothing, load static initializer
    }

    public static void disable() {
        isEnabled = false;
    }

    public static boolean isRegionScheduler() {
        return IMP.isRegionScheduler();
    }

    public static ScheduledTask runTask(World world, int chunkX, int chunkZ, Runnable task, long delay) {
        if (!isEnabled) {
            return executeNow(task);
        }

        return IMP.scheduleTask(world, chunkX, chunkZ, task, delay);
    }

    public static ScheduledTask runTask(Entity entity, Runnable task, long delay) {
        if (!isEnabled) {
            return executeNow(task);
        }

        return IMP.scheduleTask(entity, task, delay);
    }

    public static ScheduledTask runTask(Runnable task, long delay) {
        if (!isEnabled) {
            return executeNow(task);
        }

        return IMP.scheduleTask(task, delay);
    }

    public static ScheduledTask runTaskAsync(Runnable task, long delay) {
        if (!isEnabled) {
            return executeNow(task);
        }

        return IMP.scheduleAsyncTask(task, delay);
    }

    public static ScheduledTask runTask(Chunk chunk, Runnable task, long delay) {
        return runTask(chunk.getWorld(), chunk.getX(), chunk.getZ(), task, delay);
    }

    public static ScheduledTask runTask(Chunk chunk, Runnable task) {
        return runTask(chunk, task, 0L);
    }

    public static ScheduledTask runTask(Location location, Runnable task, long delay) {
        return runTask(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, delay);
    }

    public static ScheduledTask runTask(World world, int chunkX, int chunkZ, Runnable task) {
        return runTask(world, chunkX, chunkZ, task, 0L);
    }

    public static ScheduledTask runTask(Entity entity, Runnable task) {
        return runTask(entity, task, 0L);
    }

    public static ScheduledTask runTask(Location location, Runnable task) {
        return runTask(location, task, 0L);
    }

    public static ScheduledTask runTask(Runnable task) {
        return runTask(task, 0L);
    }

    public static ScheduledTask runTaskAsync(Runnable task) {
        return runTaskAsync(task, 0L);
    }

    private static ScheduledTask executeNow(Runnable task) {
        task.run();
        return NULL_TASK;
    }

}
