package com.bgsoftware.wildloaders.scheduler;

import org.bukkit.World;
import org.bukkit.entity.Entity;

public interface ISchedulerImplementation {

    boolean isRegionScheduler();

    ScheduledTask scheduleTask(World world, int chunkX, int chunkZ, Runnable task, long delay);

    ScheduledTask scheduleTask(Entity entity, Runnable task, long delay);

    ScheduledTask scheduleTask(Runnable task, long delay);

    ScheduledTask scheduleAsyncTask(Runnable task, long delay);

}
