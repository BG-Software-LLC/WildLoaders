package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.TickableProvider;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TickableProvider_EpicSpawners7 implements TickableProvider {

    private final Map<Location, TickDelay> spawnerDelays = new HashMap<>();

    @Override
    public void tick(Collection<Chunk> chunks) {
        if (EpicSpawners.getInstance().getSpawnerManager() == null)
            return;

        Set<Long> chunkKeys = new HashSet<>();
        chunks.forEach(chunk -> chunkKeys.add(pair(chunk.getX(), chunk.getZ())));

        EpicSpawners.getInstance().getSpawnerManager().getSpawners().stream()
                .filter(spawner -> chunkKeys.contains(pair(spawner.getX() >> 4, spawner.getZ() >> 4)))
                .forEach(spawner -> {
                    Location location = spawner.getLocation();
                    TickDelay tickDelay = spawnerDelays.get(location);

                    if (tickDelay == null) {
                        spawnerDelays.put(location, new TickDelay(spawner.updateDelay()));
                        return;
                    }

                    tickDelay.delay -= 1;

                    if (tickDelay.delay <= 0) {
                        spawner.spawn();
                        spawnerDelays.remove(location);
                    }
                });
    }

    private long pair(int x, int z) {
        return (x & 0xFFFFFFFFL) | (z & 0xFFFFFFFFL) << 32;
    }

    private static final class TickDelay {

        private int delay;

        TickDelay(int delay) {
            this.delay = delay;
        }

    }

}
