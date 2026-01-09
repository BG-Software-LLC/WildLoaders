package com.bgsoftware.wildloaders.chunks;

import com.bgsoftware.wildloaders.utils.chunks.Chunk2ObjectMap;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import org.bukkit.Chunk;

import java.util.concurrent.atomic.AtomicInteger;

public class ChunksTracker {

    private static final Chunk2ObjectMap<AtomicInteger> trackedChunks = new Chunk2ObjectMap<>();

    private ChunksTracker() {

    }

    public static boolean track(Chunk chunk) {
        return trackedChunks.computeIfAbsent(chunk.getWorld().getName(), ChunkPosition.pair(chunk.getX(), chunk.getZ()),
                () -> new AtomicInteger(0)).getAndIncrement() == 0;
    }

    public static boolean untrack(Chunk chunk) {
        AtomicInteger current = trackedChunks.get(chunk.getWorld().getName(), ChunkPosition.pair(chunk.getX(), chunk.getZ()));
        return current != null && current.decrementAndGet() == 0;
    }

}
